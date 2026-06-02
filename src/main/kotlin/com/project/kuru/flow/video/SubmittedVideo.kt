package com.project.kuru.flow.video

import java.io.Closeable
import java.nio.file.Files
import java.nio.file.Path

/** Média vidéo vérifié en entrée du use case — ressource temporaire incluse. */
class SubmittedVideo private constructor(
    val originalFileName: String,
    val mime: String,
    val extension: String,
    val size: Long,
    val tempFile: Path,
    val dimensions: Dimension,
    val durationSeconds: Double,
    val videoCodec: String,
) : Closeable {

    data class Dimension(val width: Int, val height: Int) {
        init {
            require(width > 0 && height > 0) { "Dimensions invalides: ${width}x${height}" }
        }
    }

    data class Metadata(
        val dimensions: Dimension,
        val durationSeconds: Double,
        val videoCodec: String,
    ) {
        init {
            require(durationSeconds > 0) { "Durée invalide: $durationSeconds" }
            require(videoCodec.isNotBlank()) { "Codec vidéo manquant" }
        }
    }

    companion object {
        operator fun invoke(
            fileName: String,
            mime: String,
            extension: String,
            tempFile: Path,
            metadata: Metadata,
        ): SubmittedVideo {
            require(Files.exists(tempFile)) { "Fichier temporaire introuvable" }
            val size = Files.size(tempFile)
            require(size > 0) { "Contenu vide" }
            return SubmittedVideo(
                originalFileName = fileName,
                mime = mime,
                extension = extension,
                size = size,
                tempFile = tempFile,
                dimensions = metadata.dimensions,
                durationSeconds = metadata.durationSeconds,
                videoCodec = metadata.videoCodec,
            )
        }
    }

    override fun close() {
        Files.deleteIfExists(tempFile)
    }
}
