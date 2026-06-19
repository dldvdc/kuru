package com.project.kuru.store

import com.project.kuru.flow.image.CatalogEntry
import com.project.kuru.flow.video.SubmittedVideo
import java.io.InputStream

/** Dépôt temporaire (bucket staging) avant commit catalogue. */
interface StagingStore {

    fun put(key: String, source: InputStream, contentType: String, contentLength: Long)

    fun delete(key: String)
}

/** Promotion staging → bucket final (images). */
fun interface ImageStore {

    fun promote(stagingKey: String, finalKey: String, entry: CatalogEntry)
}

/** Dépôt direct dans le bucket final (vidéo). */
fun interface VideoStore {

    fun store(video: SubmittedVideo, objectKey: String)
}

object ObjectKeys {

    fun staging(id: String, extension: String): String =
        "staging/$id.$extension"

    fun upload(id: String, extension: String): String =
        "uploads/$id.$extension"
}
