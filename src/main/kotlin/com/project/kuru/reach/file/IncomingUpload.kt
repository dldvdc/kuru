package com.project.kuru.reach.file

import java.io.InputStream

/** Fichier entrant prêt à être copié — sans dépendance servlet. */
interface IncomingUpload {
    val fileName: String?
    val contentType: String?
    val size: Long

    fun openStream(): InputStream
}
