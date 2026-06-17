package com.project.kuru.reach.hash

import io.github.rctcwyvrn.blake3.Blake3
import java.nio.file.Files
import java.nio.file.Path

object Blake3Hasher {
    fun hash(path: Path): ByteArray {
        val hasher = Blake3.newInstance()
        Files.newInputStream(path).use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                hasher.update(if (read == buffer.size) buffer else buffer.copyOf(read))
            }
        }
        return hasher.digest()
    }
}