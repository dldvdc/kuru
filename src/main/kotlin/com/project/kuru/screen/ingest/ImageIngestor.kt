package com.project.kuru.screen.ingest

import com.project.kuru.flow.image.AcceptedImage
import com.project.kuru.screen.multipart.ValidatedUpload

/** Port screen → reach : consomme un upload pré-validé, retourne une image acceptée. */
fun interface ImageIngestor {

    fun accept(upload: ValidatedUpload): AcceptedImage
}
