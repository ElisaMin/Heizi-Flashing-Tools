package me.heizi.flashing_tool.image.fragment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


const val Invoke_FAILED = 3f
const val Invoke_SUCCESS = 4f

interface InvokeViewModel {
    val text: State<String>
    val progress: State<Float>
}
@Composable
fun invokeScreen(viewModel: InvokeViewModel) {
    val progressing by remember { viewModel.progress }
    if (progressing>1f) Column {
        val text by remember { viewModel.text }
        progress(progressing)
        Text(text)
    } else {
        //TODO("ShowTitle")
        OutlinedTextField(
            viewModel.text.value ,
            onValueChange = {},
            label = { Text("结果") },
            modifier = Modifier.padding(3.dp).fillMaxWidth().fillMaxHeight(),
        )
    }
}
@Composable
fun progress(progress:Float) {
    if (progress!=0.11f) LinearProgressIndicator(progress, Modifier.fillMaxWidth())
    else LinearProgressIndicator(Modifier.fillMaxWidth())
}