package me.heizi.flashing_tool.image.fragment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lib.ChipCheckBox
import lib.Style
import me.heizi.kotlinx.compose.desktop.core.fragment.Event


class DeviceSelector:WaitingViewModel,Fragment<WaitingViewModel>(_content = @Composable {
    waitingScreen(viewModel)
}) {

    override val isWaiting = mutableStateOf(true)
    override val isEnable = mutableStateOf(true)
    override val devices = mutableStateMapOf<String, Boolean>()
    override val viewModel: WaitingViewModel = this

    val job = GlobalScope.launch {
        TODO()
    }

    init {
        on(Event.Create) { job.start() }
        on(Event.Destroy) { job.cancel() }
    }
    override fun onNextStepBtnChecked() {
        args["nextStep"]
        TODO("Not yet implemented")
    }
}

interface WaitingViewModel:ViewModel {
    val isWaiting: State<Boolean>
    val isEnable: State<Boolean>
    val devices: SnapshotStateMap<String, Boolean>
    fun onNextStepBtnChecked()
}

@Composable
fun waitingScreen(viewModel: WaitingViewModel) {
    val isWaiting by remember { viewModel.isWaiting }
    val isEnable by remember { viewModel.isEnable }
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
            onClick = { if (isEnable) viewModel.onNextStepBtnChecked() },
            enabled = isEnable && !isWaiting, modifier = Modifier.align(Alignment.End)
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