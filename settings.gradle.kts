

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
    plugins {

        fun get(key:String) :String
                = extra["$key.version"] as String
        kotlin("jvm") version get("kotlin")
        id ("org.jetbrains.compose") version get("compose")
        id("com.github.johnrengelman.shadow") version get("shadowJar")
    }
}

rootProject.name = "HeiziToolX"

include("khell")
include("logger")
include("ImageFlashTool")
include("compose.desktopx.core")
include("fastbootFakeDevice")
include("nativeFileDialog")
findProject(":nativeFileDialog")?.name = "fileDialog"
include("utils")
