plugins {
	kotlin("jvm") version "2.3.21"
	kotlin("plugin.spring") version "2.3.21"
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "bo.kuru"
version = "0.0.1-SNAPSHOT"
val kotlinLoggingVersion by extra("8.0.01")
val jdbiVersion by extra("3.52.0")
val awsSdkVersion by extra("2.42.33")
val ulidCreatorVersion by extra("5.2.4")

kotlin {
	jvmToolchain(25)
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

repositories {
	mavenCentral()
}

dependencies {

	implementation("org.springframework.boot:spring-boot-starter-webmvc")

	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("io.github.oshai:kotlin-logging-jvm:${kotlinLoggingVersion}")
	implementation("tools.jackson.module:jackson-module-kotlin")

	implementation("org.jdbi:jdbi3-spring5:${jdbiVersion}")
	implementation("org.jdbi:jdbi3-kotlin:${jdbiVersion}")
	implementation("org.jdbi:jdbi3-postgres:${jdbiVersion}")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("software.amazon.awssdk:s3:${awsSdkVersion}")
	implementation("com.github.f4b6a3:ulid-creator:${ulidCreatorVersion}")

	runtimeOnly("org.postgresql:postgresql")

	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
