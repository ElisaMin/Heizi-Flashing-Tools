
import me.heizi.gradle.dependencies
import me.heizi.gradle.versions

group = "me.heizi.flashing_tool"
version = versions["HFT"]

dependencies(
    coroutine = true,
    reflect = true,

    log = true,
    khell = true,
    composex = true,
    fileDialog = true,

    decompose = true,
)

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest.attributes["Main-Class"] = "me.heizi.flashing_tool.fastboot.Main"
}


tasks.getByName("build").dependsOn("clean")