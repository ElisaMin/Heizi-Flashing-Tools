

group = "me.heizi.flashing_tool"
version = "1.0"

plugins {
    kotlin("jvm") version "1.5.21"
    id("org.jetbrains.compose") version "0.5.0-build262"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}
repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}