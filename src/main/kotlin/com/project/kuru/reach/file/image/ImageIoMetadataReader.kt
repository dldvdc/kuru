package com.project.kuru.reach.file.image

import com.project.kuru.core.CoreException
import com.project.kuru.core.image.Dimension
import com.project.kuru.reach.mime.image.ImageFormat
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO

class ImageIoMetadataReader {

    fun read(tempFile: Path, format: ImageFormat): ImageMetadata {
        val mime = format.mime
        return try {
            Files.newInputStream(tempFile).use { input ->
                ImageIO.createImageInputStream(input).use { iis ->
                    if (iis == null) {
                        throw CoreException.InvalidFormat("image")
                    }
                    val reader = ImageIO.getImageReadersByMIMEType(mime).asSequence().firstOrNull()
                        ?: throw CoreException.InvalidFormat("image")

                    try {
                        reader.setInput(iis, true)
                        val width = reader.getWidth(0)
                        val height = reader.getHeight(0)
                        val animated = format == ImageFormat.Gif && isAnimatedGif(tempFile)

                        ImageMetadata(
                            dimensions = Dimension(width, height),
                            color = ImageReaderColorProfile.fromReader(reader),
                            animated = animated,
                        )
                    } finally {
                        reader.dispose()
                    }
                }
            }
        } catch (e: Exception) {
            CoreException.invalidFormat("image", e)
        }
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
}
