package me.heizi.flashing_tool.image.fragment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ListItem
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.heizi.flashing_tool.image.Style
import me.heizi.kotlinx.compose.desktop.core.components.ChipCheckBox
import me.heizi.kotlinx.compose.desktop.core.fragment.Event
import me.heizi.kotlinx.logger.debug
import me.heizi.kotlinx.shell.CommandResult
import me.heizi.kotlinx.shell.Shell


class DeviceSelector:WaitingViewModel,Fragment<WaitingViewModel>(_content = @Composable {
    title = "选择设备"
    subtitle = "你要把文件刷入的那个设备里面?"
    waitingScreen(viewModel)
}) {
    override val isWaiting = mutableStateOf(true)
    override val isEnable get() = devices.containsValue(true)
    override val devices = mutableStateMapOf<String, Boolean>()
    override val viewModel: WaitingViewModel = this

    val job = GlobalScope.launch {
        var isWaiting by isWaiting
        while (true) {
            delay(200)
            isWaiting = devices.isEmpty()
            Shell("fastboot devices").await().let {
                if (it is CommandResult.Success) it.runCatching {
//                    val list = arrayListOf<String>()
                    for (line in message.lines()) {
                        val test = line.split("\t")
                        "device".debug(test, line)
                        if (test.size != 2 && test[1] != "fastboot") break
//                        list.add(test[0])
                        devices[test[0]] = devices[test[0]] ?: false
                        "device".debug(test, line)
                    }
                    "device".debug(devices.map { "${it.key} is ${it.value}" }.joinToString(","))
//                    list.map { s ->
//                        s to devices[s]!!
//                    }.let {
//                        devices.clear()
//                        devices.putAll(it)
//                    }

                }
            }
            delay(2000)
        }
    }

    init {
        on(Event.Create) {
            job.start()
        }
        on(Event.Destroy) { job.cancel() }
    }
    override fun onNextStepBtnChecked() {
        val boot = when(val mode = args["launchMode"]) {
            "flash"-> InfoFragment::class
            "boot" -> InvokeCommand::class
            else -> throw IllegalStateException("unknown mode $mode")
        }
        val devices = this.devices.filter { it.value }.keys

        handler.go(boot,*args.toList().toTypedArray(),"devices" to devices)
    }
}

interface WaitingViewModel:ViewModel {
    val isWaiting: State<Boolean>
    val isEnable: Boolean
    val devices: SnapshotStateMap<String, Boolean>
    fun onNextStepBtnChecked()
}

@Composable
fun waitingScreen(viewModel: WaitingViewModel) {
    val isWaiting by remember { viewModel.isWaiting }
//    val isEnable by remember { viewModel.isEnable }

    Column {
        if (isWaiting) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text("正在等待设备.....")
        } else LazyColumn(Modifier.weight(2f)) {
            items(viewModel.devices.toList()) {(device,checked) ->
                deviceChooseItem(device,checked) {
                    viewModel.devices[device] = !it
                }
            }
        }
        Box(Style.Padding.bottom)
        Button(
            onClick = { if (viewModel.isEnable) viewModel.onNextStepBtnChecked() },
            enabled = viewModel.isEnable && !isWaiting, modifier = Modifier.align(Alignment.End)
        ) {
            Text("下一步")
        }
    }
}
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun deviceChooseItem(deviceId:String, checked:Boolean, onChecked:(Boolean)->Unit) = ListItem(trailing = {
    ChipCheckBox(checked,onCheck = onChecked)
}) {
    Text(deviceId)
}