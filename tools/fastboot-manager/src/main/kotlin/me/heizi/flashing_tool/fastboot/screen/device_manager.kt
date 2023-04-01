package me.heizi.flashing_tool.fastboot.screen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.mayakapps.compose.windowstyler.WindowBackdrop
import com.tunjid.me.core.ui.dragdrop.PlatformDropTargetModifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.heizi.compose.ext.monet.common.Monet
import me.heizi.flashing_tool.fastboot.extendableCard
import me.heizi.flashing_tool.fastboot.fastbootIconBuffered
import me.heizi.flashing_tool.fastboot.repositories.*
import me.heizi.flashing_tool.fastboot.screen.panel.panelAbSlotSwitch
import me.heizi.flashing_tool.fastboot.screen.panel.panelPartition
import me.heizi.kotlinx.compose.desktop.core.components.AboutExtendCard
import me.heizi.kotlinx.logger.debug

@Composable
fun DeviceManagerWindow(
    viewModel: DeviceManagerViewModel,
    onExit:()->Unit,
) {
    Window(
        onExit,title = viewModel.device.serialId,
        icon = fastbootIconBuffered.toPainter(),
        state = WindowState(
            size = DpSize(900.dp,700.dp),
            position = WindowPosition.Aligned(Alignment.Center)
        )
    ) {
        Monet {
            // fixme maybe
            // remove surface
//            LaunchedEffect(this.color) {
//                val styler = windowStyler
//                styler.backdropType = WindowBackdrop.Tabbed
//
//            }
            val density = LocalDensity.current.density
            val drop = remember {
                PlatformDropTargetModifier(
                    density,window
                )
            }
            viewModel.DeviceManagerScreen(drop)
        }
    }
}

@Preview
@Composable
fun PreView() {

    val theInfo: FastbootDeviceInfo = object :FastbootDeviceInfo {
        override val simple: FastbootDeviceInfo.Simple = object : FastbootDeviceInfo.Simple {
            override val isUnlocked: Boolean = true
            override val isMultipleSlot: Boolean = true
            override val isFastbootd: Boolean = true
            override val currentSlotA: Boolean = true

        }
        override val partitionInfos: List<PartitionInfo> = listOf(PartitionInfo("name",PartitionType.EXT4,0f))
        override fun toArray(): Array<Array<String>> = arrayOf()
        override fun get(s: String): String? = null

    }

    DeviceManagerViewModelImpl(object : FastbootDevice {
        override val serialId: String = "fuck"
        override val info: StateFlow<FastbootDeviceInfo> = MutableStateFlow(theInfo)
        override val runner: DeviceRunner = object : DeviceRunner {
            override val serialId: String = "fuck"
            override fun run(command: String) {}
            override fun run(viewModel: FastbootCommandViewModel) {}
            override suspend fun getvar(): String = ""
            @Composable
            override fun start() {}

        }
        override suspend fun updateInfo() {}

    }).DeviceManagerScreen()
}

