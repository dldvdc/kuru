package com.project.kuru.core

enum class ImageFormat(
    val mimeType: String,
    val extensions: List<String>,
    val fullName: String,
    val compression: Compression,
    val structure: Structure,
    val hasAlpha: Boolean,
    val isAnimatable: Boolean,
    val maxColorDepth: Int,
    val supportsICC: Boolean,
    val supportsMetadata: Boolean,
    val supportsLayers: Boolean,
    val supportsHDR: Boolean,
) {

    JPEG(
        mimeType = "image/jpeg",
        extensions = listOf("jpg", "jpeg"),
        fullName = "Joint Photographic Experts Group",
        compression = Compression.LOSSY,
        structure = Structure.RASTER,
        hasAlpha = false,
        isAnimatable = false,
        maxColorDepth = 8,
        supportsICC = true,
        supportsMetadata = true,
        supportsLayers = false,
        supportsHDR = false,
    ),

    PNG(
        mimeType = "image/png",
        extensions = listOf("png"),
        fullName = "Portable Network Graphics",
        compression = Compression.LOSSLESS,
        structure = Structure.RASTER,
        hasAlpha = true,
        isAnimatable = false,
        maxColorDepth = 16,
        supportsICC = true,
        supportsMetadata = false,
        supportsLayers = false,
        supportsHDR = false,
    ),

    APNG(
        mimeType = "image/apng",
        extensions = listOf("apng"),
        fullName = "Animated Portable Network Graphics",
        compression = Compression.LOSSLESS,
        structure = Structure.RASTER,
        hasAlpha = true,
        isAnimatable = true,
        maxColorDepth = 16,
        supportsICC = true,
        supportsMetadata = false,
        supportsLayers = false,
        supportsHDR = false,
    ),

    GIF(
        mimeType = "image/gif",
        extensions = listOf("gif"),
        fullName = "Graphics Interchange Format",
        compression = Compression.LOSSLESS,
        structure = Structure.RASTER,
        hasAlpha = true,  // 1-bit transparency only
        isAnimatable = true,
        maxColorDepth = 8,
        supportsICC = false,
        supportsMetadata = false,
        supportsLayers = false,
        supportsHDR = false,
    ),

    WEBP(
        mimeType = "image/webp",
        extensions = listOf("webp"),
        fullName = "Web Picture Format",
        compression = Compression.LOSSY_OR_LOSSLESS,
        structure = Structure.RASTER,
        hasAlpha = true,
        isAnimatable = true,
        maxColorDepth = 8,
        supportsICC = true,
        supportsMetadata = true,
        supportsLayers = false,
        supportsHDR = false,
    ),

    AVIF(
        mimeType = "image/avif",
        extensions = listOf("avif"),
        fullName = "AV1 Image File Format",
        compression = Compression.LOSSY_OR_LOSSLESS,
        structure = Structure.RASTER,
        hasAlpha = true,
        isAnimatable = true,
        maxColorDepth = 12,
        supportsICC = true,
        supportsMetadata = true,
        supportsLayers = false,
        supportsHDR = true,
    ),

    JXL(
        mimeType = "image/jxl",
        extensions = listOf("jxl"),
        fullName = "JPEG XL",
        compression = Compression.LOSSY_OR_LOSSLESS,
        structure = Structure.RASTER,
        hasAlpha = true,
        isAnimatable = true,
        maxColorDepth = 32,
        supportsICC = true,
        supportsMetadata = true,
        supportsLayers = false,
        supportsHDR = true,
    ),

    SVG(
        mimeType = "image/svg+xml",
        extensions = listOf("svg"),
        fullName = "Scalable Vector Graphics",
        compression = Compression.NONE,
        structure = Structure.VECTOR,
        hasAlpha = true,
        isAnimatable = true,
        maxColorDepth = 32,
        supportsICC = false,
        supportsMetadata = false,
        supportsLayers = false,
        supportsHDR = false,
    ),

    BMP(
        mimeType = "image/bmp",
        extensions = listOf("bmp"),
        fullName = "Bitmap",
        compression = Compression.NONE,
        structure = Structure.RASTER,
        hasAlpha = true,
        isAnimatable = false,
        maxColorDepth = 32,
        supportsICC = false,
        supportsMetadata = false,
        supportsLayers = false,
        supportsHDR = false,
    ),

    TIFF(
        mimeType = "image/tiff",
        extensions = listOf("tiff", "tif"),
        fullName = "Tagged Image File Format",
        compression = Compression.LOSSY_OR_LOSSLESS,
        structure = Structure.RASTER,
        hasAlpha = true,
        isAnimatable = false,
        maxColorDepth = 32,
        supportsICC = true,
        supportsMetadata = true,
        supportsLayers = true,
        supportsHDR = true,
    );

    companion object {
        fun fromMimeType(mimeType: String): ImageFormat? =
            entries.find { it.mimeType.equals(mimeType, ignoreCase = true) }

        fun fromExtension(extension: String): ImageFormat? =
            entries.find { it.extensions.contains(extension.lowercase()) }
    }
}

enum class Compression {
    LOSSY,
    LOSSLESS,
    LOSSY_OR_LOSSLESS,
    NONE,
}

enum class Structure {
    RASTER,
    VECTOR,
}