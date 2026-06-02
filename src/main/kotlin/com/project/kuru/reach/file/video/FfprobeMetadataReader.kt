package com.project.kuru.reach.file.video

import com.project.kuru.core.CoreException
import com.project.kuru.flow.video.SubmittedVideo
import com.project.kuru.reach.mime.video.VideoFormat
import tools.jackson.databind.JsonNode
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class FfprobeMetadataReader(
    private val ffprobePath: String,
    private val maxDurationSeconds: Double,
    private val maxWidth: Int,
    private val maxHeight: Int,
    private val probeTimeoutSeconds: Long = 30,
) {

    data class Result(
        val dimensions: SubmittedVideo.Dimension,
        val durationSeconds: Double,
        val videoCodec: String,
        val format: VideoFormat,
    )

    private val json = jacksonObjectMapper()

    fun read(tempFile: Path, sniffed: VideoFormat): Result {
        val output = runFfprobe(tempFile)
        val root = try {
            json.readTree(output)
        } catch (_: Exception) {
            throw CoreException.InvalidFormat("video")
        }

        val videoStream = root.path("streams").firstOrNull { it.path("codec_type").asString() == "video" }
            ?: throw CoreException.InvalidFormat("video")

        val width = videoStream.path("width").asInt()
        val height = videoStream.path("height").asInt()
        if (width <= 0 || height <= 0) {
            throw CoreException.InvalidFormat("video")
        }
        if (width > maxWidth || height > maxHeight) {
            throw CoreException.InvalidFormat("video")
        }

        val durationSeconds = parseDuration(root, videoStream)
        if (durationSeconds <= 0 || durationSeconds > maxDurationSeconds) {
            throw CoreException.InvalidFormat("video")
        }

        val codec = videoStream.path("codec_name").asString()?.trim().orEmpty()
        if (codec.isEmpty()) {
            throw CoreException.InvalidFormat("video")
        }

        val formatName = root.path("format").path("format_name").asString()?.substringBefore(',')?.trim().orEmpty()
        val format = resolveFormat(sniffed, formatName)

        return Result(
            dimensions = SubmittedVideo.Dimension(width, height),
            durationSeconds = durationSeconds,
            videoCodec = codec,
            format = format,
        )
    }

    private fun resolveFormat(sniffed: VideoFormat, formatName: String): VideoFormat {
        if (sniffed == VideoFormat.Matroska && formatName.isNotEmpty()) {
            return try {
                when (VideoFormat.fromFfprobeFormat(formatName)) {
                    VideoFormat.Webm -> VideoFormat.Webm
                    VideoFormat.Matroska -> VideoFormat.Matroska
                    else -> sniffed
                }
            } catch (_: IllegalArgumentException) {
                throw CoreException.InvalidFormat("video")
            }
        }
        if (formatName.isNotEmpty()) {
            return try {
                VideoFormat.fromFfprobeFormat(formatName)
            } catch (_: IllegalArgumentException) {
                sniffed
            }
        }
        return sniffed
    }

    private fun parseDuration(root: JsonNode, videoStream: JsonNode): Double {
        val formatDuration = root.path("format").path("duration").asString()?.toDoubleOrNull()
        if (formatDuration != null && formatDuration > 0) return formatDuration
        return videoStream.path("duration").asString()?.toDoubleOrNull() ?: 0.0
    }

    private fun runFfprobe(tempFile: Path): String {
        val process = try {
            ProcessBuilder(
                ffprobePath,
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=codec_name,codec_type,width,height,duration",
                "-show_entries", "format=format_name,duration",
                "-of", "json",
                tempFile.toAbsolutePath().toString(),
            )
                .redirectErrorStream(true)
                .start()
        } catch (_: Exception) {
            throw CoreException.InvalidFormat("video")
        }

        val output = process.inputStream.bufferedReader().readText()
        val finished = process.waitFor(probeTimeoutSeconds, TimeUnit.SECONDS)
        if (!finished) {
            process.destroyForcibly()
            throw CoreException.InvalidFormat("video")
        }
        if (process.exitValue() != 0) {
            throw CoreException.InvalidFormat("video")
        }
        return output
    }
}
