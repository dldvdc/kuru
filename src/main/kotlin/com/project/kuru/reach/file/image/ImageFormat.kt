package com.project.kuru.reach.mime.image

internal val Int.byteHex get() = toByte()

sealed interface ImageFormat {

    val mime: String
    /** Extensions reconnues ; la première est le défaut pour le stockage. */
    val extensions: List<String>
    val extension: String get() = extensions.first()
    val signature: Signature

    /** Poids de priorité au sniff (0–100) — ordre d'usage estimé, pas une stat exacte. */
    val prevalence: Int

    fun matches(header: ByteArray): Boolean

    /** Préfère l'extension du nom de fichier si elle correspond au format. */
    fun storageExtension(fileName: String): String {
        val fromName = fileName.substringAfterLast('.', "").lowercase()
        return extensions.find { it == fromName } ?: extension
    }

    data class Signature(
        val minHeaderSize: Int,
        /** 3 = forte, 1 = faible — certaines signatures se chevauchent ou sont courtes. */
        val reliability: Int,
    )

    /** Magic bytes FF D8 FF — très fiable. */
    data object Jpeg : ImageFormat {
        override val mime = "image/jpeg"
        override val extensions = listOf("jpg", "jpeg")
        override val signature = Signature(minHeaderSize = 3, reliability = 3)
        override val prevalence = 95
        override fun matches(header: ByteArray) =
            header[0] == 0xFF.byteHex && header[1] == 0xD8.byteHex && header[2] == 0xFF.byteHex
    }

    /** 8 octets fixes — très fiable. */
    data object Png : ImageFormat {
        override val mime = "image/png"
        override val extensions = listOf("png")
        override val signature = Signature(minHeaderSize = 8, reliability = 3)
        override val prevalence = 90
        override fun matches(header: ByteArray) =
            header[0] == 0x89.byteHex && header[1] == 0x50.byteHex && header[2] == 0x4E.byteHex &&
                header[3] == 0x47.byteHex && header[4] == 0x0D.byteHex && header[5] == 0x0A.byteHex &&
                header[6] == 0x1A.byteHex && header[7] == 0x0A.byteHex
    }

    data object Gif : ImageFormat {
        override val mime = "image/gif"
        override val extensions = listOf("gif")
        override val signature = Signature(minHeaderSize = 6, reliability = 3)
        override val prevalence = 60
        override fun matches(header: ByteArray) =
            header[0] == 0x47.byteHex && header[1] == 0x49.byteHex && header[2] == 0x46.byteHex &&
                header[3] == 0x38.byteHex &&
                (header[4] == 0x37.byteHex || header[4] == 0x39.byteHex) && header[5] == 0x61.byteHex
    }

    /** RIFF + fourcc WEBP — fiable si 12 octets lus ; autres RIFF (AVI, WAV) exclus par le fourcc. */
    data object Webp : ImageFormat {
        override val mime = "image/webp"
        override val extensions = listOf("webp")
        override val signature = Signature(minHeaderSize = 12, reliability = 3)
        override val prevalence = 75
        override fun matches(header: ByteArray) =
            header[0] == 0x52.byteHex && header[1] == 0x49.byteHex && header[2] == 0x46.byteHex &&
                header[3] == 0x46.byteHex &&
                header[8] == 0x57.byteHex && header[9] == 0x45.byteHex &&
                header[10] == 0x42.byteHex && header[11] == 0x50.byteHex
    }

    /** Box JP2 — 12 octets spécifiques, fiable. */
    data object Jpeg2000 : ImageFormat {
        override val mime = "image/jp2"
        override val extensions = listOf("jp2")
        override val signature = Signature(minHeaderSize = 12, reliability = 3)
        override val prevalence = 3
        override fun matches(header: ByteArray) =
            header[0] == 0x00.byteHex && header[1] == 0x00.byteHex &&
                header[2] == 0x00.byteHex && header[3] == 0x0C.byteHex &&
                header[4] == 0x6A.byteHex && header[5] == 0x50.byteHex &&
                header[6] == 0x20.byteHex && header[7] == 0x20.byteHex &&
                header[8] == 0x0D.byteHex && header[9] == 0x0A.byteHex &&
                header[10] == 0x87.byteHex && header[11] == 0x0A.byteHex
    }

    /** ISOBMFF ftyp + brand — partage la structure avec HEIC/MP4, désambiguïsé par le brand. */
    data object Avif : ImageFormat {
        override val mime = "image/avif"
        override val extensions = listOf("avif")
        override val signature = Signature(minHeaderSize = 12, reliability = 2)
        override val prevalence = 25
        override fun matches(header: ByteArray): Boolean {
            val ftyp = header.slice(4..7).toByteArray().toString(Charsets.ISO_8859_1)
            val brand = header.slice(8..11).toByteArray().toString(Charsets.ISO_8859_1)
            return ftyp == "ftyp" && (brand == "avif" || brand == "avis")
        }
    }

