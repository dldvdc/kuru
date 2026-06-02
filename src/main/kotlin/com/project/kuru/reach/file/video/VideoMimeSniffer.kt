package com.project.kuru.reach.mime.video

fun interface VideoMimeSniffer {
    fun sniffVideo(header: ByteArray): VideoFormat?
}
