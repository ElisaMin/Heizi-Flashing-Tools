plugins {
    kotlin("jvm")
}

group = "me.heizi.kotlinx"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3-native-mt")
    implementation(project(":logger"))
    implementation(kotlin("stdlib"))
}
