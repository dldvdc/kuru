package com.project.kuru.store

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("spring.kuru.storage")
data class KuruStorageProperties(
    val endpoint: String,
    val accessKey: String,
    val secretKey: String,
    val pathStyleAccess: Boolean = true,
    val bucket: String,
    val stagingBucket: String,
)
