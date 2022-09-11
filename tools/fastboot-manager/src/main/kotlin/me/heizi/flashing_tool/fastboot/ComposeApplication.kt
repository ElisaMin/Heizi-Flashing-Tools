package me.heizi.flashing_tool.fastboot

import androidx.compose.runtime.*
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberNotification
import me.heizi.flashing_tool.fastboot.repositories.Fastboot
import me.heizi.flashing_tool.fastboot.screen.*
import me.heizi.kotlinx.logger.debug

fun composeApplication() {

    application {
        Fastboot.error
        var isScannerDialogShow by remember { mutableStateOf(true) }
        val trays = remember {
            object : Trays(this@application) {
                override fun exit() {
                    exitApplication()
                }
                override fun onTrayIconSelected() {
                    isScannerDialogShow = false
                    isScannerDialogShow = true
                }
            }
        }
        val openingDeviceDialogs = remember { mutableStateListOf<String>() }
        val scannerViewModel = remember {
            object : FlowCollectedScannerViewModel() {
                override val traysViewModel: TraysViewModel get() = trays
                override fun onDeviceSelected(serial: String) {
                    openingDeviceDialogs.add(serial)
                    debug("open",serial)
                    isScannerDialogShow = false
                }
                override fun onFirstTimeCollected() {
                    if(devices.size == 1) onDeviceSelected(devices[0])
                }

                override fun onInit() {
                    super.onInit()
                    if (openingDeviceDialogs.isEmpty())
                    isScannerDialogShow = true
                }
            }

        }

        if (openingDeviceDialogs.isNotEmpty()) OpenDialogs(openingDeviceDialogs,{openingDeviceDialogs-=it},{isScannerDialogShow=!isScannerDialogShow},stopCollect={trays.onStopCollecting()})

        if (isScannerDialogShow) ScannerDialog(scannerViewModel, exitApp = {exitApplication()}) {
            isScannerDialogShow = false
        }

        val notification = rememberNotification(
            "FFT已启动，在状态栏内可以找到软件的图标。",
            "可以双击FB图标启动扫描界面，启动不了重启软件。该软件会每三秒检测一次Fastboot设备，建议在不使用时及时关闭检测/退出软件。本版本为预览版，所会有很多Bug，请谅解。", Notification.Type.Info
        )
        trays.Render()
        LaunchedEffect(trays) {
            trays.state.sendNotification(
                notification
            )
        }
    }
}