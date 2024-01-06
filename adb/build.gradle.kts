plugins {
    id("java")
}

group = "net.thenextlvl.core"
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
    compileOnly("net.thenextlvl.core:annotations:1.0.0")
    compileOnly("org.jetbrains:annotations:24.0.0")
    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")
}