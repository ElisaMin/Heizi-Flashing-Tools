dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
        }
    }
}
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}
rootProject.name = "HeiziToolX"
includeBuild("gradle/versions")
files("libs","tools").forEach { it
    .listFiles()
    ?.filter { it.isDirectory }
    ?.forEach { dir ->
        if (File(dir,"build.gradle.kts").exists() ||
            File(dir,"settings.gradle.kts").exists()) {
            include(dir.name)
            project(":" + dir.name).projectDir = dir
        }
    }
}
