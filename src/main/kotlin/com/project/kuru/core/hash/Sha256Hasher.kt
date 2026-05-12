package com.project.kuru.core.hash

import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

object Sha256Hasher {

    fun hash(path: Path): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest()
    }
}
