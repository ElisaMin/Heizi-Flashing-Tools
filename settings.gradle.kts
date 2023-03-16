rootProject.name = "HeiziToolX"
includeBuild("gradle/versions")
//enableFeaturePreview ("VERSION_CATALOGS")
dependencyResolutionManagement {
    versionCatalogs {
        create("dependentic") {
            from(files("gradle/libs.versions.toml"))
        }
    }
}
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
    plugins {

        fun get(key:String) :String= extra["$key.version"] as String

        kotlin("jvm") version get("kotlin")
        id ("org.jetbrains.compose") version get("compose")
        id("com.github.johnrengelman.shadow") version get("shadowJar")
//        id("me.heizi.gradle.controller.version") version  get("HFT")
//        id("me.heizi.gradle.controller.version") version dependentic.
        id("com.github.ben-manes.versions") version  "0.41.0"
        id("nl.littlerobots.version-catalog-update") version  "0.8.0"
    }
}

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
