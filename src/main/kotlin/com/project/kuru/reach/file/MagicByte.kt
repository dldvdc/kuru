package com.project.kuru.reach.file

import com.project.kuru.reach.mime.image.ImageFormat
import com.project.kuru.reach.mime.video.VideoFormat

object MagicByte {

    fun sniffImage(header: ByteArray): ImageFormat? =
        ImageFormat.all
            .filter { header.size >= it.signature.minHeaderSize }
            .firstOrNull { it.matches(header) }

    fun sniffVideo(header: ByteArray): VideoFormat? =
        VideoFormat.all
            .filter { header.size >= it.signature.minHeaderSize }
            .firstOrNull { it.matches(header) }
}