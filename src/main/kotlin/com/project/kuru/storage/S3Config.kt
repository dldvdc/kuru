package com.project.kuru.storage

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import java.net.URI

@Configuration
@EnableConfigurationProperties(KuruStorageProperties::class)
class S3Config {

    @Bean
    fun s3Client(props: KuruStorageProperties): S3Client =
        S3Client.builder()
            .endpointOverride(URI.create(props.endpoint))
            .region(Region.US_EAST_1)
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(props.accessKey, props.secretKey),
                ),
            )
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(props.pathStyleAccess)
                    .build(),
            )
            .build()
}
