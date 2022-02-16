import me.heizi.gradle.Libs
import me.heizi.gradle.Versions
plugins {
    kotlin("jvm")
}

group = "me.heizi.kotlinx"
version = Versions.HFT

repositories {
    mavenCentral()
}

dependencies {
    implementation(Libs.Coroutine)
    implementation(Libs.SLF4J.Api)
    implementation(Libs.SLF4J.J12)
//    implementation("org.apache.logging.log4j:log4j-core:2.14.1")
//    implementation("org.apache.logging.log4j:log4j-bom:2.14.1")
//    implementation("org.apache.logging.log4j:log4j-api:2.14.1")
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test-junit"))
}
