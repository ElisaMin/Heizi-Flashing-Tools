plugins {
    `kotlin-dsl`
}
repositories {
    mavenCentral()
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn","-Xskip-prerelease-check")
    }
}
