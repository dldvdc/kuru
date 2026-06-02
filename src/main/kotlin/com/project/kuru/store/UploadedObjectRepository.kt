package com.project.kuru.store

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

@Repository
class UploadedObjectRepository(
    private val jdbc: JdbcClient,
) {
    fun findObjectKeyByContentSha256(contentSha256: ByteArray): String? =
        jdbc.sql(
            """
            SELECT object_key FROM uploaded_object
            WHERE content_sha256 = :sha
            """.trimIndent(),
        )
            .param("sha", contentSha256)
            .query(String::class.java)
            .optional()
            .orElse(null)

    fun insert(contentSha256: ByteArray, objectKey: String, sizeBytes: Long, originalFilename: String?) {
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
    }
}
