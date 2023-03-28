import me.heizi.gradle.controller.versions.*

configure<DependencyUsage> {
    compose = true
    composex = true
    apkParser = true
    khell = true
    monet = true
    jna = true
    windows11Styler = true
}

dependencies {
    implProj(":adb-helper")
    implProj(":apk-parser")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest.attributes["Main-Class"] = "me.heizi.flashing_tool.sideloader.Main"
}

