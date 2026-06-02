package com.project.kuru.reach.file.image

import com.project.kuru.core.image.ColorProfile
import com.project.kuru.core.image.Dimension

data class ImageMetadata(
    val dimensions: Dimension,
    val color: ColorProfile,
    val animated: Boolean,
)
