

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

files("libs","tools").forEach { it
    .listFiles()
    ?.filter { it.isDirectory }
    ?.forEach { dir ->
        include(dir.name)
        project(":"+dir.name).projectDir = dir
    }
}

//
//include("libs:compose-ext-core")
//include("libs:native-file-dialog")
//include("tools:fastboot-manager")
//include("tools:image-install-wizard")
// ?.name 
//     = "compose.ext.core"
// findProject(":nativeFileDialog")?.name 
//     = "compose.ext.file_dialog"
// include("tools/")
// include("fastbootFakeDevice")
// include("nativeFileDialog")
// include("utils")
