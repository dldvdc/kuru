package com.project.kuru.flow

import com.github.f4b6a3.ulid.UlidCreator
import com.project.kuru.flow.image.AcceptedImage
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
        val entry = accepted.entry
        if (uploadedObjects.findObjectKeyByContentSha256(entry.contentSha256) != null) {
            stagingStore.delete(accepted.stagingKey)
            return
        }

        val objectKey = ObjectKeys.upload(UlidCreator.getUlid().toString(), entry.extension)
        imageStore.promote(accepted.stagingKey, objectKey, entry)
        uploadedObjects.insert(
            contentSha256 = entry.contentSha256,
            objectKey = objectKey,
            sizeBytes = entry.sizeBytes,
            originalFilename = entry.originalFileName,
        )
    }
}
