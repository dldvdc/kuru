package com.project.kuru.storage

import com.project.kuru.core.image.RawImageFile
import com.project.kuru.flow.ImageStore
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@Component
class S3ImageStore(
    private val s3: S3Client,
    private val props: KuruStorageProperties,
) : ImageStore {

    override fun store(image: RawImageFile, objectKey: String) {
        image.openStream().use { stream ->
            s3.putObject(
                PutObjectRequest.builder()
                    .bucket(props.bucket)
                    .key(objectKey)
                    .contentType(image.mimeType.value)
                    .contentLength(image.size)
                    .build(),
                RequestBody.fromInputStream(stream, image.size),
            )
        }
    }
}
