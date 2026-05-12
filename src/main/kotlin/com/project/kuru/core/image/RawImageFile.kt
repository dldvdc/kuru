package com.project.kuru.core.image

import java.io.Closeable
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

data class RawImageFile(
    val originalFileName: String,
    val mimeType: MimeType,
    val size: Long,
    val tempFile: Path,
    val metadata: Metadata
) : Closeable {

    init {
        require(size > 0) { "Contenu vide" }
        require(Files.exists(tempFile)) { "Fichier temporaire introuvable" }
    }

    fun openStream(): InputStream = Files.newInputStream(tempFile)

    override fun close() {
        Files.deleteIfExists(tempFile)
    }

    data class Metadata(
        val format: ImageFormat,
        val dimensions: Dimension,
        val color: ColorProfile,
        val animated: Boolean
    ) {
        data class Dimension(val width: Int, val height: Int) {
            init {
                require(width > 0 && height > 0) { "Dimensions invalides: ${width}x${height}" }
            }

            val totalPixels: Long get() = width.toLong() * height
        }

        data class ColorProfile(
            val depth: Int,
            val model: ColorSpace,
            val hasAlpha: Boolean
        ) {
            init {
                require(depth in 1..64) { "Profondeur de couleur invalide: $depth" }
            }
        }
    }

    @JvmInline
    value class MimeType(val value: String) {
        init {
            require(value.startsWith("image/")) { "Seuls les formats image sont acceptes" }
        }
    }
}