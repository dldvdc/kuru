package com.project.kuru.reach.mime.image

fun interface ImageMimeSniffer {
    fun sniffImage(header: ByteArray): ImageFormat?
}
