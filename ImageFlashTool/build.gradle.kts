
import me.heizi.gradle.dependencies
import me.heizi.gradle.versions


dependencies(
    coroutine = true,
    reflect = true,

    log = true,
    khell = true,
    composex = true,
    fileDialog = true,

    decompose = true,
)

group = "me.heizi.flashing_tool.image"
version = versions["HFT"]

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest.attributes["Main-Class"] = "me.heizi.flashing_tool.image.Main"
}


