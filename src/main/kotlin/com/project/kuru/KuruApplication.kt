package com.project.kuru

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KuruApplication

fun main(args: Array<String>) {
	runApplication<KuruApplication>(*args)
}
