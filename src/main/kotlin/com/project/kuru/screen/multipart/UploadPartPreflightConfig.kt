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
class UploadPartPreflightConfig {

    @Bean
    fun imageUploadPartPreflightSpec(props: KuruImageProperties): UploadPartPreflight.Spec =
        UploadPartPreflight.Spec(
            field = "image",
            maxSizeBytes = props.maxSizeBytes,
            minSizeBytes = ImageFormat.maxHeaderSize,
        )

    @Bean
    fun videoUploadPartPreflightSpec(props: KuruVideoProperties): UploadPartPreflight.Spec =
        UploadPartPreflight.Spec(
            field = "video",
            maxSizeBytes = props.maxSizeBytes,
            minSizeBytes = VideoFormat.maxHeaderSize,
        )
}
