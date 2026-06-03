package com.project.kuru.screen.multipart

import com.project.kuru.core.CoreException
import com.project.kuru.core.FileName
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.Part

private val log = KotlinLogging.logger {}

/** Validations metadata sans ouvrir le stream. */
fun Part.runUploadPreflight(spec: UploadSpec) {
    log.debug {
        "preflight[$spec.field]: début (size=$size, type=$contentType, file=$submittedFileName)"
    }

    requireKnownSize(spec)
    requireContentTypeHint(spec)
    requireFileName(spec)

    log.debug { "preflight[${spec.field}]: metadata OK (size=$size)" }
}

/** Validations puis construction de [ValidatedUpload] (ouvre le stream). */
fun Part.validatePreflight(spec: UploadSpec): ValidatedUpload {
    runUploadPreflight(spec)

    val rawName = submittedFileName!!
    val validated = ValidatedUpload(
        fileName = FileName(rawName),
        content = inputStream,
        size = size,
        contentType = contentType,
    )

    log.debug {
        "preflight[${spec.field}]: prêt pour ingest (file=${validated.fileName.value}, size=${validated.size})"
    }
    return validated
}

private fun Part.requireKnownSize(spec: UploadSpec) {
    if (size < 0L) {
        log.warn { "preflight[${spec.field}]: taille inconnue (size=$size)" }
        throw CoreException.InvalidFormat(spec.field)
    }
    when {
        size == 0L -> {
            log.warn { "preflight[${spec.field}]: fichier vide" }
            throw CoreException.InvalidState(spec.field, "fichier vide")
        }
        size < spec.minSizeBytes -> {
            log.warn {
                "preflight[${spec.field}]: trop petit ($size < min ${spec.minSizeBytes})"
            }
            throw CoreException.InvalidFormat(spec.field)
        }
        size > spec.maxSizeBytes -> {
            log.warn {
                "preflight[${spec.field}]: trop grand ($size > max ${spec.maxSizeBytes})"
            }
            throw CoreException.InvalidFormat(spec.field)
        }
    }
}

private fun Part.requireFileName(spec: UploadSpec) {
    val raw = submittedFileName
    if (raw.isNullOrBlank()) {
        log.warn { "preflight[${spec.field}]: nom de fichier manquant" }
        throw CoreException.InvalidFormat(spec.field)
    }
    try {
        FileName(raw)
    } catch (e: CoreException) {
        log.warn(e) { "preflight[${spec.field}]: nom de fichier invalide '$raw'" }
        throw e
    }
}

private fun Part.requireContentTypeHint(spec: UploadSpec) {
    val type = contentType?.substringBefore(';')?.trim()?.lowercase() ?: run {
        log.debug { "preflight[${spec.field}]: Content-Type absent (hint ignoré)" }
        return
    }

    if (type.startsWith("text/") || type == "application/json") {
        log.warn { "preflight[${spec.field}]: Content-Type rejeté '$type'" }
        throw CoreException.InvalidFormat(spec.field)
    }

    val prefix = spec.allowedContentTypePrefix?.lowercase() ?: return
    if (!type.startsWith(prefix)) {
        log.warn {
            "preflight[${spec.field}]: Content-Type '$type' ne correspond pas au préfixe '$prefix'"
        }
        throw CoreException.InvalidFormat(spec.field)
    }
}
