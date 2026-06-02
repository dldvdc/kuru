package com.project.kuru.screen.multipart

import com.project.kuru.core.CoreException
import jakarta.servlet.http.Part
import org.springframework.web.servlet.function.ServerRequest

object Multipart {

    fun select(req: ServerRequest, partName: String): Part {

        val allParts = runCatching { req.servletRequest().parts.toList() }
            .getOrElse { throw CoreException.InvalidState(partName, "requête multipart invalide") }

        val matched = allParts.filter { it.name == partName }

        return when (matched.size) {
            1 -> {
                val chosenPart = matched.single()
                allParts.filter { it != chosenPart }.forEach { runCatching { it.delete() } }
                chosenPart
            }
            0 -> {
                allParts.forEach { runCatching { it.delete() } }
                throw CoreException.InvalidState(partName, "partie '${partName}' manquante")
            }
            else -> {
                allParts.forEach { runCatching { it.delete() } }
                throw CoreException.InvalidState(partName, "une seule partie '${partName}' autorisée")
            }
        }
    }
}