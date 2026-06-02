package com.project.kuru.screen

import com.project.kuru.flow.UploadImage
import com.project.kuru.flow.UploadVideo
import com.project.kuru.screen.ingest.ImageIngestor
import com.project.kuru.screen.mapper.PartVideoMapper
import com.project.kuru.screen.multipart.Multipart
import com.project.kuru.screen.multipart.UploadPartPreflight
import com.project.kuru.screen.multipart.UploadedFilePart
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
    private val imageUploadPartPreflightSpec: UploadPartPreflight.Spec,
    private val videoUploadPartPreflightSpec: UploadPartPreflight.Spec,
) {

    fun test(req: ServerRequest): ServerResponse =
        ServerResponse.ok().build()

    fun uploadImage(req: ServerRequest): ServerResponse {
        val part = Multipart.select(req, "image")
        try {
            val validated = UploadPartPreflight.validate(part, imageUploadPartPreflightSpec)
            val accepted = imageIngestor.accept(validated)
            uploadImage(UploadImage.Cmd(accepted))
            return ServerResponse.status(HttpStatus.ACCEPTED).build()
        } finally {
            runCatching { part.delete() }
        }
    }

    fun uploadVideo(req: ServerRequest): ServerResponse {
        val part = Multipart.select(req, "video")
        UploadPartPreflight.validate(part, videoUploadPartPreflightSpec)
        return UploadedFilePart.from(part).use { uploaded ->
            uploadVideo(UploadVideo.Cmd(partVideoMapper.fromUpload(uploaded)))
            ServerResponse.status(HttpStatus.ACCEPTED).build()
        }
    }
}
