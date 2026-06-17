package com.project.kuru.reach

object Color {

    data class Profile(
        val bitsPerChannel: Int,
        val colorSpace: Space,
    ) {
        init {
            val validDepths = intArrayOf(1, 8, 16, 32)
            require(bitsPerChannel in validDepths) {
                "Profondeur par canal non supportée: $bitsPerChannel bits. (Attendu: 1, 8, 16 ou 32)"
            }
        }
        val bitsPerPixel: Int get() = bitsPerChannel * colorSpace.channels
        val bytesPerPixel: Int get() = (bitsPerPixel + 7) / 8
    }

    enum class Space(
        val channels: Int,
        val hasAlpha: Boolean = false
    ) {
        RGB(3),
        RGBA(4, hasAlpha = true),
        Grayscale(1),
        CMYK(4),
        YCB_CR(3),
        HSV(3)
    }
}