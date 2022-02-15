import org.jetbrains.compose.compose

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "me.heizi.kotlinx"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":logger"))
    implementation(compose.desktop.currentOs)
    implementation(kotlin("reflect"))
}

tasks.getByName("build").dependsOn("clean")
