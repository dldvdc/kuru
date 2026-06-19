package com.project.kuru.reach.ingest

import com.github.f4b6a3.tsid.TsidCreator
import com.project.kuru.core.CoreException
import com.project.kuru.core.toHexString
import com.project.kuru.flow.image.AcceptedImage
import com.project.kuru.flow.image.CatalogEntry
import com.project.kuru.reach.file.ImageMimeSniffer
import com.project.kuru.reach.file.image.ImageIoMetadataReader
import com.project.kuru.reach.file.image.IngestGuard
import com.project.kuru.store.StagingStore
import com.project.kuru.screen.ingest.ImageIngestor
import com.project.kuru.screen.multipart.ValidatedUpload
import com.project.kuru.store.ObjectKeys
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.SequenceInputStream

private val log = KotlinLogging.logger {}

class FileIngestor(
    private val props: KuruImageProperties,
    private val minHeaderSize: Int,
    private val imageMimeSniffer: ImageMimeSniffer,
    private val imageIoMetadataReader: ImageIoMetadataReader,
    private val stagingStore: StagingStore,
) : ImageIngestor {

    override fun stage(upload: ValidatedUpload): AcceptedImage {
        log.debug {
            "ingest[image]: début (file=${upload.fileName.value}, size=${upload.size}, " +
                "type=${upload.contentType})"
        }
        return try {
            upload.content.use { raw -> acceptFromStream(upload, raw) }
        } catch (e: Exception) {
            log.warn(e) { "ingest[image]: échec (file=${upload.fileName.value})" }
            CoreException.invalidFormat(KIND, e)
        }
    }

    private fun acceptFromStream(upload: ValidatedUpload, raw: java.io.InputStream): AcceptedImage {
        val header = raw.readNBytes(minHeaderSize)
        if (header.size < minHeaderSize) {
            log.warn { "ingest[image]: en-tête trop court (${header.size} < $minHeaderSize)" }
            throw CoreException.InvalidFormat(KIND)
        }
        log.debug { "ingest[image]: header lu (${header.size} o)" }

        val format = imageMimeSniffer.sniffImage(header) ?: run {
            log.warn { "ingest[image]: magic bytes non reconnus (header=${header.size} o)" }
            throw CoreException.InvalidFormat(KIND)
        }
        log.debug { "ingest[image]: format sniffé mime=${format.mime}" }

        val guard = imageIoMetadataReader.read(header.inputStream(), format)
            .requireDecodeBudget()
        log.debug {
            "ingest[image]: garde-fou OK (${guard.dimensions.width}x${guard.dimensions.height}, " +
                "pixels=${guard.dimensions.totalPixels})"
        }

        val full = SequenceInputStream(header.inputStream(), raw)
        val hashing = HashingInputStream(full)
        val counting = CountingInputStream(hashing, props.maxSizeBytes)

        val extension = format.storageExtension(upload.fileName.value)
        val stagingKey = ObjectKeys.staging(TsidCreator.getTsid256().toString(), extension)
        log.debug { "ingest[image]: PUT staging key=$stagingKey" }

        stagingStore.put(
            key = stagingKey,
            source = counting,
            contentType = format.mime,
            contentLength = upload.size,
        )

        val contentSha256 = hashing.hash()
        log.debug { "ingest[image]: SHA-256=${contentSha256.toHexString()}" }

        val accepted = AcceptedImage(
            stagingKey = stagingKey,
            entry = CatalogEntry(
                originalFileName = upload.fileName.value,
                mime = format.mime,
                extension = extension,
                sizeBytes = counting.bytesRead,
                contentSha256 = contentSha256,
            ),
        )
        log.info {
            "ingest[image]: accepté (stagingKey=$stagingKey, ext=$extension, size=${counting.bytesRead})"
        }
        return accepted
    }

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
