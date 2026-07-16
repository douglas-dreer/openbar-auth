plugins {
    kotlin("jvm") version "2.1.10" apply false
    kotlin("plugin.spring") version "2.1.10" apply false
    kotlin("plugin.jpa") version "2.1.10" apply false
    id("org.springframework.boot") version "3.4.7" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("org.sonarqube") version "7.3.1.8318" apply false
}

allprojects {
    group = "com.openbar"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.sonarqube")
    apply(plugin = "jacoco")

    the<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension>().jvmToolchain(21)

    dependencies {
        "implementation"("org.springframework.boot:spring-boot-starter-data-jpa")
        "implementation"("org.springframework.boot:spring-boot-starter-web")
        "implementation"("org.springframework.boot:spring-boot-starter-validation")
        "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")
        "implementation"("org.flywaydb:flyway-core")
        "implementation"("org.flywaydb:flyway-database-postgresql")

        "runtimeOnly"("org.postgresql:postgresql")

        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("org.mockito.kotlin:mockito-kotlin:5.4.0")
        "testImplementation"("com.h2database:h2")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        finalizedBy("jacocoTestReport")
    }

    tasks.named<JacocoReport>("jacocoTestReport") {
        dependsOn("test")
    }
}
