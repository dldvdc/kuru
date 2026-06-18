package com.project.kuru.reach.ingest

import com.github.f4b6a3.tsid.TsidCreator
import com.project.kuru.core.CoreException
import com.project.kuru.core.toHexString
import com.project.kuru.flow.image.AcceptedImage
import com.project.kuru.flow.image.CatalogEntry
import com.project.kuru.reach.file.PartCopy
import com.project.kuru.reach.file.image.ImageIoMetadataReader
import com.project.kuru.reach.file.image.IngestGuard
import com.project.kuru.reach.hash.Sha256Hasher
import com.project.kuru.reach.mime.image.ImageFormat
import com.project.kuru.reach.file.ImageMimeSniffer
import com.project.kuru.reach.storage.StagingStore
import com.project.kuru.screen.ingest.ImageIngestor
import com.project.kuru.screen.multipart.ValidatedUpload
import com.project.kuru.store.ObjectKeys
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Files
import java.nio.file.Path

private val log = KotlinLogging.logger {}

class ReachImageIngestor(
    private val props: KuruImageProperties,
    private val minHeaderSize: Int,
    private val imageMimeSniffer: ImageMimeSniffer,
    private val imageIoMetadataReader: ImageIoMetadataReader,
    private val stagingStore: StagingStore,
) : ImageIngestor {

    override fun accept(upload: ValidatedUpload): AcceptedImage {
        log.debug {
            "ingest[image]: début (file=${upload.fileName.value}, size=${upload.size}, " +
                "type=${upload.contentType})"
        }
        val tempFile = Files.createTempFile(props.tempPrefix, props.tempSuffix)
        return try {
            upload.content.use { stream ->
                acceptFromStream(upload, stream, tempFile)
            }
        } catch (e: Exception) {
            Files.deleteIfExists(tempFile)
            log.warn(e) { "ingest[image]: échec (file=${upload.fileName.value})" }
            CoreException.invalidFormat(KIND, e)
        }
    }

    private fun acceptFromStream(
        upload: ValidatedUpload,
        stream: java.io.InputStream,
        tempFile: Path,
    ): AcceptedImage {
        try {
            val copy = copyToTemp(stream, tempFile)
                .requireHeader()
            log.debug { "ingest[image]: copie temp OK (size=${copy.size}, header=${copy.header.size} o)" }

            val format = copy.sniffFormat()
            log.debug { "ingest[image]: format sniffé mime=${format.mime}" }

            val guard = probeGuard(tempFile, format)
                .requireDecodeBudget()
            log.debug {
                "ingest[image]: garde-fou OK (${guard.dimensions.width}x${guard.dimensions.height}, " +
                    "pixels=${guard.dimensions.totalPixels})"
            }

            val contentSha256 = Sha256Hasher.hash(tempFile)
            log.debug { "ingest[image]: SHA-256=${contentSha256.toHexString()}" }

            val extension = format.storageExtension(upload.fileName.value)
            val stagingKey = ObjectKeys.staging(TsidCreator.getTsid256().toString(), extension)
            log.debug { "ingest[image]: PUT staging key=$stagingKey" }

            stagingStore.put(
                key = stagingKey,
                source = tempFile,
                contentType = format.mime,
                contentLength = copy.size,
            )

            val accepted = AcceptedImage(
                stagingKey = stagingKey,
                entry = CatalogEntry(
                    originalFileName = upload.fileName.value,
                    mime = format.mime,
                    extension = extension,
                    sizeBytes = copy.size,
                    contentSha256 = contentSha256,
                ),
            )
            log.info {
                "ingest[image]: accepté (stagingKey=$stagingKey, ext=$extension, size=${copy.size})"
            }
            return accepted
        } finally {
            Files.deleteIfExists(tempFile)
        }
    }

    private fun copyToTemp(stream: java.io.InputStream, tempFile: Path): PartCopy.Result =
        try {
            PartCopy.toTempFile(
                input = stream,
                destination = tempFile,
                limits = PartCopy.Limits(
                    minHeaderSize = minHeaderSize,
                    maxSizeBytes = props.maxSizeBytes,
                    bufferSize = props.bufferSize,
                ),
            )
        } catch (e: Exception) {
            log.warn(e) { "ingest[image]: copie temp échouée" }
            CoreException.invalidFormat(KIND, e)
        }

    private fun PartCopy.Result.requireHeader(): PartCopy.Result {
        if (size < minHeaderSize) {
            log.warn { "ingest[image]: en-tête trop court ($size < $minHeaderSize)" }
            throw CoreException.InvalidFormat(KIND)
        }
        return this
    }

    private fun PartCopy.Result.sniffFormat(): ImageFormat =
        imageMimeSniffer.sniffImage(header) ?: run {
            log.warn { "ingest[image]: magic bytes non reconnus (header=${header.size} o)" }
            throw CoreException.InvalidFormat(KIND)
        }

    private fun probeGuard(tempFile: Path, format: ImageFormat): IngestGuard =
        imageIoMetadataReader.read(tempFile, format)

    private fun IngestGuard.requireDecodeBudget(): IngestGuard =
        apply {
            val estBytes = dimensions.totalPixels * props.bytesPerPixelEstimate
            if (estBytes > props.maxMemoryBytes) {
                log.warn {
                    "ingest[image]: budget decode dépassé (est=$estBytes > max=${props.maxMemoryBytes})"
                }
                throw CoreException.InvalidFormat(KIND)
            }
        }

    companion object {
        private const val KIND = "image"
    }
}
