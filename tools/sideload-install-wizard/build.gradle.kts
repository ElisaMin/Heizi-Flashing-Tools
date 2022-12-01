import me.heizi.gradle.dependencies
import me.heizi.gradle.implProj

dependencies(
    compose = true,
    composex = true,
    apkParser = true,
    khell = true
)
dependencies {
    implProj(":adb-helper")
    implProj(":apk-parser")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest.attributes["Main-Class"] = "me.heizi.flashing_tool.sideloader.Main"
}

