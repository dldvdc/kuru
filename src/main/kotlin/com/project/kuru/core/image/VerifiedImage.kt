package com.project.kuru.core.image

/** Image validée côté domaine — sans ressource I/O. */
data class VerifiedImage(
    val originalFileName: String,
    val mime: String,
    val extension: String,
    val sizeBytes: Long,
    val contentSha256: ByteArray,
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
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VerifiedImage

        if (sizeBytes != other.sizeBytes) return false
        if (animated != other.animated) return false
        if (originalFileName != other.originalFileName) return false
        if (mime != other.mime) return false
        if (extension != other.extension) return false
        if (!contentSha256.contentEquals(other.contentSha256)) return false
        if (dimensions != other.dimensions) return false
        if (color != other.color) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sizeBytes.hashCode()
        result = 31 * result + animated.hashCode()
        result = 31 * result + originalFileName.hashCode()
        result = 31 * result + mime.hashCode()
        result = 31 * result + extension.hashCode()
        result = 31 * result + contentSha256.contentHashCode()
        result = 31 * result + dimensions.hashCode()
        result = 31 * result + color.hashCode()
        return result
    }
}
