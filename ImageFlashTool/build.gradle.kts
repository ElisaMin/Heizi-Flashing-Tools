
import me.heizi.gradle.Libs
import me.heizi.gradle.Versions
import org.jetbrains.compose.compose

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("com.github.johnrengelman.shadow")
}

repositories {
    google()
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":khell"))
    implementation(project(":logger"))
    implementation(project(":compose.desktopx.core"))
    implementation(compose.desktop.currentOs)
    implementation(Libs.Decompose)
    implementation(Libs.M3)
    implementation(Libs.Coroutine)
}

group = "me.heizi.flashing_tool.image"
version = Versions.HFT
tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {

    manifest.attributes["Main-Class"] = "me.heizi.flashing_tool.image.Main"
}


tasks.test {
    useJUnit()
}


val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks

compileKotlin.kotlinOptions {
    jvmTarget = "11"
    freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
}
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}

tasks.getByName("build").dependsOn("clean")