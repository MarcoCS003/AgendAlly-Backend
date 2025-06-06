plugins {
    kotlin("jvm") version "1.9.22"
    id("io.ktor.plugin") version "2.3.7"
    kotlin("plugin.serialization") version "1.9.22"
    application
}

group = "com.academically"
version = "0.0.1"

application {
    mainClass.set("com.academically.ApplicationKt")
}

repositories {
    mavenCentral()
}

// Configurar Java toolchain para Java 11
kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:2.3.7")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.7")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // Content Negotiation & Serialization
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")

    // CORS & Headers
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-default-headers-jvm")

    // Base de datos - Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:0.45.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.45.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.45.0")

    // H2 Database (para empezar local y simple)
    implementation("com.h2database:h2:2.2.224")

    // PostgreSQL (para más adelante)
    implementation("org.postgresql:postgresql:42.7.6")

    // Logging
    implementation("ch.qos.logback:logback-classic:$1.5.18")
    testImplementation("org.testng:testng:7.1.0")
    testImplementation("junit:junit:4.13.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")


}