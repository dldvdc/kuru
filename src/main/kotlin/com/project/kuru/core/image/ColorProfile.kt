package com.project.kuru.core.image

data class ColorProfile(
    val depth: Int,
    val colorSpace: ColorSpace,
) {
    init {
        require(depth in 1..64) { "Profondeur de couleur invalide: $depth" }
    }
}
