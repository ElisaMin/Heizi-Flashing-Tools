package me.heizi.flashing_tool.fastboot.screen

import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.heizi.flashing_tool.fastboot.repositories.FastbootDevices
import me.heizi.kotlinx.logger.debug


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun open(serialId:String,onCloseWindow:()-> Unit,onSwitchingDevice:()->Unit,stopCollect: () -> Unit) {

    var fastbootDevice = FastbootDevices.getSingletonBySerialOrNull(serialId)
    //useless block
    while (fastbootDevice == null) AlertDialog(
        onDismissRequest = { return@AlertDialog },
        title = { Text("设备未连接") },
        text =  { Text("未找到设备$serialId，请确认设备正常连接电脑。") },
        confirmButton = {
            Button({
                runBlocking {
                    delay(300)
                    fastbootDevice = FastbootDevices.getSingletonBySerialOrNull(serialId)
                }
            }){ Text("刷新") } }
    )


    runBlocking {
        fastbootDevice!!.updateInfo()
        delay(100)
    }

    var isWindowOpen by remember { mutableStateOf(true) }

    if (!isWindowOpen) {
        onCloseWindow()
        return
    } else {
        stopCollect()
    }

    val device = DeviceManagerViewModelImpl(fastbootDevice!!) {
        isWindowOpen = !isWindowOpen
        onSwitchingDevice()
    }

    if (isWindowOpen) DeviceManagerWindow(device) {
        isWindowOpen = false
    }
}


@Composable
fun OpenDialogs(dialogs:List<String>,onCloseWindow:(serialID:String)->Unit,onSwitchingDevice: () -> Unit={},stopCollect:()->Unit) {
    for (id in dialogs) {
        "opening".debug("id",id)
        open(id,{ onCloseWindow(id) },onSwitchingDevice,stopCollect)

    }
}