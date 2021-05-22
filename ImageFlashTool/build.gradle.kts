import org.jetbrains.compose.compose

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version "0.3.1"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "me.heizi.flashing_tool.image"
version = "1.0"

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":khell"))
    implementation(project(":logger"))
    implementation(project(":compose.desktopx.core"))
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3-native-mt")
}
val shadowJar: com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar by tasks
shadowJar.apply {
    manifest.attributes["Main-Class"] = "MainKt"
}

compose.desktop {
    application {
        javaHome = "D:\\jdk\\jdk-14"
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats( org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe)
            packageName = "HeiziToolX"
            packageVersion = "1.0.0"
        }
    }
}


tasks.test {
    useJUnit()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}


val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks

compileKotlin.kotlinOptions {
    jvmTarget = "11"
    freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
}
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}

tasks.getByName("build").dependsOn("clean")