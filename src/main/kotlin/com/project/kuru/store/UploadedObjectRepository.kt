package com.project.kuru.store

import com.project.kuru.core.toHexString
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

private val log = KotlinLogging.logger {}

@Repository
class UploadedObjectRepository(
    private val jdbc: JdbcClient,
) {
    fun findObjectKeyByContentSha256(contentSha256: ByteArray): String? {
        val shaHex = contentSha256.toHexString()
        val key = jdbc.sql(
            """
            SELECT object_key FROM uploaded_object
            WHERE content_sha256 = :sha
            """.trimIndent(),
        )
            .param("sha", contentSha256)
            .query(String::class.java)
            .optional()
            .orElse(null)

        if (key != null) {
            log.debug { "repo[uploaded_object]: doublon trouvé sha=$shaHex → objectKey=$key" }
        } else {
            log.debug { "repo[uploaded_object]: pas de doublon sha=$shaHex" }
        }
        return key
    }

    fun insert(contentSha256: ByteArray, objectKey: String, sizeBytes: Long, originalFilename: String?) {
        val shaHex = contentSha256.toHexString()
        log.debug {
            "repo[uploaded_object]: INSERT objectKey=$objectKey size=$sizeBytes " +
                "file=$originalFilename sha=$shaHex"
        }
        jdbc.sql(
            """
            INSERT INTO uploaded_object (content_sha256, object_key, size_bytes, original_filename)
            VALUES (:sha, :object_key, :size_bytes, :original_filename)
            """.trimIndent(),
        )
            .param("sha", contentSha256)
            .param("object_key", objectKey)
            .param("size_bytes", sizeBytes)
            .param("original_filename", originalFilename)
            .update()
        log.debug { "repo[uploaded_object]: INSERT OK objectKey=$objectKey" }
    }
}
