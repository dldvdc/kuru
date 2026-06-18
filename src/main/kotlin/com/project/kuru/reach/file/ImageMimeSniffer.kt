package com.project.kuru.reach.file

import com.project.kuru.reach.mime.image.ImageFormat

fun interface ImageMimeSniffer {
    fun sniffImage(header: ByteArray): ImageFormat?
}
