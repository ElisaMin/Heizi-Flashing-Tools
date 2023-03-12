import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}

gradlePlugin {
    plugins {
        register("versions") {
            id = "me.heizi.gradle.controller.version"
            implementationClass = "me.heizi.gradle.controller.versions.Versions"
            version = "0"
        }
    }
}
tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn", "-Xcontext-receivers", "-Xskip-prerelease-check")
    }
}
