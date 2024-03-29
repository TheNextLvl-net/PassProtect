plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "net.thenextlvl.core"
version = "3.0.7"

java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.thenextlvl.net/releases")
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.30")
    compileOnly("org.jetbrains:annotations:24.0.0")
    compileOnly("net.thenextlvl.core:annotations:2.0.1")

    annotationProcessor("org.projectlombok:lombok:1.18.30")

    implementation("com.google.zxing:core:3.5.2")
    implementation("com.google.zxing:javase:3.5.2")
    implementation("com.google.guava:guava:33.0.0-jre")
    implementation("com.warrenstrange:googleauth:1.5.0")

    implementation("net.thenextlvl.crypto:aes:1.0.1")
    implementation("net.thenextlvl.core:utils:1.0.0")
    implementation("net.thenextlvl.core:files:1.0.3")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.github.bailuk:java-gtk:v0.4.0")
}

tasks.shadowJar {
    archiveFileName.set("pass-protect-$version.jar")
    minimize()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "net.thenextlvl.passprotect.PassProtect"
        attributes["Version"] = version
    }
}