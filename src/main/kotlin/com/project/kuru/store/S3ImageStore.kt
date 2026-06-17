package com.project.kuru.store

import com.project.kuru.flow.ImageStore
import com.project.kuru.flow.image.CatalogEntry
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest

private val log = KotlinLogging.logger {}

@Component
class S3ImageStore(
    private val s3: S3Client,
    private val props: KuruStorageProperties,
) : ImageStore {

    override fun promote(stagingKey: String, finalKey: String, entry: CatalogEntry) {
        log.debug {
            "store[image]: COPY ${props.stagingBucket}/$stagingKey → ${props.bucket}/$finalKey " +
                "type=${entry.mime}"
        }
        s3.copyObject(
            CopyObjectRequest.builder()
                .sourceBucket(props.stagingBucket)
                .sourceKey(stagingKey)
                .destinationBucket(props.bucket)
                .destinationKey(finalKey)
                .contentType(entry.mime)
                .build(),
        )
        log.debug { "store[image]: COPY OK finalKey=$finalKey" }

        log.debug { "store[image]: DELETE staging key=$stagingKey" }
        s3.deleteObject(
            DeleteObjectRequest.builder()
                .bucket(props.stagingBucket)
                .key(stagingKey)
                .build(),
        )
        log.debug { "store[image]: promote terminé finalKey=$finalKey" }
    }
}
