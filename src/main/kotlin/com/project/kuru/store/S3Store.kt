package com.project.kuru.store

import com.project.kuru.flow.image.CatalogEntry
import com.project.kuru.flow.video.SubmittedVideo
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.InputStream

private val log = KotlinLogging.logger {}

@Component
class S3Store(
    private val s3: S3Client,
    private val props: KuruStorageProperties,
) : StagingStore, ImageStore, VideoStore {

    override fun put(key: String, source: InputStream, contentType: String, contentLength: Long) {
        log.debug {
            "store[staging]: PUT bucket=${props.stagingBucket} key=$key " +
                "type=$contentType length=$contentLength"
        }
        source.use {
            s3.putObject(
                PutObjectRequest.builder()
                    .bucket(props.stagingBucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build(),
                RequestBody.fromInputStream(it, contentLength),
            )
        }
        log.debug { "store[staging]: PUT OK key=$key" }
    }

    override fun delete(key: String) {
        log.debug { "store[staging]: DELETE bucket=${props.stagingBucket} key=$key" }
        s3.deleteObject(
            DeleteObjectRequest.builder()
                .bucket(props.stagingBucket)
                .key(key)
                .build(),
        )
        log.debug { "store[staging]: DELETE OK key=$key" }
    }

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
        delete(stagingKey)
        log.debug { "store[image]: promote terminé finalKey=$finalKey" }
    }

    override fun store(video: SubmittedVideo, objectKey: String) {
        log.debug {
            "store[video]: PUT bucket=${props.bucket} key=$objectKey " +
                "type=${video.mime} length=${video.size}"
        }
        s3.putObject(
            PutObjectRequest.builder()
                .bucket(props.bucket)
                .key(objectKey)
                .contentType(video.mime)
                .contentLength(video.size)
                .build(),
            RequestBody.fromFile(video.tempFile),
        )
        log.debug { "store[video]: PUT OK key=$objectKey" }
    }
}
