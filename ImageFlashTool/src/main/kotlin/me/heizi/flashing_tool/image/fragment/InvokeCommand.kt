package me.heizi.flashing_tool.image.fragment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.heizi.kotlinx.compose.desktop.core.fragment.Event
import me.heizi.kotlinx.shell.CommandResult
import me.heizi.kotlinx.shell.CommandResult.Companion.waitForResult
import me.heizi.kotlinx.shell.shell


const val Invoke_FAILED = 3f
const val Invoke_SUCCESS = 4f
@Suppress("UNCHECKED_CAST")
class InvokeCommand:InvokeViewModel,Fragment<InvokeViewModel>(_content = @Composable {
    subtitle = if (viewModel.progress.value>1)
        "执行完成，如果有什么不对我也不知道为什么。" else "请耐心等待执行完成。"
    title = invokeScreen(viewModel)
} ) {
    override val text: MutableState<String> = mutableStateOf("")
    override val progress: MutableState<Float> = mutableStateOf(0.0f)
    init {
        on(Event.Create,::onCreate)
    }


    val devices get() = args["devices"] as Iterable<String>
    val command get() = when(args["launchMode"]) {
        "flash" -> {
            val partition = args["partition"] as String
            val (_a,_b,disableAvb) = args["bools"] as Triple<Boolean,Boolean,Boolean>
            devices.map {
                "fastboot -s $it ${if(disableAvb) "--disable-verification --disable-verity" else ""} flash $partition"
            }.flatMap { command -> sequence {
                if (!_a && !_b) yield(command)
                if (_a) yield("${command}_a")
                if (_b) yield("${command}_b")
            } }.map { command -> "$command $file" }
                .toTypedArray()
        }
        "boot" -> {
            devices.map { device->
                "fastboot -s $device boot $file  "
            }.toTypedArray()
        }
        else -> args["command"] as Array<String>
    }
//    val prefix get() = args["prefix"] as Array<String>?


    private fun onCreate() {
//        if (prefix!=null)
        var progress by progress
        var text by text
        GlobalScope.launch {
            delay(1000)
            progress = 0.11f
            launch {
                shell(*command).waitForResult(
                    onMessage = { text += "\n::$it" },
                    onError = { text+="\n!!$it"},
                    onResult = {
                        runBlocking {
                            progress = 0.9f
                            repeat(3) {
                                delay(500)
                                progress+=0.03f
                            }
                        }
                        progress = if (it is CommandResult.Success) Invoke_SUCCESS else Invoke_FAILED
                    }
                )
            }
        }
    }


    override val viewModel: InvokeViewModel = this
}


interface InvokeViewModel:ViewModel {
    val text: State<String>
    val progress: State<Float>
}



@Composable
fun invokeScreen(viewModel: InvokeViewModel):String {
    val progressing by remember { viewModel.progress }
    return if (progressing<1f) {
        Column {
            val text by remember { viewModel.text }
            progress(progressing)
            Text(text)
        }
        "运行中"
    } else {
        OutlinedTextField(
            viewModel.text.value ,
            onValueChange = {},
            label = { Text("结果") },
            modifier = Modifier.padding(3.dp).fillMaxWidth().fillMaxHeight(),
        )
        "执行" + when (progressing) {
            Invoke_SUCCESS -> "成功"
            Invoke_FAILED -> "成功"
            else -> throw IllegalStateException("$progressing is not success or failed")
        }
    }
}
@Composable
fun progress(progress:Float) {
    if (progress!=0.11f) LinearProgressIndicator(progress, Modifier.fillMaxWidth())
    else LinearProgressIndicator(Modifier.fillMaxWidth())
}