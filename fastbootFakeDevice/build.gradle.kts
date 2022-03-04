
import me.heizi.gradle.Libs
import me.heizi.gradle.Versions
import org.jetbrains.compose.compose

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("com.github.johnrengelman.shadow")
}

group = "me.heizi.flashing_tool"
version = Versions.HFT

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":khell"))
    implementation(project(":logger"))
    implementation(project(":fileDialog"))
    implementation(project(":compose.desktopx.core"))
    implementation(Libs.Coroutine)
    implementation(Libs.M3)
    implementation(compose.desktop.currentOs)
    implementation(kotlin("reflect"))
}


tasks.test {
    useJUnit()
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest.attributes["Main-Class"] = "me.heizi.flashing_tool.fastboot.Main"
}


tasks.getByName("build").dependsOn("clean")