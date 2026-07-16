plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
}

the<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension>().jvmToolchain(21)

dependencies {
    "implementation"("org.springframework.boot:spring-boot-starter-data-jpa")
    "implementation"("org.springframework.boot:spring-boot-starter-web")
    "implementation"("org.springframework.boot:spring-boot-starter-validation")
    "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
    "implementation"("org.jetbrains.kotlin:kotlin-reflect")
    "implementation"("org.flywaydb:flyway-core")
    "implementation"("org.flywaydb:flyway-database-postgresql")
    "implementation"("io.jsonwebtoken:jjwt-api:0.12.6")
    "runtimeOnly"("io.jsonwebtoken:jjwt-impl:0.12.6")
    "runtimeOnly"("io.jsonwebtoken:jjwt-jackson:0.12.6")
    "implementation"("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")
    "implementation"("org.springframework.security:spring-security-crypto")
    "implementation"("org.springframework.boot:spring-boot-starter-actuator")

    "runtimeOnly"("org.postgresql:postgresql")

    "testImplementation"("org.springframework.boot:spring-boot-starter-test")
    "testImplementation"("org.mockito.kotlin:mockito-kotlin:5.4.0")
    "testImplementation"("com.h2database:h2")
}
