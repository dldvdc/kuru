package com.project.kuru.store

object ObjectKeys {

    fun staging(ulid: String, extension: String): String =
        "staging/$ulid.$extension"

    fun upload(ulid: String, extension: String): String =
        "uploads/$ulid.$extension"
}
