import me.heizi.gradle.controller.versions.*

configure<DependencyUsage> {
    coroutine = true
    reflect = true

    log = true
    khell = true
    composex = true
    fileDialog = true

    decompose = true
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest.attributes["Main-Class"] = "me.heizi.flashing_tool.image.Main"
}


