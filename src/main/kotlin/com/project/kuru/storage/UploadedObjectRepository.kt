package com.project.kuru.storage

import org.jdbi.v3.core.Jdbi
import org.springframework.stereotype.Repository

@Repository
class UploadedObjectRepository(
    private val jdbi: Jdbi,
) {

    fun findObjectKeyByContentSha256(contentSha256: ByteArray): String? =
        jdbi.withHandle<String?, RuntimeException> { h ->
            h.createQuery(
                """
                SELECT object_key FROM uploaded_object
                WHERE content_sha256 = :sha
                """.trimIndent(),
            )
                .bind("sha", contentSha256)
                .mapTo(String::class.java)
                .list()
                .firstOrNull()
        }

    fun insert(contentSha256: ByteArray, objectKey: String, sizeBytes: Long, originalFilename: String?) {
        try {
            jdbi.withHandle<Unit, Exception> { h ->
                h.createUpdate(
                    """
                INSERT INTO uploaded_object (content_sha256, object_key, size_bytes, original_filename)
                VALUES (:sha, :object_key, :size_bytes, :original_filename)
                    """.trimIndent(),
                )
                    .bind("sha", contentSha256)
                    .bind("object_key", objectKey)
                    .bind("size_bytes", sizeBytes)
                    .bind("original_filename", originalFilename)
                    .execute()
                Unit
            }
        } catch (e: Exception) {
            throw RuntimeException("insert uploaded_object", e)
        }
    }
}
