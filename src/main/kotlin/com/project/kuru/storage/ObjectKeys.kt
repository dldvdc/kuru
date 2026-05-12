package com.project.kuru.storage

import com.project.kuru.core.image.ImageFormat

object ObjectKeys {

    fun upload(ulid: String, format: ImageFormat): String =
        "uploads/$ulid.${extension(format)}"

    fun extension(format: ImageFormat): String =
        when (format) {
            ImageFormat.JPEG -> "jpg"
            ImageFormat.PNG -> "png"
            ImageFormat.GIF -> "gif"
            ImageFormat.TIFF -> "tif"
            ImageFormat.WEBP -> "webp"
        }
}
