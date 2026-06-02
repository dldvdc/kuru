package com.project.kuru.reach.ingest

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.kuru.image")
data class KuruImageProperties(
    val maxSizeBytes: Long = 50L * 1024 * 1024,
    val maxMemoryBytes: Long = 512L * 1024 * 1024,
    val bytesPerPixelEstimate: Long = 4L,
    val bufferSize: Int = 8 * 1024,
    val tempPrefix: String = "submitted-image-",
    val tempSuffix: String = ".bin",
)
