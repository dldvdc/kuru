package com.project.kuru.core.image

data class Dimension(val width: Int, val height: Int) {
    init {
        require(width > 0 && height > 0) { "Dimensions invalides: ${width}x${height}" }
    }

    val totalPixels: Long get() = width.toLong() * height
}