/**
 * Show a dialog display all these collected getvar
 *
 * fixme : search
 * @param vars
 * @param onClose
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceGetVarInfo(vars:Array<Array<String>>,onClose: () -> Unit) {

    val s = vars.map { it.joinToString(": ") }.sorted()
    Popup(onDismissRequest = onClose, alignment = Alignment.BottomCenter) {
        var isTable by remember { mutableStateOf(false) }
//        var input by remember { mutableStateOf("") }
        Card(Modifier.height(500.dp).fillMaxWidth(), elevation = 10.dp) {
            Column {
                TopAppBar(title = {
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
                        val scroll = rememberLazyGridState()
                        LazyVerticalGrid(GridCells.Adaptive(200.dp), state = scroll, contentPadding = PaddingValues(8.dp)) {
                            items(s) { Text(it) }
                        }
                        // FIXME: 2022/5/20
//                        VerticalScrollbar(rememberScrollbarAdapter(scroll),Modifier.align(Alignment.TopEnd))
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
@Composable
fun AboutDialog(onClose: () -> Unit) {
    Popup(onDismissRequest = onClose, alignment = Alignment.BottomCenter) {
        Card(Modifier.fillMaxWidth(), elevation = 10.dp) {
            Column {
                IconButton(onClose) { Icon(Icons.Default.Close,"关闭") }
                AboutExtendCard(true)
            }
        }
    }
}
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DeviceManagerViewModel.DeviceManagerScreen(drop:Modifier = Modifier) {
    withCompose()
    if (isAboutOpening) AboutDialog { isAboutOpening = false }
    if (isOpenDetail) DeviceGetVarInfo(info.toArray()) { isOpenDetail = false}
    
    
    Scaffold(Modifier.fillMaxSize().then(drop), topBar = {
        TopAppBar(title = { Text(device.serialId, style = MaterialTheme.typography.displayLarge, maxLines = 1, overflow = TextOverflow.Ellipsis) }, actions = {
            Button(::launchDevicesSelector) { Text("切换设备") }
        })
    }) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 16.dp)
                .padding(top = 64.dp)
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
                item {
                    TextButton({isAboutOpening = true}, modifier = Modifier.wrapContentSize()) {
                        Text("关于软件/捐赠/BUG")
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

                panelPartition(info.partitionInfos,device)

            }
        }
    }

}



class DeviceManagerViewModelImpl(
    override val device: FastbootDevice,
    private val launchSelector:()->Unit = {}
):Operates(device.runner) {
    override var isAboutOpening: Boolean by mutableStateOf(false)
    override var isOpenDetail: Boolean by mutableStateOf(false)
    override var info: FastbootDeviceInfo by mutableStateOf(FastbootDeviceInfo.empty)
    override val isSlotA: Boolean? by mutableStateOf(device.info.value.simple.currentSlotA)
    override val deviceSimpleInfo = mutableStateMapOf<String,String>()

    override fun launchDevicesSelector() {
        launchSelector()
    }
    @Composable
    override fun withCompose() {
        device.runner.start()
        val info by device.info.collectAsState()
        this.info = info
        deviceSimpleInfo["是否有多个SLOT"] = if (info.simple.isMultipleSlot) "多个" else "单个"
        deviceSimpleInfo["是否BL已解锁"]   = if (info.simple.isUnlocked) "已解锁" else "未解锁"
        info.simple.isFastbootd?.let {
            deviceSimpleInfo["Fastboot状态"] = if (!it) "fastboot" else "fastbootd"
        }

    }

}
abstract class Operates(
    private val runner: DeviceRunner
):DeviceManagerViewModel {
    @FastbootOperate("OEM解锁") fun oemUnlock() {
        runner run "oem unlock"
    }
    @FastbootOperate("重启") fun reboot() {
        runner run "reboot"
    }
    @FastbootOperate("重置") fun wipe() {
        runner run "-w"
    }
    @NeedGetvar("is-userspace","yes")
    @NeedGetvar("is-userspace","no")
    @FastbootOperate("重启到Fastbootd") fun rebootToFastbootd() {
        runner run "reboot fastboot"
    }
    final override fun switchPartition(isSlotA: Boolean) {
        runner run "--set-active=${ if (isSlotA) "b" else "a" } "
    }
}

interface DeviceManagerViewModel {
    
    var isAboutOpening:Boolean
    var isOpenDetail : Boolean
    val device: FastbootDevice
    val info:FastbootDeviceInfo
    val isSlotA: Boolean?
    val deviceSimpleInfo: Map<String, String>
    fun launchDevicesSelector()
    @Composable
    fun withCompose()
    fun switchPartition(isSlotA: Boolean)
    fun toFastbootOperateList():Map<String,()->Unit> = buildMap {
        val main = this@DeviceManagerViewModel
        for (it in main::class.java.methods.filter { it.annotations.isNotEmpty() }) {
            val operate = it.getAnnotation(FastbootOperate::class.java)
            if (operate != null) {
                it.getAnnotationsByType(NeedGetvar::class.java).let bool@{ annotations->
                    if (annotations.isEmpty()) return@bool true
                    for (getvar in annotations) {
                        if (info[getvar.name] == getvar.value) return@bool true
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
