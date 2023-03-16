import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {

    implementation(kotlin("stdlib"))
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

gradlePlugin {
    plugins {
        register("versions") {
            val info = libs.plugins.me.heizi.gradle.controller.version.get()
            id = info.pluginId
            version = info.version.requiredVersion
            implementationClass = "me.heizi.gradle.controller.versions.Versions"
        }
    }
}
tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn", "-Xcontext-receivers", "-Xskip-prerelease-check")
    }
}
