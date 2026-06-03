package com.project.kuru.screen.multipart

import com.project.kuru.core.CoreException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.Part
import org.springframework.web.servlet.function.ServerRequest

private val log = KotlinLogging.logger {}

fun ServerRequest.selectMultipartPart(partName: String): Part {
    log.debug { "multipart: lecture des parts, champ attendu='$partName'" }

    val allParts = runCatching { servletRequest().parts.toList() }
        .getOrElse { e ->
            log.warn(e) { "multipart: requête invalide pour champ='$partName'" }
            throw CoreException.InvalidState(partName, "requête multipart invalide")
        }

    log.debug {
        val summary = allParts.joinToString { "'${it.name}' (${it.size} o)" }
        "multipart: ${allParts.size} part(s) reçue(s): $summary"
    }

    val matched = allParts.filter { it.name == partName }

    fun cleanUpExcept(chosen: Part?) {
        val discarded = allParts.filter { it != chosen }
        if (discarded.isNotEmpty()) {
            log.debug { "multipart: suppression de ${discarded.size} part(s) non retenue(s)" }
        }
        discarded.forEach { runCatching { it.delete() } }
    }

    return when (matched.size) {
        1 -> {
            val chosenPart = matched.single()
            cleanUpExcept(chosen = chosenPart)
            log.debug {
                "multipart: part '$partName' retenue (size=${chosenPart.size}, " +
                    "type=${chosenPart.contentType}, file=${chosenPart.submittedFileName})"
            }
            chosenPart
        }
        0 -> {
            cleanUpExcept(chosen = null)
            log.warn { "multipart: part '$partName' manquante" }
            throw CoreException.InvalidState(partName, "partie '$partName' manquante")
        }
        else -> {
            cleanUpExcept(chosen = null)
            log.warn { "multipart: ${matched.size} parts '$partName' (une seule autorisée)" }
            throw CoreException.InvalidState(partName, "une seule partie '$partName' autorisée")
        }
    }
}
