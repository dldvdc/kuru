package com.project.kuru.screen.multipart

import com.project.kuru.core.FileName
import java.io.InputStream

data class ValidatedUpload(
    val fileName: FileName,
    val content: InputStream,
    val size: Long,
    val contentType: String?
)