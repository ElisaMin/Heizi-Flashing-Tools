import me.heizi.gradle.dependencies
import me.heizi.gradle.implProj

dependencies(
    compose = true,
    composex = true
)
dependencies {
    implProj(":adb-helper")
    implProj(":apk-parser")
}