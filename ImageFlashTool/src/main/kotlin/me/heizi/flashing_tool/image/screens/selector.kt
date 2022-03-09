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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.heizi.flashing_tool.image.Component
import me.heizi.flashing_tool.image.Fastboot
import me.heizi.flashing_tool.image.ViewModel
import me.heizi.flashing_tool.image.style
import me.heizi.kotlinx.compose.desktop.core.components.ChipCheckBox


class SelectorComponent(
    context: ComponentContext,
    onNextStep:(Array<String>)->Unit
) :ComponentContext by context, Component<WaitingViewModel> {
    override val title: String = "选择设备"
    override val subtitle: String = "你要把文件刷入的那个设备里面?"
    override val viewModel:WaitingViewModel = object : AbstractWaitingViewModel() {
        override fun afterClick(devices: Array<String>) {
            job.cancel()
            runBlocking {
                job.join()
            }
            onNextStep(devices)
        }
    }

    private val job: Job = (viewModel as AbstractWaitingViewModel).scanningJob

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
    private lateinit var realJob:Job
    private lateinit var realJob2:Job

    val scanningJob get() = Fastboot.scope.launch {
        realJob = Fastboot.scannerJob
        realJob2 = launch {
            Fastboot.deviceSerials.collect(::updateDevice)
        }
        realJob.join()
        realJob2.cancel()
    }.also {
        it.invokeOnCompletion {
            realJob.cancel()
            realJob2.cancel()
        }
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
        onDebug()
        if (devices.isEmpty()) isWaiting = true
        isEnable = !isWaiting && devices.containsValue(true)
    }
    fun onDebug() {
        "afakedevice".let {
            devices[it] = devices[it]?:false
        }
    }
    abstract fun afterClick(devices: Array<String>)
    final override fun onNextStepBtnChecked() {
        afterClick(devices.filter { it.value }.keys.toTypedArray())
    }
}

interface WaitingViewModel: ViewModel {

    val isWaiting: Boolean
    val isEnable: Boolean
    val devices: SnapshotStateMap<String, Boolean>
    fun onDeviceSelected(serialId:String)
    fun onNextStepBtnChecked()
}