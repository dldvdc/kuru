package com.project.kuru.reach.file

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

object PartCopy {

    data class Limits(
        val minHeaderSize: Int,
        val maxSizeBytes: Long,
        val bufferSize: Int,
    )

    data class Result(val size: Long, val header: ByteArray)

    fun toTempFile(upload: IncomingUpload, destination: Path, limits: Limits): Result =
        upload.openStream().use { input -> toTempFile(input, destination, limits) }

    fun toTempFile(input: InputStream, destination: Path, limits: Limits): Result {
        val header = ByteArrayOutputStream(limits.minHeaderSize)
        var size = 0L
        input.use { stream ->
            Files.newOutputStream(destination).use { output ->
                val buffer = ByteArray(limits.bufferSize)
                while (true) {
                    val read = stream.read(buffer)
                    if (read < 0) break
                    size += read.toLong()
                    require(size <= limits.maxSizeBytes) { "max size exceeded: $size > ${limits.maxSizeBytes}" }
                    output.write(buffer, 0, read)
                    if (header.size() < limits.minHeaderSize) {
                        val missing = limits.minHeaderSize - header.size()
                        header.write(buffer, 0, minOf(read, missing))
                    }
                }
            }
        }
        return Result(size = size, header = header.toByteArray())
    }
}
