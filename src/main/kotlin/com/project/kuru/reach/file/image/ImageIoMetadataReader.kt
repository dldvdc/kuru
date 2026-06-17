package com.project.kuru.reach.file.image

import com.project.kuru.core.CoreException
import com.project.kuru.core.Dimension
import com.project.kuru.reach.mime.image.ImageFormat
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO

class ImageIoMetadataReader {

    fun read(tempFile: Path, format: ImageFormat): IngestGuard {
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
                        IngestGuard(dimensions = Dimension(width, height))
                    } finally {
                        reader.dispose()
                    }
                }
            }
        } catch (e: Exception) {
            CoreException.invalidFormat("image", e)
        }
    }
}
