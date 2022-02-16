import me.heizi.gradle.Libs
import me.heizi.gradle.Versions
plugins {
    kotlin("jvm")
}

group = "me.heizi.kotlinx"
version = Versions.HFT

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":logger"))
    implementation(Libs.JNA.self)
    implementation(Libs.JNA.platform)
}
