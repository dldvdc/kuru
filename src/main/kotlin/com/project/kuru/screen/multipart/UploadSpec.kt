package com.project.kuru.screen.multipart

/** Limites et règles fail-fast pour une part multipart (metadata uniquement). */
data class UploadSpec(
    val field: String,
    val maxSizeBytes: Long,
    val minSizeBytes: Int,
    /** Ex. `image/` ou `video/` — hint optionnel si le client envoie un Content-Type. */
    val allowedContentTypePrefix: String? = null,
)
