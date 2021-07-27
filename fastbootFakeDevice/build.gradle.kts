import org.jetbrains.compose.compose

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "me.heizi.flashing_tool"
version = "1.0"

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":khell"))
    implementation(project(":logger"))
    implementation(project(":fileDialog"))
    implementation(project(":compose.desktopx.core"))
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3-native-mt")
    implementation(kotlin("reflect"))
}


tasks.test {
    useJUnit()
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