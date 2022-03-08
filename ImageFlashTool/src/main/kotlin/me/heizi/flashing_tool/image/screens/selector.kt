package me.heizi.flashing_tool.image.screens

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
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.heizi.flashing_tool.image.Component
import me.heizi.flashing_tool.image.Fastboot
import me.heizi.flashing_tool.image.ViewModel
import me.heizi.flashing_tool.image.style
import me.heizi.kotlinx.compose.desktop.core.components.ChipCheckBox


class SelectorComponent(
    context: ComponentContext,
    onNextStep:()->Unit
) :ComponentContext by context, Component<WaitingViewModel> {
    override val title: String = "选择设备"
    override val subtitle: String = "你要把文件刷入的那个设备里面?"
    override val viewModel:WaitingViewModel = object : AbstractWaitingViewModel() {
        override fun onNextStepBtnChecked() { onNextStep() }
    }

    private lateinit var job: Job

    init {
        lifecycle.doOnCreate {
            job = (viewModel as AbstractWaitingViewModel).scanningJob
        }
        lifecycle.doOnDestroy {
            job.cancel()
        }
    }

    private fun updateState() {
        (viewModel as AbstractWaitingViewModel).updateState()
    }
    @Composable
    override fun rememberViewModel() = remember {
        updateState()
        viewModel
    }
    @Composable
    override fun render() {
        rememberViewModel().SelectorScreen()
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WaitingViewModel.SelectorScreen() = Column {
    if (isWaiting) {
        LinearProgressIndicator(Modifier.fillMaxWidth())
        Text("正在等待设备.....")
    } else {
        LazyColumn { items(devices.toList()) { (device,checked) ->
            ListItem (
                text = { Text(device) },
                trailing = { ChipCheckBox(checked, onCheck = {onDeviceSelected(device)}) }
            )
    } } }
    Box(style.padding.bottom)
    Button(
        content = {Text("下一步")},
        modifier = Modifier.align(Alignment.End),
        onClick = ::onNextStepBtnChecked,
        enabled = isEnable
    )

}

private abstract class AbstractWaitingViewModel:WaitingViewModel{
    final override val devices: SnapshotStateMap<String, Boolean> = mutableStateMapOf()
    final override var isWaiting: Boolean by mutableStateOf(true)
    final override var isEnable: Boolean by mutableStateOf(false)

    final override fun onDeviceSelected(serialId: String) {
        devices[serialId]?.let {
            devices[serialId] = !it
        }
        updateState()
    }

    val scanningJob get() = Fastboot.scope.launch {
        val job = Fastboot.scannerJob
        val anotherJob = launch {
            Fastboot.deviceSerials.collect(::updateDevice)
        }
        job.join()
        anotherJob.cancel()
    }
    private fun updateDevice(array:Array<String>) {
        isWaiting = true
        isEnable = false
        val hashMap = hashMapOf<String,Boolean>()
        array.forEach {
            hashMap[it] = devices[it]?: false
        }
        devices.clear()
        devices.putAll(hashMap)
        isWaiting = false
        updateState()
    }
    fun updateState() {
        isEnable = !isWaiting && devices.containsValue(true)
    }
}

interface WaitingViewModel: ViewModel {

    val isWaiting: Boolean
    val isEnable: Boolean
    val devices: SnapshotStateMap<String, Boolean>
    fun onDeviceSelected(serialId:String)
    fun onNextStepBtnChecked()
}