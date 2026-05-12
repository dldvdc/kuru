package com.project.kuru

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.nio.file.Files
import java.nio.file.Path

@SpringBootApplication
class KuruApplication

fun main(args: Array<String>) {
	val sqliteFile = System.getenv("KURU_SQLITE_FILE") ?: "data/kuru.db"
	Path.of(sqliteFile).parent?.let { Files.createDirectories(it) }
	runApplication<KuruApplication>(*args)
}
