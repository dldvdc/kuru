package com.project.kuru.store

import com.project.kuru.core.image.VerifiedImage
import com.project.kuru.flow.ImageStore
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest

@Component
class S3ImageStore(
    private val s3: S3Client,
    private val props: KuruStorageProperties,
) : ImageStore {

    override fun promote(stagingKey: String, finalKey: String, image: VerifiedImage) {
        s3.copyObject(
            CopyObjectRequest.builder()
                .sourceBucket(props.stagingBucket)
                .sourceKey(stagingKey)
                .destinationBucket(props.bucket)
                .destinationKey(finalKey)
                .contentType(image.mime)
                .build(),
        )
        s3.deleteObject(
            DeleteObjectRequest.builder()
                .bucket(props.stagingBucket)
                .key(stagingKey)
                .build(),
        )
    }
}
