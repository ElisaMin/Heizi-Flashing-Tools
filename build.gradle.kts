group = "me.heizi.flashing_tool"
version = "1.0"


val kcd:(DependencyHandlerScope.()->Unit) = {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
}

subprojects {
    this.ext["kotlinCoroutineDependency"] = kcd
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        targetCompatibility = "11"
    }
}
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
}

plugins {
    kotlin("jvm") version "1.5.31"
    id ("org.jetbrains.compose") version "1.0.0-beta5"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

