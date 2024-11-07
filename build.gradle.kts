val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.0.20"
    application
}

group = "com.alvla.tcp_port_forwarder"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-network:$ktor_version")
    implementation("io.ktor:ktor-network-tls:$ktor_version")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}