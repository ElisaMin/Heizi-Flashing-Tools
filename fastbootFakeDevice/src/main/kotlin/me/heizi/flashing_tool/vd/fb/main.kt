@file:OptIn(ExperimentalFoundationApi::class)

package me.heizi.flashing_tool.vd.fb

import androidx.compose.desktop.ComposeDialog
import androidx.compose.desktop.Window
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.application
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.heizi.flashing_tool.vd.fb.info.DeviceInfo
import me.heizi.flashing_tool.vd.fb.style.panelAbSlotSwitch
import me.heizi.flashing_tool.vd.fb.style.panelPartition
import me.heizi.kotlinx.shell.CommandResult
import me.heizi.kotlinx.shell.CommandResult.Companion.waitForResult
import me.heizi.kotlinx.shell.shell
import org.jetbrains.skiko.toImage
import java.awt.image.BufferedImage
import java.net.URL
import java.nio.charset.Charset
import javax.imageio.ImageIO

val scope = MainScope()


@OptIn(ExperimentalMaterialApi::class)
@Composable fun panelAll(viewModel: ViewModel) {
    viewModel.collectPipe()
    viewModel.setUpSimpleInfo()
    Row (
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .fillMaxHeight()
    ){

        LazyColumn(modifier = Modifier.defaultMinSize(minWidth = 172.dp)) {
            item {
                extendableCard("设备信息") {
                    for ((key,value) in viewModel.deviceSimpleInfo) {
                        ListItem(text = { Text(key) },secondaryText = { Text(value) })
                    }
                }
            }
            item {
                extendableCard("常规操作") {
                    val padding = Modifier.padding(4.dp)
                    for (it in viewModel::class.java.methods.filter { it.annotations.isNotEmpty() }) {
                        val operate = it.getAnnotation(FastbootOperate::class.java)
                        if (operate!=null) {
                            OutlinedButton(onClick = {
                                it.invoke(viewModel)
                            },modifier = padding){ Text(operate.name)}
                        }
                    }
//                    for (i in arrayOf("重启设备","启动镜像","清空用户数据")) {
//                        OutlinedButton({},modifier = padding,) { Text(i)  }
//                    }
                }
            }

            if (viewModel.isSlotA!=null) item {
                extendableCard("Slot",true) {
                    panelAbSlotSwitch(viewModel.isSlotA!!,viewModel::switchPartition)
                }
            }
        }
        panelPartition(viewModel.device.partitions)
    }

}

fun open(serialId:String) {
    val device = ViewModelImpl(serialId)
    (device.device as DeviceInfo) .refreshInfo {
        Window(title = serialId,icon = fastbootIconBuffered) {
            panelAll(device)
        }
    }

}


val fastbootIconBuffered: BufferedImage = ImageIO.read(Resources.Urls.fastboot!!)
fun read(url: URL) = ImageIO.read(url).toImage().asImageBitmap()
fun String.toGBK() =
    String(toByteArray(), Charset.forName("GBK"))



@ExperimentalMaterialApi
@OptIn(ExperimentalComposeUiApi::class)
fun main(args: Array<String>) {
    val deviceList = mutableListOf<String>()
    scope.launch {
        while (true) {
            shell("fastboot devices").waitForResult() {
                if (it is CommandResult.Success) {
                    deviceList.clear()
                    it.message.lines().drop(1).dropLast(1).takeIf { it.isNotEmpty() }?.forEach {
                        val s = it.split("\t")
                        if (s.size == 2 && s[1] == "fastboot"  ) deviceList.add(s[0])
                    }
                }
            }
            if (deviceList.isEmpty()) delay(3000) else delay(30000)
        }
    }

    runBlocking {
        delay(10)
    }

    application {
        var dialog:ComposeDialog? = null
        val isConnected = deviceList.isNotEmpty()
        var isShowDialog by remember { mutableStateOf(false) }
        if (isShowDialog)  {
            Dialog({
                dialog?.isVisible = false
                isShowDialog = false
                   },title = "",icon = fastbootIconBuffered) {
                dialog = this.dialog
                Image(fastbootIconBuffered.toImage().asImageBitmap(),"background",modifier = Modifier.fillMaxHeight().fillMaxWidth())
                Column(Modifier.fillMaxWidth().fillMaxHeight().padding(16.dp)) {
                    Title("已连接设备")

                    LazyColumn(modifier = Modifier.fillMaxWidth().background(Color.LightGray.copy(alpha = 0.3f))) {
                        items(deviceList) {
                            ListItem(modifier = Modifier.clickable {
                                open(it)
                            }) { Text(it) }
                        }
                    }
                }
            }
//            AlertDialog({
//                isShowDialog = false
//            },confirmButton = { Text("双击打开")},title = { Text("已连接设备.....")} ,text = {
//
//                if (!isConnected) Text("无设备连接，请确保你的手机已经进入Fastboot模式。") else
//                LazyColumn {
//                    items(deviceList) {
//                        ListItem { Text(it) }
//                    }
//                }
//            })
        }
        Tray(ImageIO.read( if (isConnected) Resources.Urls.connected else Resources.Urls.disconnect),hint = if (isConnected) "设备已连接" else "Fastboot未连接设备",onAction = {
            isShowDialog=true
            dialog?.isVisible = true
        }) {

//            Item("connected:0",false){}
            Item("exit"){ exitApplication() }

        }
    }
}
object Resources {
    private fun getResources(name:String): URL? = this::class.java.classLoader.getResource(name)
    object Urls {
        val disconnect get() = getResources("ic_disconnect.png")
        val connected get() = getResources("ic_connected.png")
        val fastboot get() = getResources("ic_fastboot.png")
    }
//    object Images {
//        val disconnect by lazy {
//            ImageIO.read(Urls.connected)
//        }
//    }
}