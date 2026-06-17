package com.project.kuru.reach.file.image

import com.project.kuru.core.CoreException
import com.project.kuru.core.Dimension
import com.project.kuru.reach.mime.image.ImageFormat
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO

private val log = KotlinLogging.logger {}

class ImageIoMetadataReader {

    fun read(tempFile: Path, format: ImageFormat): IngestGuard {
        val mime = format.mime
        log.debug { "ingest[image-io]: lecture dimensions mime=$mime" }
        return try {
            Files.newInputStream(tempFile).use { input ->
                ImageIO.createImageInputStream(input).use { iis ->
                    if (iis == null) {
                        log.warn { "ingest[image-io]: ImageInputStream null" }
                        throw CoreException.InvalidFormat("image")
                    }
                    val reader = ImageIO.getImageReadersByMIMEType(mime).asSequence().firstOrNull()
                        ?: run {
                            log.warn { "ingest[image-io]: pas de reader pour mime=$mime" }
                            throw CoreException.InvalidFormat("image")
                        }

                    try {
                        reader.setInput(iis, true)
                        val width = reader.getWidth(0)
                        val height = reader.getHeight(0)
                        log.debug { "ingest[image-io]: ${width}x$height" }
                        IngestGuard(dimensions = Dimension(width, height))
                    } finally {
                        reader.dispose()
                    }
                }
            }
        } catch (e: Exception) {
            log.warn(e) { "ingest[image-io]: échec lecture metadata" }
            CoreException.invalidFormat("image", e)
        }
    }
}
