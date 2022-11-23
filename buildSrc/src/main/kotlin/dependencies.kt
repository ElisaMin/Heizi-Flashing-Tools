package me.heizi.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
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
    reflect: Boolean = false,
    compose: Boolean =true,
    apkParser: Boolean =false,
): Unit = dependencies {

    val versions =  rootProject.versions

    implementation(kotlin("stdlib"))

    if (khell) {
        if (!coroutine) dependencies(coroutine=true)
        implementation("me.heizi.kotlinx:khell:${versions["khell"]}")
    }
    if (log) {
        api("me.heizi.kotlinx:khell-log:${versions["khell"]}")
        runtimeOnly("org.slf4j:slf4j-log4j12:${versions["slf4j"]}")
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
        implementation(project(":compose-ext-core"))
    if (fileDialog)
        implementation(project(":native-file-dialog"))
    if (reflect)
        implementation(kotlin("reflect"))
    if(compose) (ext["composeDependencies"] as Array<*>).forEach {
        implementation(it as String)
    }
    if (apkParser) {
        implementation("me.heizi.apk.parser:compose-desktop-ext:${versions["apk-parser"]}")
        implementation("net.dongliu.apk.parser:apk-parser:${versions["apk-parser"]}")
    }

}

val Project.ext: org.gradle.api.plugins.ExtraPropertiesExtension get() =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("ext") as org.gradle.api.plugins.ExtraPropertiesExtension

fun DependencyHandler.implProj(path: String) {
    val project = project(mutableMapOf("path" to path)) as ProjectDependency
    implementation(project)
    debugImplementation(project.copy().apply {
        capabilities {
            requireCapability("$group:$name-debug")
        }
    })
}

fun DependencyHandler.debugImplementation(dependency: Any)
        = add("debugImplementation", dependency)
fun DependencyHandler.runtimeOnly(dependencyNotation: Any): Dependency? =
    add("runtimeOnly", dependencyNotation)
//fun DependencyHandler.compileOnly(dependencyNotation: Any): Dependency? =
//    add("compileOnly", dependencyNotation)
fun DependencyHandler.implementation(dependencyNotation: Any): Dependency? =
    add("implementation", dependencyNotation)

fun DependencyHandler.api(dependencyNotation: Any): Dependency? =
    add("api", dependencyNotation)