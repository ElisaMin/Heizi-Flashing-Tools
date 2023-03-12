package me.heizi.gradle.controller.versions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*
import java.util.*
import kotlin.reflect.KProperty


context(Project)
fun DependencyHandlerScope.use(usage: DependencyUsage)= with(usage){
    val versions = rootProject.versions
    implementation(kotlin("stdlib"))

    if (khell) {
        if (!coroutine) coroutine = true
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
    if (reflect) implementation(kotlin("reflect"))
    if(compose) (ext["composeDependencies"] as Array<*>).forEach {
        implementation(it as String)
    }
    if (apkParser) {
        implementation("me.heizi.apk.parser:compose-desktop-ext:${versions["apk-parser"]}")
        implementation("net.dongliu.apk.parser:apk-parser:${versions["apk-parser"]}")
    }
}
class Versions: Plugin<Project> {
    override fun apply(project: Project):Unit = with(project) {
        with(gradle) {
            beforeProject {
                extensions.create("dependencies",DependencyUsage::class.java)
            }
            afterProject {
                val usage = runCatching { extensions["dependencies"] as DependencyUsage }
                    .getOrNull()
                if (usage != null) {
                    dependencies {
                        use(usage)
                    }
                } else println("no dependencies found. skip")
            }
        }
    }
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
//context(Project)
//inline operator fun <reified T> Class<T>.invoke(block: T.() -> Unit) {
//    tasks.withType<T>()
//    tasks.withType(this){
//        block(this)
//    }
//}


//val Project.props get() = object : Getter<Properties> {
//    override fun get(key: String): Properties = Properties().apply {
//        file("$key.properties").inputStream().use(::load)
//    }
//}
//val ExtensionAware.prop: VersionControl get() = object : VersionControl {
//    override val extraPropertiesExtension: ExtraPropertiesExtension
//        get() = extensions.extraProperties
//
//    override fun get(key: String): String
//            = extraPropertiesExtension[key] as String
//}

operator fun <T> Property<T>.setValue (thisRef:Any?, prop: KProperty<*>, value :T) {
    set(value)
}

val ExtensionAware.versions
    get() = object : VersionControl {
        override val extraPropertiesExtension: ExtraPropertiesExtension
            get() = extensions.extraProperties
    }
//operator fun ExtraPropertiesExtension.get(string: String):String
//    = get(string+".version") as String

interface VersionControl:Getter<String> {
    val extraPropertiesExtension: ExtraPropertiesExtension
    override fun get(key:String):String =
        extraPropertiesExtension["$key.version"] as String
}
interface Getter<T> {
    operator fun get(key:String):T
}


val Project.ext: ExtraPropertiesExtension get() =
    (this as ExtensionAware).extensions.getByName("ext") as ExtraPropertiesExtension

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

