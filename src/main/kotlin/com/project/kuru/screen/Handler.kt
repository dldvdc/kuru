package com.project.kuru.screen

import com.project.kuru.flow.UploadImage
import com.project.kuru.flow.UploadVideo
import com.project.kuru.screen.ingest.ImageIngestor
import com.project.kuru.screen.mapper.PartVideoMapper
import com.project.kuru.screen.multipart.UploadSpec
import com.project.kuru.screen.multipart.UploadedFilePart
import com.project.kuru.screen.multipart.parse
import com.project.kuru.screen.multipart.extractPart
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
    private val imageStager: ImageIngestor,
    private val partVideoMapper: PartVideoMapper,
    @Qualifier("imageUploadSpec") private val imageUploadSpec: UploadSpec,
    @Qualifier("videoUploadSpec") private val videoUploadSpec: UploadSpec,
) {

    fun test(req: ServerRequest): ServerResponse =
        ServerResponse.ok().build()

    fun uploadImage(req: ServerRequest): ServerResponse {

        val part = req.extractPart(imageUploadSpec.field)

        try {
            val upload = part.validatePreflight(imageUploadSpec)
            val staged = imageStager.stage(upload)

            uploadImage(UploadImage.Cmd(staged))
            log.info { "image upload accepted: ${staged.entry.originalFileName}" }

            return ServerResponse.status(HttpStatus.ACCEPTED).build()

        } finally {
            runCatching { part.delete() }
        }
    }

    fun uploadVideo(req: ServerRequest): ServerResponse {

        val part = req.extractPart(videoUploadSpec.field)

        try {
            part.parse(videoUploadSpec)
            return UploadedFilePart.from(part).use { uploaded ->
                uploadVideo(UploadVideo.Cmd(partVideoMapper.fromUpload(uploaded)))
                ServerResponse.status(HttpStatus.ACCEPTED).build()
            }

        } finally {
            runCatching { part.delete() }
        }
    }
}
