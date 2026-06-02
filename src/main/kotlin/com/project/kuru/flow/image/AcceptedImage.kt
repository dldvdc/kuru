package com.project.kuru.flow.image

import com.project.kuru.core.image.ColorProfile
import com.project.kuru.core.image.Dimension

/** Image ingérée côté reach — prête pour commit métier (dedup, promote, catalogue). */
data class AcceptedImage(
    val originalFileName: String,
    val mime: String,
    val extension: String,
    val sizeBytes: Long,
    val contentSha256: ByteArray,
    val stagingKey: String,
    val dimensions: Dimension,
    val color: ColorProfile,
    val animated: Boolean,
) {
    init {
        require(originalFileName.isNotBlank()) { "Nom de fichier original manquant" }
        require(mime.isNotBlank()) { "MIME manquant" }
        require(extension.isNotBlank()) { "Extension manquante" }
        require(sizeBytes > 0) { "Taille invalide" }
        require(contentSha256.isNotEmpty()) { "Empreinte SHA-256 manquante" }
        require(stagingKey.isNotBlank()) { "Clé staging manquante" }
    }
}
