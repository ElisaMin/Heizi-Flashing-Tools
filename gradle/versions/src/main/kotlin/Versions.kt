package me.heizi.gradle.controller.versions

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.kotlin.dsl.*


fun DependencyHandler.implProj(path: String) {
    val project = project(mutableMapOf("path" to path)) as ProjectDependency
    implementation(project)
    debugImplementation(project.copy().apply {
        capabilities {
            requireCapability("$group:$name-debug")
        }
    })
}

open class DependencyUsage {
    var decompose: Boolean = false
    var jna: Boolean = false
    var coroutine: Boolean = false
    var log: Boolean = false
    var khell: Boolean = false
    var composex: Boolean = false
    var fileDialog:Boolean = false
    var reflect: Boolean = false
    var compose: Boolean =true
    var apkParser: Boolean =false
}

class Versions: Plugin<Project> {
    override fun apply(project: Project):Unit = with(project) {
        with(gradle) {
            beforeProject {
                extensions.create("dependenciesUsage",DependencyUsage::class.java)
            }
            afterProject {
                val usage = runCatching { extensions["dependenciesUsage"] as DependencyUsage }
                    .getOrNull()
                if (usage != null) {
                    dependencies {
                        use(usage)
                    }
                } else logger.info("no dependencies found. skip")
            }
        }
    }
}

context(Project)
private fun DependencyHandlerScope.use(usage: DependencyUsage)= with(usage){ with(rootProject.the<LibrariesForLibs>()) {


    implementation(kotlin("stdlib"))

    if (khell) {
        if (!coroutine) coroutine = true
        implementation(me.heizi.kotlinx.khell.asProvider())
    }
    if (log) {
        api(me.heizi.kotlinx.khell.log)
        runtimeOnly(org.slf4j.slf4j.log4j12)
    }
    if (coroutine)
        implementation(org.jetbrains.kotlinx.kotlinx.coroutines.core)

    if (decompose) com.arkivanov.decompose.run {
        implementation(decompose.jvm)
        implementation(extensions.compose.jetbrains)
    }

    if (jna) net.java.dev.jna.run {
        implementation(jna.asProvider())
        implementation(jna.platform)
    }
    if (composex)
        implementation(project(":compose-ext-core"))
    if (fileDialog)
        implementation(project(":native-file-dialog"))
    if (reflect) implementation(kotlin("reflect"))
    if(compose) (ext["composeDependencies"] as Array<*>).forEach {
        implementation(it as String)
    }
    if (apkParser) {
        implementation(me.heizi.apk.parser.compose.desktop.ext)
        implementation(net.dongliu.apk.parser.apk.parser)
    }
} }

private val Project.ext: ExtraPropertiesExtension get() =
    (this as ExtensionAware).extensions.getByName("ext") as ExtraPropertiesExtension


private fun DependencyHandler.debugImplementation(dependency: Any)
        = add("debugImplementation", dependency)
private fun DependencyHandler.runtimeOnly(dependencyNotation: Any): Dependency? =
    add("runtimeOnly", dependencyNotation)
//fun DependencyHandler.compileOnly(dependencyNotation: Any): Dependency? =
//    add("compileOnly", dependencyNotation)
private fun DependencyHandler.implementation(dependencyNotation: Any): Dependency? =
    add("implementation", dependencyNotation)

private fun DependencyHandler.api(dependencyNotation: Any): Dependency? =
    add("api", dependencyNotation)

