package com.project.kuru.screen

import com.project.kuru.flow.UploadImage
import com.project.kuru.flow.UploadVideo
import com.project.kuru.screen.ingest.ImageIngestor
import com.project.kuru.screen.mapper.PartVideoMapper
import com.project.kuru.screen.multipart.UploadSpec
import com.project.kuru.screen.multipart.UploadedFilePart
import com.project.kuru.screen.multipart.runUploadPreflight
import com.project.kuru.screen.multipart.selectMultipartPart
import com.project.kuru.screen.multipart.validatePreflight
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

private val log = KotlinLogging.logger {}

@Component
class Handler(
    private val uploadImage: UploadImage,
    private val uploadVideo: UploadVideo,
    private val imageIngestor: ImageIngestor,
    private val partVideoMapper: PartVideoMapper,
    @Qualifier("imageUploadSpec") private val imageUploadSpec: UploadSpec,
    @Qualifier("videoUploadSpec") private val videoUploadSpec: UploadSpec,
) {

    fun test(req: ServerRequest): ServerResponse =
        ServerResponse.ok().build()

    fun uploadImage(req: ServerRequest): ServerResponse {
        log.debug { "handler[image]: début POST /uploads/image" }
        val part = req.selectMultipartPart(imageUploadSpec.field)
        try {
            val validated = part.validatePreflight(imageUploadSpec)
            log.debug { "handler[image]: preflight OK → ingest (file=${validated.fileName.value})" }

            val accepted = imageIngestor.accept(validated)
            log.debug {
                "handler[image]: ingest OK → commit (stagingKey=${accepted.stagingKey}, " +
                    "mime=${accepted.entry.mime}, size=${accepted.entry.sizeBytes})"
            }

            uploadImage(UploadImage.Cmd(accepted))
            log.info { "handler[image]: 202 Accepted (file=${accepted.entry.originalFileName})" }
            return ServerResponse.status(HttpStatus.ACCEPTED).build()
        } catch (e: Exception) {
            log.warn(e) { "handler[image]: échec upload" }
            throw e
        } finally {
            runCatching { part.delete() }
                .onFailure { e -> log.debug(e) { "handler[image]: suppression part multipart" } }
        }
    }

    fun uploadVideo(req: ServerRequest): ServerResponse {
        val part = req.selectMultipartPart(videoUploadSpec.field)
        try {
            part.runUploadPreflight(videoUploadSpec)
            return UploadedFilePart.from(part).use { uploaded ->
                uploadVideo(UploadVideo.Cmd(partVideoMapper.fromUpload(uploaded)))
                ServerResponse.status(HttpStatus.ACCEPTED).build()
            }
        } finally {
            runCatching { part.delete() }
        }
    }
}
