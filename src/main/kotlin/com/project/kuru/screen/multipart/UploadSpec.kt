package com.project.kuru.screen.multipart

data class UploadSpec(
    val field: String,
    val maxSizeBytes: Long,
    val minSizeBytes: Int,
    val allowedContentTypePrefix: String? = null,
)
