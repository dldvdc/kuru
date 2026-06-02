package com.project.kuru.core.image

sealed interface ColorSpace {
    val channels: Int
    val hasAlpha: Boolean get() = false

    data object RGB       : ColorSpace { override val channels = 3 }
    data object RGBA      : ColorSpace { override val channels = 4; override val hasAlpha = true }
    data object Grayscale : ColorSpace { override val channels = 1 }
    data object CMYK      : ColorSpace { override val channels = 4 }
    data object Unknown   : ColorSpace { override val channels = 3 }
}