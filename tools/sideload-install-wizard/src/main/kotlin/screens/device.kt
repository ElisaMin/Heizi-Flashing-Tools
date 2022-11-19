package me.heizi.flashing_tool.sideloader.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import me.heizi.flashing_tool.adb.ADBDevice

var isSideload by mutableStateOf(false)


/**
 * device choosing
 *
 * multiple selection of choosing the device that user wanted to install
 * finally returns a list of device serial
 *
 * device serial can be remembered on disk and its offline device
 * so must be having a function to re-connect the device
 *
 * its deference color identify system between sideload mode and apk
 * install mode
 *
 * @param devices list of device no matter is connected
 * @param addDevice
 */

@Composable
fun DeviceScreen(
    devices:List<ADBDevice>,
    selected:MutableSet<String>,
    isWaiting:Boolean,
    addDevice:(serial:String)->Boolean,
    onConnectRequest: (context: InnerDeviceContextState) -> Unit
) = Column {
    var isInputDialogLaunch by remember { mutableStateOf(false) }
    if (isInputDialogLaunch) addDeviceDialog(addDevice) {isInputDialogLaunch = false}
    TextButton({}, Modifier.paddingButBottom(8.dp).fillMaxWidth()) {
        Icon(Icons.Default.Add,"add device")
        Text("添加设备")
    }
    if (isWaiting) LinearProgressIndicator(Modifier.height(8.dp).padding(horizontal = 16.dp).fillMaxWidth())
    else Box(Modifier.height(8.dp).padding(horizontal = 16.dp))
    ListDevice(devices = devices, onConnectRequest = onConnectRequest, onSelecting = { device ->
        if (device in selected) selected-=device else selected+=device
        device in selected
    },isWaiting = isWaiting)
}


/**
 * launch a dialog to deal with adding new device using serial or host name
 *
 * @param onSubmit returns a boolean as allow dialog dismiss after checking submit
 * @param onDismissing
 */

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun addDeviceDialog(
    onSubmit:(String)->Boolean={true},
    onDismissing:()->Unit={}
) {
    Popup(alignment = Alignment.Center, onDismissRequest = onDismissing, focusable = true) {
        ElevatedCard(Modifier.sizeIn(maxWidth = 340.dp), elevation = CardDefaults.cardElevation(5.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("添加设备")
                var value by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        value=it
                    },trailingIcon = {
                        if (value.isNotEmpty()) IconButton({
                            if (onSubmit(value)) onDismissing()
                        }) {
                            Icon(Icons.Default.Check,"done")
                        } else IconButton(onDismissing) {
                            Icon(Icons.Default.Clear,"close it")
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun ListDevice(
    isWaiting: Boolean,
    devices:List<ADBDevice>,
    onSelecting:(serial:String)->Boolean,
    onConnectRequest:(context:InnerDeviceContextState) ->Unit
) {
    for (device in devices) {
        DeviceRemembered(device.serial,device.state, select =  {
            onSelecting(device.serial)
        }, connectRequest = onConnectRequest, isWaiting = isWaiting)
    }
}


@Composable
fun DeviceRemembered(
    serial: String,
    state: ADBDevice.DeviceState,
    select:(isSelected:Boolean)->Boolean,
    isWaiting: Boolean = false,
    connectRequest: (state:InnerDeviceContextState) -> Unit
) {
    val context = remember(serial,101) { state.context() }
    val text = remember(serial,102) { state.notify(context) }
    var isSelected by remember(serial,103) { mutableStateOf(false) }
    val color = if (isSelected) InnerDeviceContextState.clickedColor() else context.color()
    Device(serial,state,isSelected,context,color,text,isWaiting) {
        if (state == if (isSideload) ADBDevice.DeviceState.sideload else ADBDevice.DeviceState.device ) {
            isSelected = select(isSelected)
        } else connectRequest(context)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Device(
    serial:String,
    deviceConnectState: ADBDevice.DeviceState,
    isChecked:Boolean=false,
    context: InnerDeviceContextState = deviceConnectState.context(),
    contentColor: CardColors = if (isChecked) InnerDeviceContextState.clickedColor() else context.color(),
    notifyMsg:String = deviceConnectState.notify(context),
    isWaiting: Boolean = false,
    onClick:()->Unit
) {
    Card(
        enabled = context.isAvailable || !isWaiting ,
        modifier = Modifier.paddingButBottom(8.dp),
        colors = contentColor,
        onClick = onClick,
        content = {
            Row(Modifier.padding(16.dp,8.dp).fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(serial, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(notifyMsg, maxLines = 1, overflow = TextOverflow.Visible)
            }
        }
    )
}

fun ADBDevice.DeviceState.notify(
    context:InnerDeviceContextState = this.context()
):String = buildString {
    with(ADBDevice.DeviceState.Companion) {
        when(this@notify) {
            device -> append("安卓设备")
            recovery -> append("Recovery模式")
            sideload -> append("Sideload模式")
            else -> append("不可用")
        }
        append(when(context) {
            InnerDeviceContextState.Reconnect -> " | 点击重新连接"
            InnerDeviceContextState.SideloadRebootNeed -> " | 点击重启至线刷模式"
            InnerDeviceContextState.AndroidEvenRebootNeed -> " | 点击重起"
            else -> ""
        })
    }
}
