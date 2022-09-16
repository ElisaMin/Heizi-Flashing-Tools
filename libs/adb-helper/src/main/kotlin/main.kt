package me.heizi.flashing_tool.adb

import me.heizi.kotlinx.logger.debug
import me.heizi.kotlinx.shell.Shell
import java.awt.Image
import java.io.File

interface ADB {
    val devices:List<ADBDevice>
}
interface ADBDevice {
    val info:DeviceInfo
    var isConnected:Boolean?
    suspend fun execute(vararg command: String):Shell
    suspend infix fun shell(command:String)
        = execute("shell",command)
    suspend infix fun reboot(mode:DeviceMode)
        = execute("reboot",mode.rebootTo)
    suspend fun install(
        apk:APK,
        isReplaceExisting:Boolean = false,
        isTestAllow:Boolean = false,
        isDebugAllow:Boolean = false,
        isGrantAllPms:Boolean = false,
    ) = buildList {
        add("install")
        if (isTestAllow) add("-t")
        if (isDebugAllow) add("-d")
        if (isGrantAllPms) add("-g")
        add(apk.absolutePath)
    }.toTypedArray().let {
        execute(*it)
    }
    suspend fun disconnect()
        = execute("disconnect")
}

enum class DeviceMode(val rebootTo:String) {
    Android(""),
    Recovery("Recovery"),
    Sideload("sideload"),
    AutoRebootSideload("sideload-auto-reboot"),
    Bootloader("bootloader")
}

abstract class APK(path:String): File(path) {
    abstract val pkgName:String
    abstract val appName:String
    abstract val icon:Image
    abstract val versionCode:String
    abstract val version:String
}
interface  DeviceInfo {
    val serialId:String
    val state:String
}