import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("base")
    id("org.springframework.boot") version "2.4.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.4.31"
    kotlin("plugin.spring") version "1.4.31"
    kotlin("plugin.jpa") version "1.4.31"
    kotlin("plugin.serialization") version "1.4.31"
}

group = "com.loxbear"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch:2.5.4")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:2.5.4")
    implementation("org.springframework.boot:spring-boot-starter-web:2.5.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.5")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.12.5")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.5")
    implementation("com.fasterxml.jackson.core:jackson-core:2.12.5")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.12.5")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.12.5")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.boot:spring-boot-starter-security:2.5.4")
    runtimeOnly("org.postgresql:postgresql:42.2.23.jre7")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.5.4")
    implementation(group = "org.json", name = "json", version = "20201115")
    implementation(group = "org.springframework.kafka", name = "spring-kafka", version = "2.7.2")
    implementation(group = "org.springframework.boot", name = "spring-boot-starter-mail")
    implementation(group = "com.auth0", name = "java-jwt", version = "3.13.0")
    implementation(group = "com.google.code.gson", name = "gson", version = "2.8.6")
    implementation(group = "com.stripe", name = "stripe-java", version = "20.56.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf:2.5.4")
    implementation ("com.konghq:unirest-java:3.11.09")
    implementation("io.springfox:springfox-swagger2:2.9.2")
    implementation("io.springfox:springfox-bean-validators:2.9.2")
    implementation("io.springfox:springfox-swagger-ui:2.9.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
