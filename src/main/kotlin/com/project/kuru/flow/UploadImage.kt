package com.project.kuru.flow

import com.github.f4b6a3.ulid.UlidCreator
import com.project.kuru.flow.image.AcceptedImage
import com.project.kuru.flow.image.toVerifiedImage
import com.project.kuru.reach.storage.StagingStore
import com.project.kuru.store.ObjectKeys
import com.project.kuru.store.UploadedObjectRepository
import org.springframework.stereotype.Service

@Service
class UploadImage(
    private val imageStore: ImageStore,
    private val stagingStore: StagingStore,
    private val uploadedObjects: UploadedObjectRepository,
) {

    data class Cmd(
        val image: AcceptedImage,
    )

    operator fun invoke(cmd: Cmd) {
        val accepted = cmd.image
        if (uploadedObjects.findObjectKeyByContentSha256(accepted.contentSha256) != null) {
            stagingStore.delete(accepted.stagingKey)
            return
        }

        val verified = accepted.toVerifiedImage()
        val objectKey = ObjectKeys.upload(UlidCreator.getUlid().toString(), verified.extension)
        imageStore.promote(accepted.stagingKey, objectKey, verified)
        uploadedObjects.insert(
            contentSha256 = verified.contentSha256,
            objectKey = objectKey,
            sizeBytes = verified.sizeBytes,
            originalFilename = verified.originalFileName,
        )
        Thread.sleep(500)
    }
}
