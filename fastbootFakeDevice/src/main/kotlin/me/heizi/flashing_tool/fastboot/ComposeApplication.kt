package me.heizi.flashing_tool.fastboot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberNotification
import me.heizi.flashing_tool.fastboot.screen.*
import me.heizi.kotlinx.logger.debug

fun composeApplication() {
    application {
        val welcomeNotifications = rememberNotification(
            "欢迎使用预览版HFT-Fastboot设备管理器", "软件已经启动，在状态栏内可以找到软件的图标，该软件会在后台三秒一次轮询检测Fastboot设备，在使用完成请及时退出（关闭本窗口不可关闭程序）" +
                    "。在您的Fastboot设备电脑插入后，可以双击图标启动Fastboot设备管理器。", Notification.Type.Info
        )

        if (openedDeviceDialog.isNotEmpty()) DialogOpen()
        var isScannerDialogShow by remember { mutableStateOf(false) }
        if (isScannerDialogShow)  object : FlowCollectedScannerViewModel() {
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

        trays.Render()

        trays.state.sendNotification(
            welcomeNotifications
        )

    }
}