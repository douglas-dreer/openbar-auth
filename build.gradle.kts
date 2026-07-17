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
    version = "0.2.0"

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

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.4.7")
        }
    }

    dependencies {
        "implementation"("org.springframework.boot:spring-boot-starter-data-jpa")
        "implementation"("org.springframework.boot:spring-boot-starter-web")
        "implementation"("org.springframework.boot:spring-boot-starter-validation")
        "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")
        "implementation"("org.flywaydb:flyway-core")
        "implementation"("org.flywaydb:flyway-database-postgresql")
        "implementation"("com.bucket4j:bucket4j_jdk17-core:8.19.0")

        "runtimeOnly"("org.postgresql:postgresql")

        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("org.mockito.kotlin:mockito-kotlin:5.4.0")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        finalizedBy("jacocoTestReport")
    }

    tasks.named<JacocoReport>("jacocoTestReport") {
        dependsOn("test")
        reports {
            xml.required.set(true)
        }
    }

    the<org.sonarqube.gradle.SonarExtension>().apply {
        properties {
            property("sonar.projectKey", "ob-${project.name}")
            property("sonar.projectName", "OPENBAR ${project.name.removePrefix("openbar-").replaceFirstChar { it.uppercase() }}")
            property("sonar.sources", "src/main/kotlin")
            property("sonar.tests", "src/test/kotlin")
            property("sonar.java.source", "21")
            property("sonar.kotlin.source", "src/main/kotlin")
            property("sonar.kotlin.test", "src/test/kotlin")
            property("sonar.junit.reportPaths", "build/test-results/test/")
            property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
            property("sonar.exclusions", "**/build/**,**/node_modules/**,**/.gradle/**")
            property("sonar.coverage.exclusions", "**/test/**,**/*Test.kt,**/*Tests.kt,**/config/**")
        }
    }
}
