package com.project.kuru.reach

import com.project.kuru.reach.file.MagicByte
import com.project.kuru.reach.file.image.ImageIoMetadataReader
import com.project.kuru.reach.mime.image.ImageMimeSniffer
import com.project.kuru.reach.mime.video.VideoMimeSniffer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Reach {

    @Bean
    fun imageMimeSniffer(): ImageMimeSniffer = ImageMimeSniffer(MagicByte::sniffImage)

    @Bean
    fun imageIoMetadataReader(): ImageIoMetadataReader = ImageIoMetadataReader()

    @Bean
    fun videoMimeSniffer(): VideoMimeSniffer = VideoMimeSniffer(MagicByte::sniffVideo)
}
