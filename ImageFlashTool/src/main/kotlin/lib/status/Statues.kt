package lib.status

import lib.Style
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import lib.ChipCheckBox
import lib.mutableStateFlowOf
import lib.toState
import me.heizi.kotlinx.logger.debug
import me.heizi.kotlinx.shell.CommandResult
import me.heizi.kotlinx.shell.CommandResult.Companion.waitForResult
import me.heizi.kotlinx.shell.ProcessingResults
import me.heizi.kotlinx.shell.shell
import java.io.File

/**
 * Statues
 *            (delay 1s no matter)
 * launcher -> wait for device ->| info -> flashing ->| success
 *                               |                  ->| failed
 *                             ->| booting          ->|
 *
 */

sealed class Statues {


    abstract val view:@Composable ColumnScope.()->Unit

    abstract val help:String?

    /**
     * 启动界面
     *
     * 启动别的东西
     */
    object Launcher: Statues() {
        val _a = mutableStateOf(true)
        val _b = mutableStateOf(false)
        val avb = mutableStateOf(false)

        val partitions = mutableStateOf("")

        override val view: @Composable ColumnScope.() -> Unit =  {
            title = file.name
            subtitle = "你想要刷入哪个分区里面?"
            var partitions by remember { partitions }
            var errorText by remember { mutableStateOf("") }
            errorText = when {
                partitions.contains("_",) -> {
                    "错误!包含'_'字符"
                }
                partitions.contains(",",) -> {
                    "错误!包含','字符"
                }
                partitions.contains(" ",) -> {
                    "错误!包含' '字符"
                }

                else -> { "" }
            }

            //=====
            TextField(
                partitions,
                isError = errorText.isNotEmpty(),
                onValueChange = {
                    partitions = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("分区名称")
                },
            )
            if (errorText.isNotEmpty()) Text(errorText)
            Box(Style.Padding.bottom)
            //=====
            Row {
                ChipCheckBox("_a", modifier = Style.Padding.end, enable = _a)
                ChipCheckBox("_b", modifier = Style.Padding.end, enable = _b)
                ChipCheckBox("disable avb", modifier = Style.Padding.end, enable = avb)
            }
            //=====
            Button(
                onClick = {
                    if (errorText.isEmpty()) nextStep(Modes.Flashing(partition = partitions))
                }, modifier = Style.Padding.vertical.align(Alignment.End),
                enabled = errorText.isEmpty()&&partitions.isNotEmpty()
            ) {
                Text("下一步")
            }

            //-----------
            Text("其他功能?", modifier = Style.Padding.bottom)
            OutlinedButton(onClick = {
                nextStep(Modes.Boot)
            }) {
                Text("启动镜像")
            }
        }
        override val help: String =
        """ |刷入分区|分区名称:你要刷入的分区的名称
            |刷入分区|_a/_b:激活后会在分区名称后面硬拼接相应的后缀 例如激活_a时输入boot就会刷入boot_a分区
            |刷入分区|disable avb:在指令加入--disable-verity --disable-verification
            |启动镜像|:执行指令 fastboot boot file
        """.trimMargin()

        private fun nextStep(mode: Modes) {
            Companion.mode = mode
            current = WaitForDevice
        }
    }
    object WaitForDevice: Statues() {

        val devices = mutableStateMapOf<String, Boolean> ()
        val selected get() =  devices.filter { it.value }.keys


        @OptIn(ExperimentalCoroutinesApi::class)
        val job = CoroutineScope(Dispatchers.IO).launch(start = CoroutineStart.ATOMIC) {
            while (true) {
                delay(1000)
                shell(prefix = arrayOf("cmd","/c","fastboot devices"),isWindows_keep = false).waitForResult {
                    if (it is CommandResult.Success) it.runCatching {
                        for (line in message.lines()) {
                            val test = line.split("\t")
                            "device".debug(test,line)
                            if (test.size!=2&&test[1]!="fastboot") break
                            devices[test[0]] = devices[test[0]] ?: false
                        }
                    }
                }
            }

//            var j = 1
//
//            repeat(3) {
//                delay(1100)
//                devices["fak3ee1de6vi8ce${j++}"] = false
//                devices["fa2de9v0ces994w${j++}"] = false
//                devices["fakeadevi36ce66${j++}"] = false
//
//            }
        }


