
import me.heizi.gradle.dependencies
import me.heizi.gradle.versions


group = "me.heizi.kotlinx"
version = versions["HFT"]


dependencies(
    reflect = true,
)

tasks.getByName("build").dependsOn("clean")
