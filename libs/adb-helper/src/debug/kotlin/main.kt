@file:JvmName("adbHelperDebug")
package debug.heizi.flashing_tool.adb

import me.heizi.flashing_tool.adb.ADBDevice

fun main() {
    println(ADBDevice.DeviceState("device"))
}