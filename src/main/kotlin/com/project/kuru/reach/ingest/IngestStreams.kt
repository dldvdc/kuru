package com.project.kuru.reach.ingest

import java.io.InputStream
import java.security.MessageDigest

class HashingInputStream(private val delegate: InputStream) : InputStream() {

    private val digest = MessageDigest.getInstance("SHA-256")

    override fun read(): Int {
        val byte = delegate.read()
        if (byte >= 0) digest.update(byte.toByte())
        return byte
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val read = delegate.read(b, off, len)
        if (read > 0) digest.update(b, off, read)
        return read
    }

    override fun close() = delegate.close()

    fun hash(): ByteArray = digest.digest()
}

class CountingInputStream(
    private val delegate: InputStream,
    private val maxBytes: Long,
) : InputStream() {

    var bytesRead: Long = 0
        private set

    override fun read(): Int {
        val byte = delegate.read()
        if (byte >= 0) increment(1)
        return byte
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val read = delegate.read(b, off, len)
        if (read > 0) increment(read.toLong())
        return read
    }

    override fun close() = delegate.close()

    private fun increment(count: Long) {
        bytesRead += count
        require(bytesRead <= maxBytes) { "max size exceeded: $bytesRead > $maxBytes" }
    }
}
