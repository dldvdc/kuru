package com.project.kuru.screen.mapper

import com.project.kuru.core.CoreException
import com.project.kuru.core.image.ColorSpace
import com.project.kuru.core.image.ImageFormat
import com.project.kuru.core.image.RawImageFile
import jakarta.servlet.http.Part
import java.awt.image.ColorModel
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import javax.imageio.ImageReader

class PartImageMapper(
    private val config: SubmittedFileConfigValues
) {
    fun fromPart(part: Part): RawImageFile {
        val fileName = part.submittedFileName?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: throw CoreException.InvalidFormat("image")

        val tempFile = Files.createTempFile(config.tempPrefix, config.tempSuffix)
        return try {
            val copyResult = copyWithLimitAndHeader(part, tempFile)
            if (copyResult.size < config.minHeaderSize) {
                throw CoreException.InvalidFormat("image")
            }

            val mimeType = detectMime(copyResult.header)
            val metadata = readMetadata(tempFile, mimeType.value)

            if (metadata.dimensions.totalPixels * config.bytesPerPixelEstimate > config.maxMemoryBytes) {
                throw CoreException.InvalidFormat("image")
            }

            RawImageFile(
                originalFileName = fileName,
                mimeType = mimeType,
                size = copyResult.size,
                tempFile = tempFile,
                metadata = metadata
            )
        } catch (e: Exception) {
            Files.deleteIfExists(tempFile)
            throw CoreException.InvalidFormat("image")
        }
    }

    private fun readMetadata(tempFile: Path, mimeType: String): RawImageFile.Metadata {
        return try {
            Files.newInputStream(tempFile).use { input ->
                ImageIO.createImageInputStream(input).use { iis ->
                    val reader = ImageIO.getImageReadersByMIMEType(mimeType).asSequence().firstOrNull()
                        ?: throw CoreException.InvalidFormat("image")

                    try {
                        reader.setInput(iis, true)
                        val width = reader.getWidth(0)
                        val height = reader.getHeight(0)
                        val cm = resolveColorModel(reader)
                        val animated = mimeType == "image/gif" && isAnimatedGif(tempFile)

                        RawImageFile.Metadata(
                            format = ImageFormat.fromMimeType(mimeType),
                            dimensions = RawImageFile.Metadata.Dimension(width, height),
                            color = toColorProfile(cm),
                            animated = animated
                        )
                    } finally {
                        reader.dispose()
                    }
                }
            }
        } catch (e: Exception) {
            throw CoreException.InvalidFormat("image")
        }
    }

    private fun resolveColorModel(reader: ImageReader): ColorModel? {
        val rawType = runCatching { reader.getRawImageType(0) }.getOrNull()
        if (rawType != null) {
            return rawType.colorModel
        }

        val imageType = runCatching { reader.getImageTypes(0).asSequence().firstOrNull() }.getOrNull()
        if (imageType != null) {
            return imageType.colorModel
        }

        val firstFrame = runCatching { reader.read(0) }.getOrNull()
        return firstFrame?.colorModel
    }

    private fun toColorProfile(colorModel: ColorModel?): RawImageFile.Metadata.ColorProfile {
        if (colorModel == null) {
            return RawImageFile.Metadata.ColorProfile(
                depth = 24,
                model = ColorSpace.UNKNOWN,
                hasAlpha = false
            )
        }
        return RawImageFile.Metadata.ColorProfile(
            depth = colorModel.pixelSize.coerceIn(1, 64),
            model = mapColorSpace(colorModel),
            hasAlpha = colorModel.hasAlpha()
        )
    }

    private fun detectMime(header: ByteArray): RawImageFile.MimeType {
        val b = header
        return when {
            b[0] == 0xFF.b && b[1] == 0xD8.b && b[2] == 0xFF.b ->
                RawImageFile.MimeType("image/jpeg")

            b[0] == 0x89.b && b[1] == 0x50.b && b[2] == 0x4E.b && b[3] == 0x47.b &&
                    b[4] == 0x0D.b && b[5] == 0x0A.b && b[6] == 0x1A.b && b[7] == 0x0A.b ->
                RawImageFile.MimeType("image/png")

            b[0] == 0x47.b && b[1] == 0x49.b && b[2] == 0x46.b && b[3] == 0x38.b &&
                    (b[4] == 0x37.b || b[4] == 0x39.b) && b[5] == 0x61.b ->
                RawImageFile.MimeType("image/gif")

            b[0] == 0x52.b && b[1] == 0x49.b && b[2] == 0x46.b && b[3] == 0x46.b &&
                    b[8] == 0x57.b && b[9] == 0x45.b && b[10] == 0x42.b && b[11] == 0x50.b ->
                RawImageFile.MimeType("image/webp")

            else -> throw RuntimeException("image")
        }
    }

    private val Int.b get() = toByte()

    private fun mapColorSpace(model: ColorModel): ColorSpace =
        when (model.colorSpace.type) {
            java.awt.color.ColorSpace.TYPE_GRAY -> ColorSpace.GRAYSCALE
            java.awt.color.ColorSpace.TYPE_RGB -> if (model.hasAlpha()) ColorSpace.RGBA else ColorSpace.RGB
            java.awt.color.ColorSpace.TYPE_CMYK -> ColorSpace.CMYK
            else -> ColorSpace.UNKNOWN
        }

    private fun isAnimatedGif(tempFile: Path): Boolean {
        return try {
            Files.newInputStream(tempFile).use { input ->
                ImageIO.createImageInputStream(input).use { iis ->
                    val reader = ImageIO.getImageReadersByFormatName("gif").asSequence().firstOrNull() ?: return false
                    reader.setInput(iis)
                    try {
                        reader.read(1)
                        true
                    } catch (_: IndexOutOfBoundsException) {
                        false
                    } finally {
                        reader.dispose()
                    }
                }
            }
        } catch (_: IOException) {
            false
        }
    }

    private data class CopyResult(val size: Long, val header: ByteArray)

    private fun copyWithLimitAndHeader(part: Part, destination: Path): CopyResult {
        val header = ByteArrayOutputStream(config.minHeaderSize)
        var size = 0L
        part.inputStream.use { input ->
            Files.newOutputStream(destination).use { output ->
                val buffer = ByteArray(config.bufferSize)
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    size += read.toLong()
                    if (size > config.maxSizeBytes) {
                        throw CoreException.InvalidFormat("Mss")
                    }
                    output.write(buffer, 0, read)
                    if (header.size() < config.minHeaderSize) {
                        val missing = config.minHeaderSize - header.size()
                        header.write(buffer, 0, minOf(read, missing))
                    }
                }
            }
        }
        return CopyResult(size = size, header = header.toByteArray())
    }
}
