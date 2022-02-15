pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }

}
rootProject.name = "HeiziToolX"

include("khell")
include("logger")
include("ImageFlashTool")
include("compose.desktopx.core")
include("fastbootFakeDevice")
include("nativeFileDialog")
findProject(":nativeFileDialog")?.name = "fileDialog"
