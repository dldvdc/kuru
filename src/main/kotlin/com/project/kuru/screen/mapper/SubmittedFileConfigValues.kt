package com.project.kuru.screen.mapper

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration(proxyBeanMethods = false)
class SubmittedFileConfig {

    @Bean
    fun submittedFileConfigValues(): SubmittedFileConfigValues =
        SubmittedFileConfigValues(
            minHeaderSize = 12,
            maxSizeBytes = 10L * 1024 * 1024,
            maxMemoryBytes = 200L * 1024 * 1024,
            bytesPerPixelEstimate = 4L,
            bufferSize = 8 * 1024,
            tempPrefix = "submitted-image-",
            tempSuffix = ".bin"
        )

    @Bean
    fun submittedImageMapper(config: SubmittedFileConfigValues): PartImageMapper =
        PartImageMapper(config)
}

data class SubmittedFileConfigValues(
    val minHeaderSize: Int,
    val maxSizeBytes: Long,
    val maxMemoryBytes: Long,
    val bytesPerPixelEstimate: Long,
    val bufferSize: Int,
    val tempPrefix: String,
    val tempSuffix: String
)
