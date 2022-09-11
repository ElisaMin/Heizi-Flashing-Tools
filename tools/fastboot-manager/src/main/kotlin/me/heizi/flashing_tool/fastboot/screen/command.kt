package me.heizi.flashing_tool.fastboot.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import me.heizi.flashing_tool.fastboot.repositories.DeviceRunner
import me.heizi.kotlinx.logger.debug

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun fastbootCommand(viewModel: FastbootCommandViewModel, onDismiss:()->Unit={}) {


    AlertDialog({},title = {
        Text("设备:${viewModel.serialId}")
    },text = {
        Column {
            Text(text = "正在请求执行:\n${viewModel.command}",modifier = Modifier.padding(vertical = 6.dp).alpha(
                ContentAlpha.medium))
            if (viewModel.isRunning==true) LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical = 6.dp))
            if (viewModel.isRunning!=false) Text(viewModel.log,modifier = Modifier.padding(vertical = 6.dp).alpha(
                ContentAlpha.medium))
        }
    },confirmButton = {
        if (viewModel.isRunning==false) OutlinedButton(onClick = { viewModel() }){ Text("下一步") }
    },dismissButton = {
        if (viewModel.isRunning!=true) TextButton(onDismiss) { Text("关闭") }
    },
        modifier = Modifier.defaultMinSize(300.dp,200.dp)
    )

}

class FastbootCommandViewModel(
    private val runner: DeviceRunner,
    val command: String,
) {
    val serialId get() = runner.serialId

    var isRunning:Boolean? by mutableStateOf(false)
    var log by mutableStateOf("正在执行中~!\n")

    fun onMessage(message: String) {
        "executor".debug("msg",message)
        log += "${message}\n"
    }
    fun onResult(success:Boolean) {
        "executor".debug("result",log)
        isRunning = null
        log += "\n\n指令执行似乎" + if (!success) "失败了" else "成功了"
    }


    operator fun invoke() {
        isRunning = true
        runner run this
    }

}
