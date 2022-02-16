import me.heizi.gradle.Libs
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
version = "1.0"
tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {

    manifest.attributes["Main-Class"] = "me.heizi.flashing_tool.image.Main"
}

compose.desktop {
    application {
        javaHome = "D:\\jdk\\jdk-14"
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats( org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe)
            packageName = "HeiziToolX"
            packageVersion = "1.0.0"
        }
    }
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