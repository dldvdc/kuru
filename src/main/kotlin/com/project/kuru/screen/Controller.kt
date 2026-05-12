package com.project.kuru.screen

import com.project.kuru.core.CoreException
import com.project.kuru.flow.UploadImage
import com.project.kuru.screen.mapper.PartImageMapper
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Component
class Controller(
    private val uploadImage: UploadImage,
    private val partImageMapper: PartImageMapper,
) {

    fun test(req: ServerRequest): ServerResponse {
        return ServerResponse.ok().build()
    }

    fun uploadImage(req: ServerRequest): ServerResponse {
        val imageParts = req.servletRequest().parts.filter { it.name == PART_IMAGE }.toList()

        if (imageParts.isEmpty()) {
            return badRequest("Partie multipart '$PART_IMAGE' manquante")
        }
        if (imageParts.size > 1) {
            return badRequest("Une seule partie '$PART_IMAGE' est autorisée par requête")
        }

        val raw = try {
            partImageMapper.fromPart(imageParts.single())
        } catch (e: CoreException) {
            return badRequest(e.message ?: "Image invalide ou refusée")
        }

        uploadImage(UploadImage.Cmd(image = raw))

        return ServerResponse.ok().build()
    }

    private fun badRequest(message: String): ServerResponse =
        ServerResponse.badRequest().body(mapOf("error" to message))

    companion object {
        const val PART_IMAGE = "image"
    }
}
