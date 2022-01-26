import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
    kotlin("plugin.jpa") version "1.6.10"
    kotlin("kapt") version "1.4.32"
    kotlin("plugin.serialization") version "1.6.10"
}

group = "ai.logsight"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server") //
//    implementation("org.springframework.boot:spring-boot-starter-security") //
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(group = "org.springframework.boot", name = "spring-boot-starter-mail")
    implementation(group = "com.auth0", name = "java-jwt", version = "3.13.0")
    implementation("org.springframework.data:spring-data-elasticsearch:4.3.1") //
//    implementation("org.liquibase:liquibase-core")
    kapt("org.springframework.boot:spring-boot-configuration-processor")/**/
    implementation("org.json:json:20160810")

    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.junit.jupiter:junit-jupiter:5.8.2")
    implementation("junit:junit:4.13.2")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.integration:spring-integration-zeromq:5.5.7")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
//    testImplementation("org.springframework.security:spring-security-test")
    implementation("org.springdoc:springdoc-openapi-webmvc-core:1.6.3")
    testImplementation("io.mockk:mockk:1.12.2")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf:2.6.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
