plugins {
    kotlin("jvm")
}

group = "me.heizi.kotlinx"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3-native-mt")
    implementation("org.slf4j:slf4j-api:1.7.9")
    implementation("org.slf4j:slf4j-log4j12:1.7.9")
//    implementation("org.apache.logging.log4j:log4j-core:2.14.1")
//    implementation("org.apache.logging.log4j:log4j-bom:2.14.1")
//    implementation("org.apache.logging.log4j:log4j-api:2.14.1")
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test-junit"))
}
