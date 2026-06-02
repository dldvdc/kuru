package com.project.kuru.flow

import com.project.kuru.core.image.VerifiedImage

fun interface ImageStore {

    fun promote(stagingKey: String, finalKey: String, image: VerifiedImage)
}
