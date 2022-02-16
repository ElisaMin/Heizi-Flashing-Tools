package me.heizi.flashing_tool.fastboot.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.Window
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import fastbootIconBuffered
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.heizi.flashing_tool.fastboot.screen.panel.panelAbSlotSwitch
import me.heizi.flashing_tool.fastboot.screen.panel.panelPartition
import me.heizi.flashing_tool.vd.fb.FastbootDevice
import me.heizi.flashing_tool.vd.fb.extendableCard
import me.heizi.flashing_tool.vd.fb.fastboot.FastbootCommandViewModel
import me.heizi.flashing_tool.vd.fb.fastboot.fastbootCommand
import me.heizi.flashing_tool.vd.fb.info.DeviceInfo
import me.heizi.flashing_tool.vd.fb.scope
import me.heizi.kotlinx.logger.debug

@Composable
fun DeviceManagerWindow(
    viewModel: DeviceManagerViewModel,
    onExit:()->Unit,
) {
    Window(onExit,title = viewModel.device.serialID,icon = fastbootIconBuffered.toPainter()) {
        viewModel.DeviceManagerScreen()
    }
}

/**
 * Show a dialog display all these collected getvar
 *
 * fixme : search
 * @param vars
 * @param onClose
 */
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DeviceGetVarInfo(vars:List<Array<String>>,onClose: () -> Unit) {

    val s = vars.map { it.joinToString(": ") }.sorted()
    Popup(onDismissRequest = onClose, alignment = Alignment.BottomCenter) {
        var isTable by remember { mutableStateOf(false) }
//        var input by remember { mutableStateOf("") }
        Card(Modifier.height(500.dp).fillMaxWidth(), elevation = 10.dp) {
            Column {
                SmallTopAppBar(title = {
                    Row{
                        Text("详细Fastboot设备信息")

//                        OutlinedTextField(input, { input = it }, placeholder = {
//                            Text("搜索")
//                        })
                    }
                }, actions = {
                    IconButton({ isTable = !isTable }) {
                        Icon(Icons.Default.List,"切换")
                    }
                    IconButton(onClose) { Icon(Icons.Default.Close,"关闭") }
                })


                Box(Modifier.fillMaxSize()) {
                    if (isTable) {
                        val scroll = rememberLazyListState()
                        LazyVerticalGrid(GridCells.Adaptive(200.dp), state = scroll, contentPadding = PaddingValues(8.dp)) {
                            items(s) { Text(it) }
                        }
                        VerticalScrollbar(rememberScrollbarAdapter(scroll),Modifier.align(Alignment.TopEnd))
                    }
                    else {
                        val scroll = rememberScrollState()
                        FlowRow(
                            modifier = Modifier.verticalScroll(scroll).padding(end = 26.dp, start = 16.dp),
                            crossAxisAlignment = FlowCrossAxisAlignment.Center,
                            mainAxisAlignment = MainAxisAlignment.Center,
                            lastLineMainAxisAlignment = MainAxisAlignment.Center
                        ) {
                            for (ss in s
//                        .filter { input.isEmpty() || it.contains(input) }
                            )
                                Box(
                                    Modifier.padding(4.dp).background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ).padding(8.dp)
                                ) { Text(ss, style = MaterialTheme.typography.bodyLarge) }
                        }
                        VerticalScrollbar(rememberScrollbarAdapter(scroll), Modifier.align(Alignment.TopEnd))
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DeviceManagerViewModel.DeviceManagerScreen() {
    collectPipe()
    setUpSimpleInfo()
    if (isOpenDetail)
        DeviceGetVarInfo(device.cache) { isOpenDetail = false}
    Scaffold(Modifier.fillMaxSize(), topBar = {
        SmallTopAppBar(title = { Text(device.serialID, style = MaterialTheme.typography.displayLarge) })
    }) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 16.dp)
        ){

            LazyColumn(modifier = Modifier.defaultMinSize(minWidth = 172.dp)) {
                item {
                    extendableCard("设备信息") {
                        for ((key,value) in deviceSimpleInfo) {
                            ListItem(text = { Text(key) },secondaryText = { Text(value) })
                        }
                        Button({isOpenDetail = true}) {
                            Text("查看详细设备信息")
                        }
                    }
                }

                if (isSlotA!=null) item {
                    extendableCard("Slot",true) {
                        panelAbSlotSwitch(isSlotA!!,::switchPartition)
                    }
                }
            }
            Column {

                extendableCard("常规操作", modifier = Modifier.fillMaxWidth(), initExtend = false) {
                    val padding = Modifier.padding(4.dp)
                    FlowRow {
                        for ((name,block) in toFastbootOperateList().also {
                            "buttons".debug("fastboot operate",*it.keys.toTypedArray())
                        }) {
                            OutlinedButton(onClick = block, modifier = padding) { Text(name) }
                        }
                    }
                }
                panelPartition(device.partitions,device)

            }
        }
    }

}


open class DeviceManagerViewModelImpl(
    serialID: String
): DeviceManagerViewModel {

    private var fastbootCommandBuffer: MutableState<FastbootCommandViewModel?> = mutableStateOf(null)

    override val device: FastbootDevice = DeviceInfo(serialID)
    override var isOpenDetail: Boolean by mutableStateOf(false)
    override val isSlotA: Boolean? get()  = device.currentSlotA
    override val deviceSimpleInfo: MutableMap<String, String> = mutableStateMapOf()
    @Composable
    override fun setUpSimpleInfo() {
        deviceSimpleInfo["是否有多个SLOT"] = if (device.isMultipleSlot) "多个" else "单个"
        deviceSimpleInfo["是否BL已解锁"]   = if (device.isUnlocked) "已解锁" else "未解锁"
        device.isFastbootd?.let {
            deviceSimpleInfo["Fastboot状态"] = if (!it) "fastboot" else "fastbootd"
        }
    }

    override fun switchPartition(isSlotA: Boolean) {
        device.run("--set-active=${if (isSlotA) "b" else "a" } ") {
            runBlocking {
                delay(100)
            }
            device.refreshInfo()
        }
    }


    @FastbootOperate("OEM解锁") fun oemUnlock() {
        device run "oem unlock"
    }
    @FastbootOperate("重启") fun reboot() {
        device run "reboot"
    }
    @FastbootOperate("重置") fun wipe() {
        device run "-w"
    }
    @NeedGetvar("is-userspace","yes")
    @NeedGetvar("is-userspace","no")
    @FastbootOperate("重启到Fastbootd") fun rebootToFastbootd() {
        device run "reboot fastboot"
    }

    @Composable
    override fun collectPipe() {
        scope.launch {
            device.fastbootCommandPipe.collect {
                fastbootCommandBuffer.value = it
            }
        }
        val buffer by fastbootCommandBuffer
        if (buffer!=null) {
            fastbootCommand(buffer!!) {
                fastbootCommandBuffer.value = null
            }
        }
    }
//    @FastbootOperate("启动镜像") fun boot(){

}

//    }
interface DeviceManagerViewModel {
    
    
    var isOpenDetail : Boolean
    val device: FastbootDevice
    val isSlotA: Boolean?
    val deviceSimpleInfo: Map<String, String>
    @Composable
    fun collectPipe()
    @Composable fun setUpSimpleInfo()
    fun switchPartition(isSlotA: Boolean)
    @OptIn(ExperimentalStdlibApi::class)
    fun toFastbootOperateList():Map<String,()->Unit> = buildMap {
        val main = this@DeviceManagerViewModel
        for (it in main::class.java.methods.filter { it.annotations.isNotEmpty() }) {
            val operate = it.getAnnotation(FastbootOperate::class.java)
            if (operate != null) {
                it.getAnnotationsByType(NeedGetvar::class.java).let bool@{ annotations->
                    if (annotations.isEmpty()) return@bool true
                    for (getvar in annotations) {
                        if (device[getvar.name] == getvar.value) return@bool true
                    }
                    false
                } .let { isShow ->
                    if (isShow) this[operate.name] = {
                        it.invoke(main)
                    }
                }
                // bool? : off -> null /on yes -> true /on no -> false
                //should show : off & on yes -> true /on no -> false
            }
        }
    }


}
@Repeatable
annotation class NeedGetvar(val name:String,val value:String = "")
annotation class FastbootOperate(val name:String)
