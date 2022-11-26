package me.heizi.flashing_tool.sideloader

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import me.heizi.flashing_tool.adb.ADBDevice
import me.heizi.flashing_tool.sideloader.InnerDeviceContextState.*


sealed class InnerDeviceContextState {
    object Unavailable:InnerDeviceContextState()
    object Reconnect:InnerDeviceContextState()
    object AndroidEvenRebootNeed:InnerDeviceContextState()
//    object RecoveryRebootNeed:InnerDeviceContextState()
    object SideloadRebootNeed:InnerDeviceContextState()
    object Connected:InnerDeviceContextState()
    companion object {
        @Composable
        fun clickedColor() =
            CardDefaults.cardColors(MaterialTheme.colorScheme.primary)
    }
}
val InnerDeviceContextState.isAvailable
    get() = this !is Unavailable

@Composable
fun InnerDeviceContextState.color():CardColors = when(this) {
    Connected, Unavailable ->
        MaterialTheme.colorScheme.secondaryContainer
    Reconnect, SideloadRebootNeed,AndroidEvenRebootNeed ->
        MaterialTheme.colorScheme.tertiaryContainer
}.let {
    CardDefaults.cardColors(it)
}

fun ADBDevice.DeviceState.context():InnerDeviceContextState = with(ADBDevice.DeviceState) {
    when(this@context) {
        // unavailable
        bootloader, notfound -> Unavailable
        // reconnect-able
        authorizing, host, offline -> Reconnect
        device -> if (isSideload) SideloadRebootNeed else Connected
        recovery -> if (isSideload) SideloadRebootNeed else AndroidEvenRebootNeed
        sideload -> if (isSideload) Connected else SideloadRebootNeed
        else -> Unavailable
    }

}
