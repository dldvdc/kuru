package com.project.kuru.store

import com.project.kuru.flow.video.SubmittedVideo
import com.project.kuru.flow.VideoStore
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@Component
class S3VideoStore(
    private val s3: S3Client,
    private val props: KuruStorageProperties,
) : VideoStore {

    override fun store(video: SubmittedVideo, objectKey: String) {
        s3.putObject(
            PutObjectRequest.builder()
                .bucket(props.bucket)
                .key(objectKey)
                .contentType(video.mime)
                .contentLength(video.size)
                .build(),
            RequestBody.fromFile(video.tempFile),
        )
    }
}
