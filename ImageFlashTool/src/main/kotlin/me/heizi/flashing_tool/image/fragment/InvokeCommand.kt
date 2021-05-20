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
import me.heizi.kotlinx.compose.desktop.core.fragment.Event


const val Invoke_FAILED = 3f
const val Invoke_SUCCESS = 4f


class InvokeCommand:InvokeViewModel,Fragment<InvokeViewModel>(_content = @Composable {
    invokeScreen(viewModel)
} ) {
    override val text: MutableState<String> = mutableStateOf("")
    override val progress: MutableState<Float> = mutableStateOf(0.0f)
    init {
        on(Event.Create) {
            TODO()
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
        //TODO("ShowTitle")
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