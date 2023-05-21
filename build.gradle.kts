plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "net.thenextlvl.passprotect"
version = "3.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven("https://repo.thenextlvl.net/releases")
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.26")

    annotationProcessor("org.projectlombok:lombok:1.18.26")

    implementation("com.google.code.findbugs:jsr305:302")
    implementation("com.google.code.gson:gson:2.10")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.google.zxing:core:3.5.1")
    implementation("com.google.zxing:javase:3.5.1")

    implementation("com.github.weisj:darklaf-core:3.0.2")
    implementation("com.warrenstange:googleauth:1.5.0")

    implementation("net.thenextlvl.crypto:aes:1.0.0")
    implementation("net.thenextlvl.core:api:3.1.12")
    implementation("net.thenextlvl.core:annotations:1.0.0")
}

tasks.shadowJar {
    minimize()
}