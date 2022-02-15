import org.jetbrains.compose.compose

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("com.github.johnrengelman.shadow")
}

group = "me.heizi.flashing_tool"
version = "1.0"

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

dependencies {
    @Suppress("UNCHECKED_CAST")
    (project.extra.get("kotlinCoroutineDependency")!! as DependencyHandler.()->Unit)()
    implementation(kotlin("stdlib"))
    implementation(project(":khell"))
    implementation(project(":logger"))
    implementation(project(":fileDialog"))
    implementation(project(":compose.desktopx.core"))
    implementation(compose.desktop.currentOs)
    implementation(kotlin("reflect"))
//    implementation(kotlin("kotlinx-coroutines-core"))
}


tasks.test {
    useJUnit()
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest.attributes["Main-Class"] = "me.heizi.flashing_tool.vd.fb.Main"
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