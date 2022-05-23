import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    id("org.springframework.boot") version "2.5.7"
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
//    implementation("org.springdoc:springdoc-openapi-data-rest:1.6.3")

    implementation("org.springframework.integration:spring-integration-zeromq:5.5.11")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server") //
//    implementation("org.springframework.boot:spring-boot-starter-security") //
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    implementation(group = "org.springframework.boot", name = "spring-boot-starter-mail")
    implementation(group = "com.auth0", name = "java-jwt", version = "3.13.0")
    implementation("org.springframework.data:spring-data-elasticsearch:4.3.4")
//    implementation("org.liquibase:liquibase-core")
    implementation("com.h2database:h2:2.1.212")
//    implementation("org.liquibase:liquibase-core")
    kapt("org.springframework.boot:spring-boot-configuration-processor")/**/
    implementation("org.json:json:20160810")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.junit.jupiter:junit-jupiter:5.8.2")
    implementation("org.junit.vintage:junit-vintage-engine:5.8.2")
    implementation("com.antkorwin:xsync:1.3")

    implementation("io.springfox:springfox-boot-starter:3.0.0")
    implementation("io.springfox:springfox-swagger-ui:3.0.0")

//    implementation("io.springfox:springfox-swagger2:2.9.2")
//    implementation("io.springfox:springfox-bean-validators:2.9.2")
//    implementation("io.springfox:springfox-swagger-ui:2.9.2")

    implementation("org.springframework:spring-test")
    implementation("org.springframework.data:spring-data-jpa")
    implementation("com.h2database:h2")
    implementation("org.hibernate:hibernate-core")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "com.vaadin.external.google", module = "android-json")
    }
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.springframework.security:spring-security-test")
    implementation("org.springdoc:springdoc-openapi-webmvc-core:1.6.8")
    testImplementation("io.mockk:mockk:1.12.3")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf:2.6.7")
    // testing dependencies
    testCompileOnly("org.mockito:mockito-junit-jupiter:4.5.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.mockito:mockito-inline:4.5.1")
    testCompileOnly("org.assertj:assertj-core:3.22.0")
    testImplementation(kotlin("test"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xuse-experimental=kotlinx.coroutines.ObsoleteCoroutinesApi")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