    /** ISOBMFF ftyp + brand — même famille qu'AVIF, brands distincts. */
    data object Heic : ImageFormat {
        override val mime = "image/heic"
        override val extensions = listOf("heic", "heif")
        override val signature = Signature(minHeaderSize = 12, reliability = 2)
        override val prevalence = 45
        override fun matches(header: ByteArray): Boolean {
            val ftyp = header.slice(4..7).toByteArray().toString(Charsets.ISO_8859_1)
            val brand = header.slice(8..11).toByteArray().toString(Charsets.ISO_8859_1)
            return ftyp == "ftyp" && brand in setOf("heic", "heix", "hevc", "hevx", "mif1", "msf1")
        }
    }

    /** II/MM — 4 octets, partagé avec d'autres formats TIFF-like. */
    data object Tiff : ImageFormat {
        override val mime = "image/tiff"
        override val extensions = listOf("tif", "tiff")
        override val signature = Signature(minHeaderSize = 4, reliability = 2)
        override val prevalence = 15
        override fun matches(header: ByteArray) =
            (header[0] == 0x49.byteHex && header[1] == 0x49.byteHex && header[2] == 0x2A.byteHex && header[3] == 0x00.byteHex) ||
                (header[0] == 0x4D.byteHex && header[1] == 0x4D.byteHex && header[2] == 0x00.byteHex && header[3] == 0x2A.byteHex)
    }

    data object Bmp : ImageFormat {
        override val mime = "image/bmp"
        override val extensions = listOf("bmp")
        override val signature = Signature(minHeaderSize = 2, reliability = 2)
        override val prevalence = 10
        override fun matches(header: ByteArray) =
            header[0] == 0x42.byteHex && header[1] == 0x4D.byteHex
    }

    /** 00 00 01 00 — court, risque théorique de faux positif binaire. */
    data object Ico : ImageFormat {
        override val mime = "image/x-icon"
        override val extensions = listOf("ico")
        override val signature = Signature(minHeaderSize = 4, reliability = 2)
        override val prevalence = 8
        override fun matches(header: ByteArray) =
            header[0] == 0x00.byteHex && header[1] == 0x00.byteHex &&
                header[2] == 0x01.byteHex && header[3] == 0x00.byteHex
    }

    /** Container ISOBMFF JXL — 12 octets spécifiques. Codestream nu (FF 0A) volontairement exclu. */
    data object Jxl : ImageFormat {
        override val mime = "image/jxl"
        override val extensions = listOf("jxl")
        override val signature = Signature(minHeaderSize = 12, reliability = 3)
        override val prevalence = 5
        override fun matches(header: ByteArray) =
            header[0] == 0x00.byteHex && header[1] == 0x00.byteHex &&
                header[2] == 0x00.byteHex && header[3] == 0x0C.byteHex &&
                header[4] == 0x4A.byteHex && header[5] == 0x58.byteHex &&
                header[6] == 0x4C.byteHex && header[7] == 0x20.byteHex &&
                header[8] == 0x0D.byteHex && header[9] == 0x0A.byteHex &&
                header[10] == 0x87.byteHex && header[11] == 0x0A.byteHex
    }

    /** Sniff textuel : balise <svg requise (pas seul <?xml). En-tête large pour les déclarations XML. */
    data object Svg : ImageFormat {
        override val mime = "image/svg+xml"
        override val extensions = listOf("svg")
        override val signature = Signature(minHeaderSize = 512, reliability = 1)
        override val prevalence = 35
        override fun matches(header: ByteArray): Boolean =
            SVG_TAG.containsMatchIn(header.toString(Charsets.UTF_8))
    }

    companion object {

        private val SVG_TAG = Regex("""<\s*svg\b""", RegexOption.IGNORE_CASE)

        /** Octets à capturer pour le sniff — max des minHeaderSize par format. */
        val maxHeaderSize: Int
            get() = all.maxOf { it.signature.minHeaderSize }

        /** Fiabilité d'abord, puis prévalence — limite les faux positifs des signatures faibles. */
        val all: List<ImageFormat> =
            listOf(Jpeg, Png, Gif, Webp, Jpeg2000, Heic, Avif, Tiff, Bmp, Ico, Jxl, Svg)
                .sortedWith(
                compareByDescending<ImageFormat> { it.signature.reliability }
                    .thenByDescending { it.prevalence },
            )
    }
}
