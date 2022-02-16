@file:JvmName("Main")
package me.heizi.flashing_tool.vd.fb


import Resources
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlinx.coroutines.*
import me.heizi.flashing_tool.fastboot.screen.*
import me.heizi.flashing_tool.vd.fb.info.DeviceInfo
import me.heizi.kotlinx.logger.debug
import javax.imageio.ImageIO
import kotlin.coroutines.EmptyCoroutineContext


val scope = CoroutineScope(EmptyCoroutineContext)


@Composable
fun open(serialId:String) {
    val device = DeviceManagerViewModelImpl(serialId)
    (device.device as DeviceInfo) .refreshInfo {
        "opening".debug("refresh","done")
    }
    var isWindowOpen by remember { mutableStateOf(true) }
    "opening".debug("isOpen",isWindowOpen)
    if (isWindowOpen) DeviceManagerWindow(device) {
        openedDeviceDialog.remove(serialId)
        isWindowOpen = false
    }
}


@Composable fun DialogOpen() {
    for (d in openedDeviceDialog) {
        "opening".debug("id",d)
        open(d)
    }
}



val openedDeviceDialog = mutableListOf<String>()
val deviceList = mutableListOf<String>()
@ExperimentalMaterialApi
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    scope.launch {
        singleWindowApplication(
            title = "提示", icon = withContext(Dispatchers.IO) {
                ImageIO.read(Resources.Urls.fastboot!!)
            }.toPainter(), state = WindowState(size = DpSize(400.dp,200.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("欢迎使用预览版HFT-Fastboot设备管理器",  modifier = Modifier.padding(bottom = 8.dp), style = MaterialTheme.typography.titleMedium)
                Text("软件已经启动，在状态栏内可以找到软件的图标，该软件会在后台三秒一次轮询检测Fastboot设备，在使用完成请及时退出（关闭本窗口不可关闭程序）" +
                        "。在您的Fastboot设备电脑插入后，可以双击图标启动Fastboot设备管理器。")
            }
        }
    }

    runBlocking {
        delay(10)
    }

    application {
        val welcomeNotifications = rememberNotification(
            "欢迎使用预览版HFT-Fastboot设备管理器", "软件已经启动，在状态栏内可以找到软件的图标，该软件会在后台三秒一次轮询检测Fastboot设备，在使用完成请及时退出（关闭本窗口不可关闭程序）" +
                    "。在您的Fastboot设备电脑插入后，可以双击图标启动Fastboot设备管理器。", Notification.Type.Info
        )

        if (openedDeviceDialog.isNotEmpty()) DialogOpen()
        var isScannerDialogShow by remember { mutableStateOf(false) }
        if (isScannerDialogShow)  object : ScannerViewModel {
            override val devices: List<String>
                get() = deviceList
            override fun onDeviceSelected(serial: String) {
                openedDeviceDialog.add(serial)
                debug("open",serial)
                isScannerDialogShow = false
            }

        } .let {

            ScannerDialog(it) {
                isScannerDialogShow = false
            }
        }
        val trays = object : Trays(this) {
            override fun exit() {
                exitApplication()
            }
            override fun onTrayIconSelected() {
                isScannerDialogShow = false
                isScannerDialogShow = true
            }
        }
        var isConnected by remember { trays.connectedState }
        isConnected = deviceList.isNotEmpty()
        trays.Render()

        trays.state.sendNotification(
            welcomeNotifications
        )

    }
}

