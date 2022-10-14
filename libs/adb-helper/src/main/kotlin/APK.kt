package me.heizi.flashing_tool.adb

import java.awt.Image
import java.io.File
import java.nio.file.Path

fun getApk(path: String):File {
    TODO()
}

val File.isAPK get() = this is APK
abstract class APK(path:String): File(path) {
    abstract val pkgName:String
    abstract val appName:String
    abstract val icon: Image
    abstract val versionCode:String
    abstract val version:String

}
