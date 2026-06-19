package com.project.kuru.flow

import com.github.f4b6a3.ulid.UlidCreator
import com.project.kuru.flow.video.SubmittedVideo
import com.project.kuru.reach.hash.Sha256Hasher
import com.project.kuru.store.ObjectKeys
import com.project.kuru.store.UploadedObjectRepository
import com.project.kuru.store.VideoStore
import org.springframework.stereotype.Service

@Service
class UploadVideo(
    private val videoStore: VideoStore,
    private val uploadedObjects: UploadedObjectRepository,
) {

    data class Cmd(
        val video: SubmittedVideo,
    )

    operator fun invoke(cmd: Cmd) {
        cmd.video.use { raw ->
            val contentSha = Sha256Hasher.hash(raw.tempFile)
            if (uploadedObjects.findObjectKeyByContentSha256(contentSha) != null) {
                return@use
            }

            val objectKey = ObjectKeys.upload(UlidCreator.getUlid().toString(), raw.extension)
            videoStore.store(raw, objectKey)
            uploadedObjects.insert(
                contentSha256 = contentSha,
                objectKey = objectKey,
                sizeBytes = raw.size,
                originalFilename = raw.originalFileName,
            )
        }
    }
}
