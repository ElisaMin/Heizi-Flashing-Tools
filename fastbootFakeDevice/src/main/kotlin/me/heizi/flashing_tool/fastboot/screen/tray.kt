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
import me.heizi.flashing_tool.fastboot.screen.Trays.Companion.isRunning
import javax.imageio.ImageIO

abstract class TraysViewModel {
    val state = TrayState()
    val connectedState = mutableStateOf(false)
    // FIXME: 2022/3/5  不可更新
    var enableStart by mutableStateOf(true)
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
        isRunning.value = true
    }

    final override fun onStopCollecting() {
        Fastboot.isScanning = false
        isRunning.value = false
    }
    companion object {
        var isRunning = mutableStateOf(true)
    }
}

@Composable
private fun TraysViewModel.Trays(applicationScope: ApplicationScope) {
    val isConnected by remember { connectedState }
    val iconTray = ImageIO.read( if (isConnected) Resources.Urls.connected else Resources.Urls.disconnect).toPainter()
    val tooltip = if (isConnected) "设备已连接" else "Fastboot未连接设备"

    applicationScope.Tray(
        state = state, icon = iconTray,tooltip = tooltip, onAction = ::onTrayIconSelected
    ) {

        Item("启动设备监听", enabled = !isRunning.value) {
            onStartCollecting()
        }
        Item("停止监听设备", enabled = isRunning.value) {
            onStopCollecting()
        }
        Item("退出软件"){ exit() }
    }
}