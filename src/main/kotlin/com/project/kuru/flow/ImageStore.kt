package com.project.kuru.flow

import com.project.kuru.core.image.RawImageFile

fun interface ImageStore {
    fun store(image: RawImageFile, objectKey: String)
}
