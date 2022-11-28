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
    // its wireless
    object Unconnected:InnerDeviceContextState()
    object Connected:InnerDeviceContextState()
    companion object {
        @Composable
        fun clickedColor() =
            CardDefaults.cardColors(colors.current.primary)
    }
}
val InnerDeviceContextState.isAvailable
    get() = this !is Unavailable

@Composable
fun InnerDeviceContextState.color():CardColors = when(this) {
    Connected, Unavailable, ->
        colors.current.secondaryContainer
    Reconnect, SideloadRebootNeed,AndroidEvenRebootNeed,Unconnected ->
        colors.current.tertiaryContainer
}.let {
    CardDefaults.cardColors(it)
}
val ADBDevice.isContextConnected get() = state.toContext() is Connected
fun ADBDevice.DeviceState.toContext() = with(ADBDevice.DeviceState) {
    when(this@toContext) {
        // unavailable
        bootloader, notfound -> Unavailable
        // reconnect-able
        authorizing, offline -> Reconnect
        host-> Unconnected
        device -> if (isSideload) SideloadRebootNeed else Connected
        recovery -> if (isSideload) SideloadRebootNeed else AndroidEvenRebootNeed
        sideload -> if (isSideload) Connected else SideloadRebootNeed
        else -> Unavailable
    }

}

@Composable
fun ADBDevice.DeviceState.context():InnerDeviceContextState = toContext()