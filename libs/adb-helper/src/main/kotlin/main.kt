package me.heizi.flashing_tool.adb

import me.heizi.kotlinx.shell.CommandResult

fun CommandResult.successMessageOrThrow():String {
    require(this is CommandResult.Success) {
        this as CommandResult.Failed
        "run command failed cuz $code : $ \n $errorMessage \n msg: $processingMessage  "
    }
    return this.message
}

enum class DeviceMode(val rebootTo:String) {
    Android(""),
    Recovery("Recovery"),
    Sideload("sideload"),
    AutoRebootSideload("sideload-auto-reboot"),
    Bootloader("bootloader")
}