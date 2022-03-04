import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "me.heizi.flashing_tool"
version = me.heizi.gradle.Versions.HFT



kotlin {
    sourceSets.all {
        languageSettings.languageVersion = "1.6"
    }
}
allprojects {
    repositories {
        mavenCentral()
        google()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
        }
    }
}

plugins {
    kotlin("jvm") version me.heizi.gradle.Versions.kotlin
    id ("org.jetbrains.compose") version  me.heizi.gradle.Versions.compose
    id("com.github.johnrengelman.shadow") version "7.0.0"
}
tasks.getByName("build").dependsOn("clean")

