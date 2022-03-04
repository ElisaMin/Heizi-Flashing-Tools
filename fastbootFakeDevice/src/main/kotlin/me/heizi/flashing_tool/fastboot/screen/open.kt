package me.heizi.flashing_tool.fastboot.screen

import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.heizi.flashing_tool.fastboot.openedDeviceDialog
import me.heizi.flashing_tool.fastboot.repositories.FastbootDevices
import me.heizi.kotlinx.logger.debug


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun open(serialId:String) {
    var fastbootDevice = FastbootDevices.getSingletonBySerialOrNull(serialId)
    while (fastbootDevice == null) {
        AlertDialog({
            return@AlertDialog
        }, title = { Text("设备未连接") },
            text = { Text("未找到设备$serialId，请确认设备正常连接电脑。") },
            confirmButton = {
                Button({
                    runBlocking {
                        delay(300)
                        fastbootDevice = FastbootDevices.getSingletonBySerialOrNull(serialId)
                    }
                }){ Text("刷新") }
            })
    }

    runBlocking {
        fastbootDevice!!.updateInfo()
        delay(100)
    }

    val device = DeviceManagerViewModelImpl(fastbootDevice!!)
    var isWindowOpen by remember { mutableStateOf(true) }
    "opening".debug("isOpen",isWindowOpen)
    if (isWindowOpen) DeviceManagerWindow(device) {
        openedDeviceDialog.remove(serialId)
        isWindowOpen = false
    }
}


@Composable
fun DialogOpen() {
    for (d in openedDeviceDialog) {
        "opening".debug("id",d)
        open(d)
    }
}