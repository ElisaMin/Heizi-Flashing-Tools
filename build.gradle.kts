import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins { libs.run {
    val apply = true
    plugins.run {
        arrayOf(
            org.jetbrains.kotlin.jvm to apply.not(),
            me.heizi.gradle.controller.version to apply,
            org.jetbrains.compose to apply,
            com.github.ben.manes.versions to apply,
            com.github.johnrengelman.shadow to apply.not(),
            nl.littlerobots.version.catalog.update to apply,
        ).forEach { (dependent,enabled) ->
            alias(dependent) apply enabled
        }
    }
} }

// defined the info of the project
allprojects {
    version = rootProject.libs.versions.heizi.flash.tools.get()
    group = when (this.projectDir.toPath().parent.fileName.toString()) {
        "libs" -> "me.heizi.kotlinx"
         else -> "me.heizi.flashing_tool"
    }
    repositories {
        mavenCentral()
        google()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        maven { url = uri("https://raw.githubusercontent.com/ElisaMin/Maven/master/")}
    }
}
// config shadow jar
allprojects {
    apply (plugin = "com.github.johnrengelman.shadow")
    tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        // minimize()
        manifest.attributes["Manifest-Version"] = rootProject.version
    }
}
// config kotlin
allprojects {
    apply (plugin = "org.jetbrains.kotlin.jvm")
    configure<KotlinProjectExtension> {
        jvmToolchain(19)
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn", "-Xcontext-receivers", "-Xskip-prerelease-check")
        }
    }
}
// debug sources set
allprojects {
    apply (plugin = "org.jetbrains.kotlin.jvm")
    configure<SourceSetContainer> {
        val main by getting
        create("debug") {
            compileClasspath+=(main.compileClasspath+main.output)
            runtimeClasspath+=(main.runtimeClasspath+main.output)
            resources+main.resources
        }
    }
    configure<JavaPluginExtension> {
        registerFeature("debug") {
            usingSourceSet(sourceSets["debug"])
            disablePublication()
        }
    }
}
// config compose
@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
subprojects {
    apply(plugin ="org.jetbrains.compose")
    with(ComposePlugin.Dependencies(this)) {
        ext["composeDependencies"] = arrayOf(material3, desktop.currentOs)
    }
}

versionCatalogUpdate {
    catalogFile.set(rootProject.file("gradle/libs.versions.toml"))
}


tasks.getByName("build").dependsOn("clean")