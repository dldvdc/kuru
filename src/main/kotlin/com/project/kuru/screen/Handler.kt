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
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

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
        val part = req.selectMultipartPart(imageUploadSpec.field)
        try {
            val validated = part.validatePreflight(imageUploadSpec)
            val accepted = imageIngestor.accept(validated)
            uploadImage(UploadImage.Cmd(accepted))
            return ServerResponse.status(HttpStatus.ACCEPTED).build()
        } finally {
            runCatching { part.delete() }
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
