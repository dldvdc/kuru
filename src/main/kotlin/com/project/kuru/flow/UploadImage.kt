package com.project.kuru.flow

import com.github.f4b6a3.tsid.TsidCreator
import com.project.kuru.core.toHexString
import com.project.kuru.flow.image.AcceptedImage
import com.project.kuru.reach.storage.StagingStore
import com.project.kuru.store.ObjectKeys
import com.project.kuru.store.UploadedObjectRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

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
        val shaHex = entry.contentSha256.toHexString()

        log.debug {
            "flow[upload-image]: début commit (stagingKey=${accepted.stagingKey}, sha=$shaHex, " +
                "file=${entry.originalFileName})"
        }

        val existingKey = uploadedObjects.findObjectKeyByContentSha256(entry.contentSha256)
        if (existingKey != null) {
            log.info {
                "flow[upload-image]: doublon SHA-256 → skip promote (existingKey=$existingKey, sha=$shaHex)"
            }
            stagingStore.delete(accepted.stagingKey)
            return
        }

        val objectKey = ObjectKeys.upload(TsidCreator.getTsid256().toString(), entry.extension)
        log.debug { "flow[upload-image]: promote staging → final key=$objectKey" }

        imageStore.promote(accepted.stagingKey, objectKey, entry)

        uploadedObjects.insert(
            contentSha256 = entry.contentSha256,
            objectKey = objectKey,
            sizeBytes = entry.sizeBytes,
            originalFilename = entry.originalFileName,
        )

        log.info {
            "flow[upload-image]: commit OK (objectKey=$objectKey, size=${entry.sizeBytes}, sha=$shaHex)"
        }
    }
}
