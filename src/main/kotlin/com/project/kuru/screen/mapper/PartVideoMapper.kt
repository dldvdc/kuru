package com.project.kuru.screen.mapper

import com.project.kuru.core.CoreException
import com.project.kuru.flow.video.SubmittedVideo
import com.project.kuru.reach.file.IncomingUpload
import com.project.kuru.reach.file.PartCopy
import com.project.kuru.reach.file.video.FfprobeMetadataReader
import com.project.kuru.reach.mime.video.VideoFormat
import com.project.kuru.reach.mime.video.VideoMimeSniffer
import java.nio.file.Files
import java.nio.file.Path

class PartVideoMapper(
    private val config: SubmittedVideoConfigValues,
    private val videoMimeSniffer: VideoMimeSniffer,
    private val ffprobeMetadataReader: FfprobeMetadataReader,
) {

    fun fromUpload(upload: IncomingUpload): SubmittedVideo {
        val tempFile = Files.createTempFile(config.tempPrefix, config.tempSuffix)
        return try {
            upload.toSubmittedVideo(tempFile)
        } catch (e: Exception) {
            Files.deleteIfExists(tempFile)
            CoreException.invalidFormat(KIND, e)
        }
    }

    private fun IncomingUpload.toSubmittedVideo(tempFile: Path): SubmittedVideo {
        val fileName = requireFileName()
        val format = copyTo(tempFile)
            .requireHeader()
            .sniffFormat()
        val probe = probeMetadata(tempFile, format)

        return SubmittedVideo(
            fileName = fileName,
            mime = probe.format.mime,
            extension = probe.format.storageExtension(fileName),
            tempFile = tempFile,
            metadata = SubmittedVideo.Metadata(
                dimensions = probe.dimensions,
                durationSeconds = probe.durationSeconds,
                videoCodec = probe.videoCodec,
            ),
        )
    }

    private fun IncomingUpload.requireFileName(): String =
        fileName?.takeIf { it.isNotEmpty() }
            ?: throw CoreException.InvalidFormat(KIND)

    private fun IncomingUpload.copyTo(tempFile: Path): PartCopy.Result =
        try {
            PartCopy.toTempFile(
                upload = this,
                destination = tempFile,
                limits = PartCopy.Limits(
                    minHeaderSize = config.minHeaderSize,
                    maxSizeBytes = config.maxSizeBytes,
                    bufferSize = config.bufferSize,
                ),
            )
        } catch (e: Exception) {
            CoreException.invalidFormat(KIND, e)
        }

    private fun PartCopy.Result.requireHeader(): PartCopy.Result {
        if (size < config.minHeaderSize) {
            throw CoreException.InvalidFormat(KIND)
        }
        return this
    }

    private fun PartCopy.Result.sniffFormat(): VideoFormat =
        videoMimeSniffer.sniffVideo(header) ?: throw CoreException.InvalidFormat(KIND)

    private fun probeMetadata(tempFile: Path, format: VideoFormat): FfprobeMetadataReader.Result =
        ffprobeMetadataReader.read(tempFile, format)

    companion object {
        private const val KIND = "video"
    }
}
