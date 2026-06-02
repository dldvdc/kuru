package com.project.kuru.reach.storage

import java.nio.file.Path

/** Port reach → store : dépôt technique temporaire des octets validés. */
interface StagingStore {

    fun put(key: String, source: Path, contentType: String, contentLength: Long)

    fun delete(key: String)
}
