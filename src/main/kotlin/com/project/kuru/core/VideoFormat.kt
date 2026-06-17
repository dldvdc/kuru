package com.project.kuru.core
// ─────────────────────────────────────────────────────────────────────────────
// Magic bytes helper
// ─────────────────────────────────────────────────────────────────────────────

class MagicBytes(
    val bytes: ByteArray,
    val offset: Int = 0,
    val mask: ByteArray? = null, // null = exact match
) {
    fun matches(header: ByteArray): Boolean {
        if (header.size < offset + bytes.size) return false
        return bytes.indices.all { i ->
            val b = header[offset + i].toInt() and 0xFF
            val m = mask?.getOrNull(i)?.toInt()?.and(0xFF) ?: 0xFF
            val expected = bytes[i].toInt() and 0xFF
            (b and m) == (expected and m)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// VideoCodec
// ─────────────────────────────────────────────────────────────────────────────

enum class VideoCodec(
    val fullName: String,
    val ffmpegDecoder: String,
    val ffmpegEncoder: String?,
    val isDecodableByBrowser: Boolean,
    val supportsHDR: Boolean,
    val maxColorDepth: Int,
    val isLossy: Boolean,
    val isHardwareAccelerated: Boolean, // widely available HW decode
) {

    H264(
        fullName = "Advanced Video Coding (H.264 / AVC)",
        ffmpegDecoder = "h264",
        ffmpegEncoder = "libx264",
        isDecodableByBrowser = true,
        supportsHDR = false,
        maxColorDepth = 8,
        isLossy = true,
        isHardwareAccelerated = true,
    ),

    H265(
        fullName = "High Efficiency Video Coding (H.265 / HEVC)",
        ffmpegDecoder = "hevc",
        ffmpegEncoder = "libx265",
        isDecodableByBrowser = false, // Safari only natively
        supportsHDR = true,
        maxColorDepth = 10,
        isLossy = true,
        isHardwareAccelerated = true,
    ),

    AV1(
        fullName = "AOMedia Video 1",
        ffmpegDecoder = "av1",
        ffmpegEncoder = "libaom-av1",
        isDecodableByBrowser = true, // Chrome, Firefox, Edge
        supportsHDR = true,
        maxColorDepth = 12,
        isLossy = true,
        isHardwareAccelerated = false, // emerging
    ),

    VP8(
        fullName = "VP8",
        ffmpegDecoder = "vp8",
        ffmpegEncoder = "libvpx",
        isDecodableByBrowser = true,
        supportsHDR = false,
        maxColorDepth = 8,
        isLossy = true,
        isHardwareAccelerated = false,
    ),

    VP9(
        fullName = "VP9",
        ffmpegDecoder = "vp9",
        ffmpegEncoder = "libvpx-vp9",
        isDecodableByBrowser = true,
        supportsHDR = true,
        maxColorDepth = 10,
        isLossy = true,
        isHardwareAccelerated = false,
    ),

    MPEG2(
        fullName = "MPEG-2 Video",
        ffmpegDecoder = "mpeg2video",
        ffmpegEncoder = "mpeg2video",
        isDecodableByBrowser = false,
        supportsHDR = false,
        maxColorDepth = 8,
        isLossy = true,
        isHardwareAccelerated = true,
    ),

    MPEG4(
        fullName = "MPEG-4 Part 2 (DivX / Xvid)",
        ffmpegDecoder = "mpeg4",
        ffmpegEncoder = "mpeg4",
        isDecodableByBrowser = false,
        supportsHDR = false,
        maxColorDepth = 8,
        isLossy = true,
        isHardwareAccelerated = false,
    ),

    THEORA(
        fullName = "Theora",
        ffmpegDecoder = "theora",
        ffmpegEncoder = "libtheora",
        isDecodableByBrowser = false, // Firefox legacy
        supportsHDR = false,
        maxColorDepth = 8,
        isLossy = true,
        isHardwareAccelerated = false,
    ),

    PRORES(
        fullName = "Apple ProRes",
        ffmpegDecoder = "prores",
        ffmpegEncoder = "prores_ks",
        isDecodableByBrowser = false,
        supportsHDR = true,
        maxColorDepth = 12,
        isLossy = false,
        isHardwareAccelerated = false, // Apple Silicon only
    ),

    DNXHD(
        fullName = "Avid DNxHD / DNxHR",
        ffmpegDecoder = "dnxhd",
        ffmpegEncoder = "dnxhd",
        isDecodableByBrowser = false,
        supportsHDR = false,
        maxColorDepth = 10,
        isLossy = false,
        isHardwareAccelerated = false,
    ),

    VP6(
        fullName = "VP6 (Flash legacy)",
        ffmpegDecoder = "vp6f",
        ffmpegEncoder = null, // encode not supported in modern FFmpeg
        isDecodableByBrowser = false,
        supportsHDR = false,
        maxColorDepth = 8,
        isLossy = true,
        isHardwareAccelerated = false,
    ),
}

// ─────────────────────────────────────────────────────────────────────────────
// AudioCodec
// ─────────────────────────────────────────────────────────────────────────────

enum class AudioCodec(
    val fullName: String,
    val ffmpegDecoder: String,
    val ffmpegEncoder: String?,
    val isLossy: Boolean,
    val isDecodableByBrowser: Boolean,
    val maxSampleRateHz: Int,
    val maxChannels: Int,
) {

    AAC(
        fullName = "Advanced Audio Coding",
        ffmpegDecoder = "aac",
        ffmpegEncoder = "aac",
        isLossy = true,
        isDecodableByBrowser = true,
        maxSampleRateHz = 96_000,
        maxChannels = 8,
    ),

    MP3(
        fullName = "MPEG Audio Layer III",
        ffmpegDecoder = "mp3",
        ffmpegEncoder = "libmp3lame",
        isLossy = true,
        isDecodableByBrowser = true,
        maxSampleRateHz = 48_000,
        maxChannels = 2,
    ),

    OPUS(
        fullName = "Opus",
        ffmpegDecoder = "opus",
        ffmpegEncoder = "libopus",
        isLossy = true,
        isDecodableByBrowser = true, // Chrome, Firefox, Edge
        maxSampleRateHz = 48_000,
        maxChannels = 8,
    ),

    VORBIS(
        fullName = "Vorbis",
        ffmpegDecoder = "vorbis",
        ffmpegEncoder = "libvorbis",
        isLossy = true,
        isDecodableByBrowser = false,
        maxSampleRateHz = 192_000,
        maxChannels = 8,
    ),

    FLAC(
        fullName = "Free Lossless Audio Codec",
        ffmpegDecoder = "flac",
        ffmpegEncoder = "flac",
        isLossy = false,
        isDecodableByBrowser = false,
        maxSampleRateHz = 655_350,
        maxChannels = 8,
    ),

    PCM(
        fullName = "Pulse-Code Modulation (raw audio)",
        ffmpegDecoder = "pcm_s16le",
        ffmpegEncoder = "pcm_s16le",
        isLossy = false,
        isDecodableByBrowser = false,
        maxSampleRateHz = 192_000,
        maxChannels = 32,
    ),

    AC3(
        fullName = "Dolby Digital (AC-3)",
        ffmpegDecoder = "ac3",
        ffmpegEncoder = "ac3",
        isLossy = true,
        isDecodableByBrowser = false,
        maxSampleRateHz = 48_000,
        maxChannels = 6,
    ),

    EAC3(
        fullName = "Dolby Digital Plus (E-AC-3)",
        ffmpegDecoder = "eac3",
        ffmpegEncoder = "eac3",
        isLossy = true,
        isDecodableByBrowser = false,
        maxSampleRateHz = 48_000,
        maxChannels = 16,
    ),
}

// ─────────────────────────────────────────────────────────────────────────────
// VideoContainer
// ─────────────────────────────────────────────────────────────────────────────

enum class VideoContainer(
    val mimeType: String,
    val extensions: List<String>,
    val fullName: String,
    val magicBytes: List<MagicBytes>,
    val supportedVideoCodecs: List<VideoCodec>,
    val supportedAudioCodecs: List<AudioCodec>,
    val supportsChapters: Boolean,
    val supportsSubtitles: Boolean,
    val supportsMultipleStreams: Boolean,
    val isStreamingFriendly: Boolean, // fragmented / seekable without full download
) {

    MP4(
        mimeType = "video/mp4",
        extensions = listOf("mp4", "m4v"),
        fullName = "MPEG-4 Part 14",
        magicBytes = listOf(
            // "ftyp" at offset 4 — brand varies (isom, mp41, mp42, avc1...)
            MagicBytes(byteArrayOf(0x66, 0x74, 0x79, 0x70), offset = 4),
        ),
        supportedVideoCodecs = listOf(VideoCodec.H264, VideoCodec.H265, VideoCodec.AV1, VideoCodec.MPEG4),
        supportedAudioCodecs = listOf(AudioCodec.AAC, AudioCodec.MP3, AudioCodec.AC3, AudioCodec.EAC3, AudioCodec.FLAC),
        supportsChapters = true,
        supportsSubtitles = true,
        supportsMultipleStreams = true,
        isStreamingFriendly = true,
    ),

    MOV(
        mimeType = "video/quicktime",
        extensions = listOf("mov"),
        fullName = "QuickTime Movie",
        magicBytes = listOf(
            // "ftyp" at offset 4 with brand "qt  "
            MagicBytes(byteArrayOf(0x66, 0x74, 0x79, 0x70, 0x71, 0x74, 0x20, 0x20), offset = 4),
            // legacy MOV: "moov" or "wide" or "mdat" at offset 4
            MagicBytes(byteArrayOf(0x6D, 0x6F, 0x6F, 0x76), offset = 4),
        ),
        supportedVideoCodecs = listOf(VideoCodec.H264, VideoCodec.H265, VideoCodec.PRORES, VideoCodec.DNXHD),
        supportedAudioCodecs = listOf(AudioCodec.AAC, AudioCodec.PCM, AudioCodec.FLAC),
        supportsChapters = true,
        supportsSubtitles = true,
        supportsMultipleStreams = true,
        isStreamingFriendly = false,
    ),

    MKV(
        mimeType = "video/x-matroska",
        extensions = listOf("mkv"),
        fullName = "Matroska Video",
        magicBytes = listOf(
            MagicBytes(byteArrayOf(0x1A, 0x45, 0xDF.toByte(), 0xA3.toByte()), offset = 0),
        ),
        supportedVideoCodecs = VideoCodec.entries,
        supportedAudioCodecs = AudioCodec.entries,
        supportsChapters = true,
        supportsSubtitles = true,
        supportsMultipleStreams = true,
        isStreamingFriendly = false,
    ),

    WEBM(
        mimeType = "video/webm",
        extensions = listOf("webm"),
        fullName = "WebM",
        // WebM is a subset of MKV — same magic bytes
        magicBytes = listOf(
            MagicBytes(byteArrayOf(0x1A, 0x45, 0xDF.toByte(), 0xA3.toByte()), offset = 0),
        ),
        supportedVideoCodecs = listOf(VideoCodec.VP8, VideoCodec.VP9, VideoCodec.AV1),
        supportedAudioCodecs = listOf(AudioCodec.OPUS, AudioCodec.VORBIS),
        supportsChapters = false,
        supportsSubtitles = true,
        supportsMultipleStreams = true,
        isStreamingFriendly = true,
    ),

    AVI(
        mimeType = "video/x-msvideo",
        extensions = listOf("avi"),
        fullName = "Audio Video Interleave",
        magicBytes = listOf(
            // "RIFF" at 0, "AVI " at 8
            MagicBytes(byteArrayOf(0x52, 0x49, 0x46, 0x46), offset = 0),
        ),
        supportedVideoCodecs = listOf(VideoCodec.H264, VideoCodec.MPEG4, VideoCodec.MPEG2),
        supportedAudioCodecs = listOf(AudioCodec.MP3, AudioCodec.AAC, AudioCodec.PCM, AudioCodec.AC3),
        supportsChapters = false,
        supportsSubtitles = false,
        supportsMultipleStreams = false,
        isStreamingFriendly = false,
    ),

    TS(
        mimeType = "video/mp2t",
        extensions = listOf("ts", "mts", "m2ts"),
        fullName = "MPEG-2 Transport Stream",
        magicBytes = listOf(
            // sync byte 0x47 repeated every 188 bytes
            MagicBytes(byteArrayOf(0x47), offset = 0),
        ),
        supportedVideoCodecs = listOf(VideoCodec.H264, VideoCodec.H265, VideoCodec.MPEG2),
        supportedAudioCodecs = listOf(AudioCodec.AAC, AudioCodec.MP3, AudioCodec.AC3),
        supportsChapters = false,
        supportsSubtitles = true,
        supportsMultipleStreams = true,
        isStreamingFriendly = true, // designed for broadcast streaming
    ),

    FLV(
        mimeType = "video/x-flv",
        extensions = listOf("flv"),
        fullName = "Flash Video",
        magicBytes = listOf(
            MagicBytes(byteArrayOf(0x46, 0x4C, 0x56), offset = 0), // "FLV"
        ),
        supportedVideoCodecs = listOf(VideoCodec.H264, VideoCodec.VP6),
        supportedAudioCodecs = listOf(AudioCodec.AAC, AudioCodec.MP3),
        supportsChapters = false,
        supportsSubtitles = false,
        supportsMultipleStreams = false,
        isStreamingFriendly = false,
    ),

    OGV(
        mimeType = "video/ogg",
        extensions = listOf("ogv", "ogg"),
        fullName = "Ogg Video",
        magicBytes = listOf(
            MagicBytes(byteArrayOf(0x4F, 0x67, 0x67, 0x53), offset = 0), // "OggS"
        ),
        supportedVideoCodecs = listOf(VideoCodec.THEORA),
        supportedAudioCodecs = listOf(AudioCodec.VORBIS, AudioCodec.OPUS, AudioCodec.FLAC),
        supportsChapters = false,
        supportsSubtitles = false,
        supportsMultipleStreams = true,
        isStreamingFriendly = false,
    );

    companion object {
        /**
         * Sniff container from raw header bytes (read at least 16 bytes).
         * Note: MKV and WebM share the same magic bytes — further inspection
         * of the DocType EBML element is required to distinguish them.
         */
        fun sniff(header: ByteArray): VideoContainer? =
            entries.firstOrNull { container ->
                container.magicBytes.any { it.matches(header) }
            }

        fun fromExtension(extension: String): VideoContainer? =
            entries.find { it.extensions.contains(extension.lowercase()) }
    }
}