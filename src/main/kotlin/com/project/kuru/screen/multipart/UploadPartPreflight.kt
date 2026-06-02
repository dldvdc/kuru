package com.project.kuru.screen.multipart

import com.project.kuru.core.CoreException
import com.project.kuru.core.FileName
import jakarta.servlet.http.Part
object UploadPartPreflight {

    data class Spec(
        val field: String,
        val maxSizeBytes: Long,
        val minSizeBytes: Int,
    )

    fun validate(part: Part, spec: Spec): ValidatedUpload {
        part.requireKnownSize(spec)
        part.requireContentTypeHint(spec)
        val rawName = part.submittedFileName
            ?: throw CoreException.InvalidFormat(spec.field)

        return ValidatedUpload(
            fileName = FileName(rawName),
            content = part.inputStream,
            size = part.size,
            contentType = part.contentType
        )
    }

    private fun Part.requireKnownSize(spec: Spec) {
        val bytes = size
        if (bytes < 0L) throw CoreException.InvalidFormat(spec.field)
        when {
            bytes == 0L -> throw CoreException.InvalidState(spec.field, "fichier vide")
            bytes < spec.minSizeBytes -> throw CoreException.InvalidFormat(spec.field)
            bytes > spec.maxSizeBytes -> throw CoreException.InvalidFormat(spec.field)
        }
    }

    private fun Part.requireContentTypeHint(spec: Spec) {
        val type = contentType?.substringBefore(';')?.trim()?.lowercase() ?: return
        if (type.startsWith("text/") || type == "application/json") {
            throw CoreException.InvalidFormat(spec.field)
        }
    }
}
