package com.project.kuru.core.image

enum class ImageFormat {
    JPEG, PNG, GIF, TIFF, WEBP;
    companion object {
        fun fromMimeType(mime: String): ImageFormat = when (mime) {
            "image/jpeg" -> JPEG
            "image/png" -> PNG
            "image/gif" -> GIF
            "image/webp" -> WEBP
            "image/tiff" -> TIFF
            else -> throw IllegalArgumentException("image")
        }
    }
}
