package com.project.kuru.store

import com.project.kuru.reach.storage.StagingStore
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.nio.file.Path

private val log = KotlinLogging.logger {}

@Component
class S3StagingStore(
    private val s3: S3Client,
    private val props: KuruStorageProperties,
) : StagingStore {

    override fun put(key: String, source: Path, contentType: String, contentLength: Long) {
        log.debug {
            "store[staging]: PUT bucket=${props.stagingBucket} key=$key " +
                "type=$contentType length=$contentLength"
        }
        s3.putObject(
            PutObjectRequest.builder()
                .bucket(props.stagingBucket)
                .key(key)
                .contentType(contentType)
                .contentLength(contentLength)
                .build(),
            RequestBody.fromFile(source),
        )
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
}
