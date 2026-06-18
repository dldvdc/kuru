package com.project.kuru.reach.ingest

import com.project.kuru.reach.file.image.ImageIoMetadataReader
import com.project.kuru.reach.mime.image.ImageFormat
import com.project.kuru.reach.file.ImageMimeSniffer
import com.project.kuru.reach.storage.StagingStore
import com.project.kuru.screen.ingest.ImageIngestor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KuruImageProperties::class)
class ReachImageIngestConfig {

    @Bean
    fun imageIngestor(
        props: KuruImageProperties,
        imageMimeSniffer: ImageMimeSniffer,
        imageIoMetadataReader: ImageIoMetadataReader,
        stagingStore: StagingStore,
    ): ImageIngestor =
        ReachImageIngestor(
            props = props,
            minHeaderSize = ImageFormat.maxHeaderSize,
            imageMimeSniffer = imageMimeSniffer,
            imageIoMetadataReader = imageIoMetadataReader,
            stagingStore = stagingStore,
        )
}
