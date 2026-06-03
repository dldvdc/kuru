package com.project.kuru.screen.multipart

import com.project.kuru.reach.ingest.KuruImageProperties
import com.project.kuru.reach.mime.image.ImageFormat
import com.project.kuru.reach.mime.video.VideoFormat
import com.project.kuru.screen.mapper.KuruVideoProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KuruImageProperties::class, KuruVideoProperties::class)
class UploadSpecConfig {

    @Bean
    fun imageUploadSpec(props: KuruImageProperties): UploadSpec =
        UploadSpec(
            field = "image",
            maxSizeBytes = props.maxSizeBytes,
            minSizeBytes = ImageFormat.maxHeaderSize,
            allowedContentTypePrefix = "image/",
        )

    @Bean
    fun videoUploadSpec(props: KuruVideoProperties): UploadSpec =
        UploadSpec(
            field = "video",
            maxSizeBytes = props.maxSizeBytes,
            minSizeBytes = VideoFormat.maxHeaderSize,
            allowedContentTypePrefix = "video/",
        )
}
