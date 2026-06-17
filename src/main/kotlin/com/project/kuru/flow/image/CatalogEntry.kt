package com.project.kuru.flow.image

/** Métadonnées persistées (catalogue + S3) — sans clé staging ni détails d’ingest. */
data class CatalogEntry(
    val originalFileName: String,
    val mime: String,
    val extension: String,
    val sizeBytes: Long,
    val contentSha256: ByteArray,
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
        other as CatalogEntry
        if (sizeBytes != other.sizeBytes) return false
        if (originalFileName != other.originalFileName) return false
        if (mime != other.mime) return false
        if (extension != other.extension) return false
        if (!contentSha256.contentEquals(other.contentSha256)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = sizeBytes.hashCode()
        result = 31 * result + originalFileName.hashCode()
        result = 31 * result + mime.hashCode()
        result = 31 * result + extension.hashCode()
        result = 31 * result + contentSha256.contentHashCode()
        return result
    }
}
