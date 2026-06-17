package com.project.kuru.flow

import com.project.kuru.flow.image.CatalogEntry

fun interface ImageStore {
    fun promote(stagingKey: String, finalKey: String, entry: CatalogEntry)
}
