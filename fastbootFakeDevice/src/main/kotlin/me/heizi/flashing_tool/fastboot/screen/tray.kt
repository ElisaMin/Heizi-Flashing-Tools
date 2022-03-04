package me.heizi.flashing_tool.fastboot.screen


import androidx.compose.foundation.layout.Row
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.TrayState
import me.heizi.flashing_tool.fastboot.Resources
import me.heizi.flashing_tool.fastboot.repositories.Fastboot
import javax.imageio.ImageIO

abstract class TraysViewModel {
    val state = TrayState()
    val connectedState = mutableStateOf(false)
    private val loopActiveState = mutableStateOf(true)
//    var error by mutableStateOf("")
    var enableStart by loopActiveState
    abstract fun exit()
    abstract fun onTrayIconSelected()
    abstract fun onStopCollecting()
    abstract fun onStartCollecting()
}

abstract class Trays(
    private val applicationScope: ApplicationScope,

): TraysViewModel() {


    @Composable
    fun Render() {
        error()
        val devices by Fastboot.deviceSerials.collectAsState()
        connectedState.value = devices.isNotEmpty()
        Trays(applicationScope)
    }
    @OptIn(ExperimentalMaterialApi::class)
    @Composable fun error() {
        val error = Fastboot.error
//        var error by remember { this.error as MutableState<String>  }
        if (error.isNotEmpty()) AlertDialog(::exit,{
            Row {
                TextButton({Fastboot.error = ""}) {
                    Text("忽略")
                }
                Button(::exit) {
                    Text("关闭")
                }
            }
        }, title = {Text("错误!")}, text = { Text(error) })
    }

    final override fun onStartCollecting() {
        Fastboot.isScanning = true
        enableStart = false
    }

    final override fun onStopCollecting() {
        Fastboot.isScanning = false
        enableStart = true
    }
}

@Composable
private fun TraysViewModel.Trays(applicationScope: ApplicationScope) {


    val trayState = remember { state }
    val isConnected by remember { connectedState }
    val iconTray = ImageIO.read( if (isConnected) Resources.Urls.connected else Resources.Urls.disconnect).toPainter()
    val tooltip = if (isConnected) "设备已连接" else "Fastboot未连接设备"

    applicationScope.Tray(
        state = trayState, icon = iconTray,tooltip = tooltip, onAction = ::onTrayIconSelected
    ) {

        Item("Start Collection", enabled = !enableStart) {
            onStartCollecting()
        }
        Item("Stop Collecting", enabled = enableStart) {
            onStopCollecting()
        }
        Item("exit"){ exit() }
    }
}