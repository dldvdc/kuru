package com.project.kuru.reach.ingest

import com.github.f4b6a3.ulid.UlidCreator
import com.project.kuru.core.CoreException
import com.project.kuru.flow.image.AcceptedImage
import com.project.kuru.reach.file.PartCopy
import com.project.kuru.reach.file.image.ImageMetadata
import com.project.kuru.reach.file.image.ImageIoMetadataReader
import com.project.kuru.reach.hash.Sha256Hasher
import com.project.kuru.reach.ingest.KuruImageProperties
import com.project.kuru.reach.mime.image.ImageFormat
import com.project.kuru.reach.mime.image.ImageMimeSniffer
import com.project.kuru.reach.storage.StagingStore
import com.project.kuru.screen.ingest.ImageIngestor
import com.project.kuru.screen.multipart.ValidatedUpload
import com.project.kuru.store.ObjectKeys
import java.nio.file.Files
import java.nio.file.Path

class ReachImageIngestor(
    private val props: KuruImageProperties,
    private val minHeaderSize: Int,
    private val imageMimeSniffer: ImageMimeSniffer,
    private val imageIoMetadataReader: ImageIoMetadataReader,
    private val stagingStore: StagingStore,
) : ImageIngestor {

    override fun accept(upload: ValidatedUpload): AcceptedImage {
        val tempFile = Files.createTempFile(props.tempPrefix, props.tempSuffix)
        return try {
            upload.content.use { stream ->
                acceptFromStream(upload, stream, tempFile)
            }
        } catch (e: Exception) {
            Files.deleteIfExists(tempFile)
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
            val format = copy.sniffFormat()
            val metadata = probeMetadata(tempFile, format)
                .requireDecodeBudget()
            val contentSha256 = Sha256Hasher.hash(tempFile)
            val extension = format.storageExtension(upload.fileName.value)
            val stagingKey = ObjectKeys.staging(UlidCreator.getUlid().toString(), extension)
            stagingStore.put(
                key = stagingKey,
                source = tempFile,
                contentType = format.mime,
                contentLength = copy.size,
            )
            return AcceptedImage(
                originalFileName = upload.fileName.value,
                mime = format.mime,
                extension = extension,
                sizeBytes = copy.size,
                contentSha256 = contentSha256,
                stagingKey = stagingKey,
                dimensions = metadata.dimensions,
                color = metadata.color,
                animated = metadata.animated,
            )
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
            CoreException.invalidFormat(KIND, e)
        }

    private fun PartCopy.Result.requireHeader(): PartCopy.Result {
        if (size < minHeaderSize) {
            throw CoreException.InvalidFormat(KIND)
        }
        return this
    }

    private fun PartCopy.Result.sniffFormat(): ImageFormat =
        imageMimeSniffer.sniffImage(header) ?: throw CoreException.InvalidFormat(KIND)

    private fun probeMetadata(tempFile: Path, format: ImageFormat) =
        imageIoMetadataReader.read(tempFile, format)

    private fun ImageMetadata.requireDecodeBudget(): ImageMetadata =
        apply {
            val estBytes = dimensions.totalPixels * props.bytesPerPixelEstimate
            if (estBytes > props.maxMemoryBytes) {
                throw CoreException.InvalidFormat(KIND)
            }
        }

    companion object {
        private const val KIND = "image"
    }
}
