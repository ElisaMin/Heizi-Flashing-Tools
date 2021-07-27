plugins {
    kotlin("jvm")
}

group = "me.heizi.kotlinx"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":logger"))
    implementation("net.java.dev.jna:jna:5.8.0")
    implementation("net.java.dev.jna:jna-platform:5.8.0")
}
