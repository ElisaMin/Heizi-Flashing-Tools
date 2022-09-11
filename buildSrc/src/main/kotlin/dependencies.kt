package me.heizi.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin


fun Project.dependencies(
    decompose: Boolean = false,
    jna: Boolean = false,
    coroutine: Boolean = false,
    log: Boolean = false,
    khell: Boolean = false,
    composex: Boolean = false,
    fileDialog:Boolean = false,
    reflect: Boolean = false
): Unit = this.dependencies {

    val versions =  rootProject.versions

    implementation(kotlin("stdlib"))
//    implementation("org.jetbrains.compose.material3:material3:${versions["compose"]}")
    if (khell) {
        if (!coroutine) dependencies(coroutine=true)
        implementation("me.heizi.kotlinx:khell:${versions["khell"]}")
    }
    if (log) {
        api("me.heizi.kotlinx:khell-log:${versions["khell"]}")
        implementation("org.slf4j:slf4j-log4j12:${versions["slf4j"]}")
    }
    if (coroutine)
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions["coroutines"]}")
    if (decompose){
        implementation("com.arkivanov.decompose:decompose-jvm:${versions["decompose"]}")
        implementation("com.arkivanov.decompose:extensions-compose-jetbrains:${versions["decompose"]}")
    }

    if (jna) {
        implementation("net.java.dev.jna:jna:${versions["jna"]}")
        implementation("net.java.dev.jna:jna-platform:${versions["jna"]}")
    }
    if (composex)
        implementation(project(":compose.desktopx.core"))
    if (fileDialog)
        implementation(project(":fileDialog"))
    if (reflect)
        implementation(kotlin("reflect"))

}

fun DependencyHandler.implementation(dependencyNotation: Any): Dependency? =
    add("implementation", dependencyNotation)

fun DependencyHandler.api(dependencyNotation: Any): Dependency? =
    add("api", dependencyNotation)

