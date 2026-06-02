package com.project.kuru.reach.file.image

import com.project.kuru.core.image.ColorProfile
import com.project.kuru.core.image.ColorSpace
import java.awt.image.ColorModel
import javax.imageio.ImageReader

object ImageReaderColorProfile {

    fun fromReader(reader: ImageReader): ColorProfile =
        toColorProfile(resolveColorModel(reader))

    private fun resolveColorModel(reader: ImageReader): ColorModel? {
        val rawType = runCatching { reader.getRawImageType(0) }.getOrNull()
        if (rawType != null) {
            return rawType.colorModel
        }

        val imageType = runCatching { reader.getImageTypes(0).asSequence().firstOrNull() }.getOrNull()
        if (imageType != null) {
            return imageType.colorModel
        }

        val firstFrame = runCatching { reader.read(0) }.getOrNull()
        return firstFrame?.colorModel
    }

    private fun toColorProfile(colorModel: ColorModel?): ColorProfile {
        if (colorModel == null) {
            return ColorProfile(
                depth = 24,
                colorSpace = ColorSpace.Unknown,
            )
        }
        return ColorProfile(
            depth = colorModel.pixelSize.coerceIn(1, 64),
            colorSpace = mapColorSpace(colorModel),
        )
    }

    private fun mapColorSpace(model: ColorModel): ColorSpace =
        when (model.colorSpace.type) {
            java.awt.color.ColorSpace.TYPE_GRAY -> ColorSpace.Grayscale
            java.awt.color.ColorSpace.TYPE_RGB -> if (model.hasAlpha()) ColorSpace.RGBA else ColorSpace.RGB
            java.awt.color.ColorSpace.TYPE_CMYK -> ColorSpace.CMYK
            else -> ColorSpace.Unknown
        }
}
