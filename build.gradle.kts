plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.1"
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.3.21"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("net.portswigger.burp.extensions:montoya-api:2025.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.miglayout:miglayout-swing:11.3")
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}

tasks.jar {
    enabled = false
}

tasks.shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()
}
kotlin {
    jvmToolchain(21)
}