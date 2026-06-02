package com.project.kuru.reach.mime.video

internal val Int.byteHex get() = toByte()

sealed interface VideoFormat {

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

    /** Conteneur ISOBMFF (MP4, M4V, 3GP…). */
    data object Mp4 : VideoFormat {
        override val mime = "video/mp4"
        override val extensions = listOf("mp4", "m4v", "3gp", "3g2")
        override val signature = Signature(minHeaderSize = 12, reliability = 3)
        override val prevalence = 90
        override fun matches(header: ByteArray): Boolean {
            val ftyp = header.slice(4..7).toByteArray().toString(Charsets.ISO_8859_1)
            if (ftyp != "ftyp") return false
            val brand = header.slice(8..11).toByteArray().toString(Charsets.ISO_8859_1)
            return brand in MP4_BRANDS
        }
    }

    data object Mov : VideoFormat {
        override val mime = "video/quicktime"
        override val extensions = listOf("mov")
        override val signature = Signature(minHeaderSize = 12, reliability = 3)
        override val prevalence = 40
        override fun matches(header: ByteArray): Boolean {
            val ftyp = header.slice(4..7).toByteArray().toString(Charsets.ISO_8859_1)
            val brand = header.slice(8..11).toByteArray().toString(Charsets.ISO_8859_1)
            return ftyp == "ftyp" && brand == "qt  "
        }
    }

    /** Matroska — signature EBML ; WebM est affiné par ffprobe. */
    data object Matroska : VideoFormat {
        override val mime = "video/x-matroska"
        override val extensions = listOf("mkv")
        override val signature = Signature(minHeaderSize = 4, reliability = 2)
        override val prevalence = 25
        override fun matches(header: ByteArray): Boolean =
            header[0] == 0x1A.byteHex && header[1] == 0x45.byteHex &&
                header[2] == 0xDF.byteHex && header[3] == 0xA3.byteHex
    }

    /** MIME distinct pour WebM (même EBML que Matroska, affiné par ffprobe). */
    data object Webm : VideoFormat {
        override val mime = "video/webm"
        override val extensions = listOf("webm")
        override val signature = Signature(minHeaderSize = 4, reliability = 2)
        override val prevalence = 30
        override fun matches(header: ByteArray): Boolean = Matroska.matches(header)
    }

    companion object {

        private val MP4_BRANDS = setOf(
            "isom", "iso2", "mp41", "mp42", "avc1", "M4V ", "ndas", "MSNV", "3gp4", "3g2a",
        )

        /** Octets à capturer pour le sniff — max des minHeaderSize par format. */
        val maxHeaderSize: Int
            get() = all.maxOf { it.signature.minHeaderSize }

        /** Formats sniffables — WebM exclu (même EBML que Matroska, affiné par ffprobe). */
        val all: List<VideoFormat> =
            listOf(Mp4, Mov, Matroska)
                .sortedWith(
                    compareByDescending<VideoFormat> { it.signature.reliability }
                        .thenByDescending { it.prevalence },
                )

        fun fromFfprobeFormat(formatName: String): VideoFormat = when (formatName.lowercase()) {
            "webm" -> Webm
            "matroska" -> Matroska
            "mov" -> Mov
            "mp4", "m4v", "3gp", "3g2", "mj2" -> Mp4
            else -> throw IllegalArgumentException("Unsupported ffprobe format: $formatName")
        }
    }
}
