package com.project.kuru.core

import java.text.Normalizer
import java.util.Locale

@JvmInline
value class FileName private constructor(val value: String) {

    companion object {
        private const val FIELD = "File name"
        private const val MAX_LENGTH = 255

        private val FORBIDDEN_CHARS = Regex("[\\\\/:*?\"<>|]")

        private val RESERVED_NAMES = setOf(
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
        )

        operator fun invoke(rawInput: String): FileName {
            val input = rawInput
                ?.trim()
                ?.substringAfterLast("/")
                ?.substringAfterLast("\\")
                ?: throw CoreException.BlankValue(FIELD)

            if (input.length > MAX_LENGTH)
                throw CoreException.TooLong(FIELD, MAX_LENGTH)

            if (FORBIDDEN_CHARS.containsMatchIn(input))
                throw CoreException.InvalidFormat(FIELD)

            if (input == "." || input == "..")
                throw CoreException.InvalidFormat(FIELD)

            if (input.endsWith(' ') || input.endsWith('.'))
                throw CoreException.InvalidFormat(FIELD)

            val dot = input.lastIndexOf('.')
            val baseName = if (dot <= 0) input else input.substring(0, dot)

            if (baseName.uppercase(Locale.ROOT) in RESERVED_NAMES)
                throw CoreException.InvalidFormat(FIELD)

            return FileName(input)
        }
    }

    fun extension(): String {
        val dot = value.lastIndexOf('.')
        return if (dot <= 0 || dot == value.length - 1) ""
        else value.substring(dot + 1).lowercase(Locale.ROOT)
    }

    fun baseName(): String {
        val dot = value.lastIndexOf('.')
        return if (dot <= 0) value else value.substring(0, dot)
    }

    private fun normalize(input: String?): String {
        if (input == null) return ""
        val fileName = input.trim().substringAfterLast('/').substringAfterLast('\\')
        val dot = fileName.lastIndexOf('.')
        val base = if (dot <= 0) fileName else fileName.substring(0, dot)
        val ext = if (dot <= 0 || dot == fileName.length - 1) "" else fileName.substring(dot + 1)

        val tempBase = Normalizer.normalize(base, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .lowercase(Locale.ROOT)
            .replace(Regex("[\\s_]+"), "-")
            .replace(Regex("[^a-z0-9-]"), "")
            .replace(Regex("-+"), "-")
            .trim('-')

        val finalBase = tempBase.ifBlank { "file" }

        return if (ext.isBlank()) finalBase else "$finalBase.${ext.lowercase(Locale.ROOT)}"
    }
}