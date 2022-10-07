import me.heizi.gradle.dependencies
import me.heizi.gradle.implProj
import me.heizi.gradle.implementation

dependencies(
    compose = true,
)
dependencies {
    implProj(":adb-helper")
}