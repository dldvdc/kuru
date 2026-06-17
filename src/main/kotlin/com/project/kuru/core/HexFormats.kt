package com.project.kuru.core

fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }
