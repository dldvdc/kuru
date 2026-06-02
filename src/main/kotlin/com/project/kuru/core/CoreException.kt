package com.project.kuru.core

sealed class CoreException(
    open val field: String,
    message: String
) : RuntimeException(message) {

    data class NullValue(
        override val field: String
    ) : CoreException(field, "$field cannot be null")

    data class BlankValue(
        override val field: String
    ) : CoreException(field, "$field cannot be blank")

    data class TooShort(
        override val field: String,
        val min: Int
    ) : CoreException(field, "$field must be at least $min characters")

    data class TooLong(
        override val field: String,
        val max: Int
    ) : CoreException(field, "$field must be at most $max characters")

    data class InvalidFormat(
        override val field: String
    ) : CoreException(field, "$field has invalid format")

    data class InvalidState(
        override val field: String,
        val reason: String
    ) : CoreException(field, "$field: $reason")

    companion object {
        /** Relance une CoreException ; sinon InvalidFormat pour le champ donné. */
        fun invalidFormat(field: String, cause: Exception): Nothing {
            if (cause is CoreException) throw cause
            throw InvalidFormat(field)
        }
    }
}