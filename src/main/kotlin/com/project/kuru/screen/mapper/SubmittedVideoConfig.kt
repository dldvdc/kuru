package com.project.kuru.screen.mapper

import com.project.kuru.reach.file.video.FfprobeMetadataReader
import com.project.kuru.reach.mime.video.VideoFormat
import com.project.kuru.reach.mime.video.VideoMimeSniffer
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "spring.kuru.video")
data class KuruVideoProperties(
    val ffprobePath: String = "ffprobe",
    val maxSizeBytes: Long = 500L * 1024 * 1024,
    val maxDurationSeconds: Double = 300.0,
    val maxWidth: Int = 3840,
    val maxHeight: Int = 2160,
)

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KuruVideoProperties::class)
class SubmittedVideoConfig {

    @Bean
    fun submittedVideoConfigValues(props: KuruVideoProperties): SubmittedVideoConfigValues =
        SubmittedVideoConfigValues(
            minHeaderSize = VideoFormat.maxHeaderSize,
            maxSizeBytes = props.maxSizeBytes,
            bufferSize = 8 * 1024,
            tempPrefix = "submitted-video-",
            tempSuffix = ".bin",
        )

    @Bean
    fun ffprobeMetadataReader(props: KuruVideoProperties): FfprobeMetadataReader =
        FfprobeMetadataReader(
            ffprobePath = props.ffprobePath,
            maxDurationSeconds = props.maxDurationSeconds,
            maxWidth = props.maxWidth,
            maxHeight = props.maxHeight,
        )

    @Bean
    fun submittedVideoMapper(
        config: SubmittedVideoConfigValues,
        videoMimeSniffer: VideoMimeSniffer,
        ffprobeMetadataReader: FfprobeMetadataReader,
    ): PartVideoMapper =
        PartVideoMapper(config, videoMimeSniffer, ffprobeMetadataReader)
}

data class SubmittedVideoConfigValues(
    val minHeaderSize: Int,
    val maxSizeBytes: Long,
    val bufferSize: Int,
    val tempPrefix: String,
    val tempSuffix: String,
)