        override val view: @Composable ColumnScope.() -> Unit = {
            if (!job.isActive) job.start()
            title = file.name
            subtitle = "你要把它刷入哪个设备?"
//            val devices:SnapshotStateMap<String,Boolean> by rememberSaveable {devices}


            if (devices.isEmpty()) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Text("正在等待设备.....")
            } else {
                LazyColumn(Modifier.weight(2f)) {
                    items(devices.keys.toList()) {
                        Item(it)
                    }
                }
            }
            Box(Style.Padding.bottom)

            val enabled = !devices.isEmpty() && true in devices.values
            Button(
                onClick = {
                    if (enabled) nextStep()
                }, enabled = enabled, modifier = Modifier.align(Alignment.End)
            ) {
                Text("下一步")
            }

        }
        override val help: String =
        """ |等待设备|现在我要干什么？:把你的Android设备启动到fastboot模式，软件会每隔一秒用fastboot devices指令扫描设备。
            |等待设备|卡在这了?:可以检查一下驱动或者USB接口什么的
        """.trimMargin()

        fun nextStep() {
            when(mode) {
                Modes.Boot -> {
                    current =  Invoke {
                        shell(*selected.map { device ->
                            "fastboot -s $device boot $file"
                        }.toTypedArray())
                    }
                }
                is Modes.Flashing -> {
                    current = Info
                }

                Modes.Else -> {
                    throw IllegalStateException("炸了 在Models.Else")
                }
            }
        }
        @OptIn(ExperimentalMaterialApi::class)
        @Composable
        fun Item (deviceId: String) = ListItem(trailing = {
//            val  state = (true)
            ChipCheckBox {
                devices[deviceId] = it
            }
        }) {
            Text(deviceId)
        }
    }



    object Info : Statues() {

        val partition get() = Launcher.partitions.value
        val _a get() =  Launcher._a.value
        val _b get() =  Launcher._b.value
        val avb get() =  Launcher.avb.value
        val filltedDevice = WaitForDevice.selected

        override val view: @Composable ColumnScope.() -> Unit = {
            val emptyBlock = { _:String->}
            val modifier = Modifier.fillMaxWidth()
            OutlinedTextField(onValueChange = emptyBlock,value = file.path,label = {Text("💿文件")},enabled = false,modifier = modifier)
            OutlinedTextField(onValueChange = emptyBlock,value = WaitForDevice.devices
                .filter { it.value }
                .keys.joinToString("\n")
                ,label = {Text("📱设备")},enabled = false
                ,modifier = modifier
            )
            OutlinedTextField(onValueChange = emptyBlock,value = buildString {
                fun afterPtt(string: String) {
                    append(partition)
                    append(string)
                    append(" ")
                }
                if (!_a && !_b) afterPtt("")
                if (_a) afterPtt("_a")
                if (_b) afterPtt("_b")
                if (avb) append(" disable verity/verification")
             },
                label = {Text("🍰分区")},enabled = false,modifier = modifier)
            Box(Style.Padding.bottom)
            Button(onClick = {
                nextStep()
            }, modifier = Modifier.align(Alignment.End)) {
                Text("下一步")
            }
        }
        override val help: String? = null

        fun nextStep() {
            arrayListOf("fastboot -s").flatMap { command ->
                val partition = partition
                filltedDevice.map { device->
                    " $command $device flash $partition"
                }
            }.flatMap { command -> sequence {
                if (!_a && !_b) yield(command)
                if (_a) yield("${command}_a")
                if (_b) yield("${command}_b")
            } }.map { command -> "$command $file" }
                .toTypedArray()
                .let {
                    current =  Invoke {
                    shell(*it)
                }
            }

        }

    }



    class Invoke(
        val block:suspend CoroutineScope.()->Flow<ProcessingResults>
    ) : Statues() {
        private val _text = MutableStateFlow("++start")
        var text by mutableStateFlowOf(_text)

        private val _progressing = MutableStateFlow(0f)
        var progressing by mutableStateFlowOf(_progressing)

        @OptIn(ExperimentalCoroutinesApi::class)
        val job = GlobalScope.launch (start = CoroutineStart.ATOMIC) {
            delay(1000)
            _progressing.emit(0.11f)
            launch {
                block().waitForResult(
                    onMessage = { text += "\n::$it" },
                    onError = { text+="\n!!$it"},
                    onResult = ::onResult
                )
            }
        }

        fun onResult(r:CommandResult) {
            "result".debug("statues","called")
            progressing = 0.9f
            runBlocking {
                delay(300)
                progressing = 1f
            }
            current = Result(r,text)

        }

        override val view: @Composable ColumnScope.() -> Unit = {
            title = "正在刷入"
            subtitle = "请稍等"
            if (!job.isActive) job.start()
            val stateText = _text.collectAsState()
            val stateProgressing = _progressing.collectAsState()
            val text by remember { stateText }
            val progressing by remember { stateProgressing }
            progress(progressing)
            Text(text)
        }
        override val help: String? = null

        @Composable
        fun progress(progress:Float) {
            if (progress!=0.11f) LinearProgressIndicator(progressing,Modifier.fillMaxWidth())
            else LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }
    class Result(val result:CommandResult,message:String): Statues() {
        override val view: @Composable ColumnScope.() -> Unit = {
            val result = if (result is CommandResult.Success) "成功"  else "失败"
            title = "执行完成"
            subtitle = "$result，如果发现什么不对劲请截图请教大佬。"
            OutlinedTextField(message,
                onValueChange = {},
                label = { Text("结果") },
                modifier = Modifier.padding(3.dp).fillMaxWidth().fillMaxHeight(),
            )
        }
        override val help: String? = null

    }

    sealed class Modes {
        data class Flashing(
            val partition: String,
//            val slot:Int,
        ) : Modes() {
            companion object {
//                const val SLOT_NONE = 1
//                const val SLOT_A = 2
//                const val SLOT_B = 3
//                const val SLOT_ALL = 4
            }
        }
        object Boot : Modes()
        object Else : Modes()
    }

    companion object {
        private val _title = MutableStateFlow("")
        private val _subTitle = MutableStateFlow("")
        private val _current = MutableStateFlow<Statues>(Launcher)

        var mode by mutableStateOf<Modes>(Modes.Else)
        var title by mutableStateFlowOf(_title)
        var subtitle by mutableStateFlowOf(_subTitle)
        var current by mutableStateFlowOf(_current)

        lateinit var file:File

        @OptIn(ExperimentalFoundationApi::class)
        @Composable
        fun face(file:File) {

            Companion.file = file

            val state1 = _current.toState()
            val state2 = _title.toState()
            val state3 = _subTitle.toState()

            val state: Statues by remember { state1 }
            val title by remember { state2 }
            val subtitle by remember { state3 }
            var dialog by remember { mutableStateOf(false) }
            if (current.help!=null) Column(
                Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = {dialog = !dialog},Modifier.align(Alignment.End).padding(4.dp)) {
                    Text("🤨")
                }
            }
            if (dialog) Dialog(onDismissRequest = {dialog = false}, DialogProperties(icon = Style.Image.flashable,title = ""),) {

                LazyColumn(Modifier.padding(horizontal = 16.dp).padding(top = 16.dp),contentPadding = PaddingValues(bottom = 24.dp)) {
                    current.help!!.lines().groupBy {
                        it.split("|")[0]
                    }.forEach { t, u ->
                        stickyHeader {
                            Box(Modifier.background(Color.LightGray).padding(bottom =  0.5.dp).background(Color.White).fillMaxWidth(),) {
                                Text(t,fontSize = 24.sp,fontWeight = FontWeight.W500,modifier = Style.Padding.bottom)
                            }
                        }
                        items(u.map {
                            val sb1 = StringBuilder()
                            val sb2 = StringBuilder()
                            var label = true
                            var label2 = true
                            for (c in it) {
                                if (label2) {
                                    if (c == '|')
                                        label2 = false
                                } else {
                                    if (label) if (c == ':') {
                                        label = false
                                        continue
                                    }
                                    (if (label) { sb1 } else sb2).append(c)
                                }
                            }
                            sb1.toString() to sb2.toString()
                        }) {(title,content) ->
                            if (title.isNotEmpty()) Text(title,fontSize = 20.sp,fontWeight = FontWeight.Bold)
                            if (content.isNotEmpty()) Text(content,fontSize = 16.sp)
                        }
                    }
                }
            }
            Column(
                Modifier.fillMaxWidth().fillMaxHeight().padding(16.dp)
            ) {
                Text(title, style = Style.Font.h1, modifier = Style.Padding.bottom)
                Text(subtitle, modifier = Style.Padding.bottom)
                state.view(this)
            }
        }
    }

}