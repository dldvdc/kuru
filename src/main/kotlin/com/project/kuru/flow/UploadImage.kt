package com.project.kuru.flow

import com.github.f4b6a3.ulid.UlidCreator
import com.project.kuru.core.hash.Sha256Hasher
import com.project.kuru.core.image.RawImageFile
import com.project.kuru.storage.ObjectKeys
import com.project.kuru.storage.UploadedObjectRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class UploadImage (
    private val imageStore: ImageStore,
    private val uploadedObjects: UploadedObjectRepository,
) {

    private val logger = KotlinLogging.logger {}

    data class Cmd(
        val image: RawImageFile,
    )

    operator fun invoke(cmd: Cmd) {
        cmd.image.use { raw ->
            val contentSha = Sha256Hasher.hash(raw.tempFile)
            val existingKey = uploadedObjects.findObjectKeyByContentSha256(contentSha)
            if (existingKey != null) {
                logger.info {
                    "upload dedup: même contenu (SHA-256) déjà stocké sous objectKey=$existingKey " +
                        "fichier=${raw.originalFileName}"
                }
                return@use
            }

            val ulid = UlidCreator.getUlid().toString()
            val objectKey = ObjectKeys.upload(ulid, raw.metadata.format)
            logger.debug { "upload ulid=$ulid objectKey=$objectKey file=${raw.originalFileName}" }
            imageStore.store(raw, objectKey)
            uploadedObjects.insert(
                contentSha256 = contentSha,
                objectKey = objectKey,
                sizeBytes = raw.size,
                originalFilename = raw.originalFileName,
            )
        }
    }
}
