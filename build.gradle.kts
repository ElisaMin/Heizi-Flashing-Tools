@file:Suppress("UnstableApiUsage")

import me.heizi.gradle.versions
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


allprojects {
    group =
        if (this.projectDir.toPath().parent.fileName.toString() == "libs")
            "me.heizi.kotlinx"
        else "me.heizi.flashing_tool"
    version = versions["HFT"]

    apply (plugin = "org.jetbrains.kotlin.jvm")
    apply (plugin = "com.github.johnrengelman.shadow")

    repositories {
        mavenCentral()
        google()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        maven { url = uri("https://raw.githubusercontent.com/ElisaMin/Maven/master/")}

    }
    configure<SourceSetContainer> {
        val main by getting
        create("debug") {
            compileClasspath+=main.compileClasspath
            runtimeClasspath+=main.runtimeClasspath
            compileClasspath+=main.output
            runtimeClasspath+=main.output
            resources+main.resources
        }
    }
    configure<JavaPluginExtension>() {
        registerFeature("debug") {
            usingSourceSet(sourceSets["debug"])
            disablePublication()
        }
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn","-Xcontext-receivers","-Xskip-prerelease-check")
        }
    }
    tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
//        minimize()
        manifest.attributes["Manifest-Version"] = rootProject.versions["HFT"]
    }
}
@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
subprojects {
    with(ComposePlugin.Dependencies) {
        ext["composeDependencies"] = arrayOf(material3, desktop.currentOs)
    }
    apply(plugin ="org.jetbrains.compose")
}
plugins {
    kotlin("jvm") apply false
    id("org.jetbrains.compose")
    id("com.github.johnrengelman.shadow") apply false
}




tasks.getByName("build").dependsOn("clean")


