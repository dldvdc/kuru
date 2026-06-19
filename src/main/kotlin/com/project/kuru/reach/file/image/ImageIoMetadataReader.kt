package com.project.kuru.reach.file.image

import com.project.kuru.core.CoreException
import com.project.kuru.core.Dimension
import com.project.kuru.reach.mime.image.ImageFormat
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.InputStream
import javax.imageio.ImageIO

private val log = KotlinLogging.logger {}

class ImageIoMetadataReader {

    fun read(stream: InputStream, format: ImageFormat): IngestGuard =
        try {
            ImageIO.createImageInputStream(stream).use { iis ->
                if (iis == null) throw CoreException.InvalidFormat("image")
                val reader = ImageIO.getImageReadersByMIMEType(format.mime).asSequence().firstOrNull()
                    ?: throw CoreException.InvalidFormat("image")
                try {
                    reader.setInput(iis, true)
                    IngestGuard(dimensions = Dimension(reader.getWidth(0), reader.getHeight(0)))
                } finally {
                    reader.dispose()
                }
            }
        } catch (e: Exception) {
            log.debug(e) { "ingest[image-io]: échec lecture dimensions mime=${format.mime}" }
            CoreException.invalidFormat("image", e)
        }
}
