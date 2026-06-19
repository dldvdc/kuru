package com.project.kuru.reach.ingest

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.kuru.image")
data class KuruImageProperties(
    val maxSizeBytes: Long = 100L * 1024 * 1024,
    val maxMemoryBytes: Long = 512L * 1024 * 1024,
    val bytesPerPixelEstimate: Long = 4L,
)
