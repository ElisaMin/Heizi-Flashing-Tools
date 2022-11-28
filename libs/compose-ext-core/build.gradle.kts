import me.heizi.gradle.dependencies
import me.heizi.gradle.versions


dependencies(
    reflect = true,
    apkParser = true
)
dependencies {
    implementation("androidx.collection:collection-ktx:${rootProject.versions["androidx.collection"]}")
}