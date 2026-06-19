package com.project.kuru.screen.multipart

import com.project.kuru.core.FileName
import com.project.kuru.reach.file.IncomingUpload
import jakarta.servlet.http.Part
import java.io.Closeable
import java.io.InputStream

class UploadedFilePart private constructor(
    private val part: Part,
) : IncomingUpload, Closeable {

    override val fileName: String? =
        part.submittedFileName?.let { runCatching { FileName(it).value }.getOrNull() }
    override val contentType: String? = part.contentType
    override val size: Long = part.size

    override fun openStream(): InputStream = part.inputStream

    override fun close() {
        runCatching { part.delete() }
    }

    companion object {
        fun from(part: Part): UploadedFilePart = UploadedFilePart(part)
    }
}
