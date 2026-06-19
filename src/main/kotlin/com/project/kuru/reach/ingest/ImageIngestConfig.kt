package com.project.kuru.reach.ingest

import com.project.kuru.reach.file.ImageMimeSniffer
import com.project.kuru.reach.file.image.ImageIoMetadataReader
import com.project.kuru.reach.mime.image.ImageFormat
import com.project.kuru.store.StagingStore
import com.project.kuru.screen.ingest.ImageIngestor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class ImageIngestConfig {

    @Bean
    fun imageIngestor(
        props: KuruImageProperties,
        imageMimeSniffer: ImageMimeSniffer,
        imageIoMetadataReader: ImageIoMetadataReader,
        stagingStore: StagingStore,
    ): ImageIngestor =
        FileIngestor(
            props = props,
            minHeaderSize = ImageFormat.maxHeaderSize,
            imageMimeSniffer = imageMimeSniffer,
            imageIoMetadataReader = imageIoMetadataReader,
            stagingStore = stagingStore,
        )
}
