package com.project.kuru.store

import com.project.kuru.reach.storage.StagingStore
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.nio.file.Path

@Component
class S3StagingStore(
    private val s3: S3Client,
    private val props: KuruStorageProperties,
) : StagingStore {

    override fun put(key: String, source: Path, contentType: String, contentLength: Long) {
        s3.putObject(
            PutObjectRequest.builder()
                .bucket(props.stagingBucket)
                .key(key)
                .contentType(contentType)
                .contentLength(contentLength)
                .build(),
            RequestBody.fromFile(source),
        )
    }

    override fun delete(key: String) {
        s3.deleteObject(
            DeleteObjectRequest.builder()
                .bucket(props.stagingBucket)
                .key(key)
                .build(),
        )
    }
}
