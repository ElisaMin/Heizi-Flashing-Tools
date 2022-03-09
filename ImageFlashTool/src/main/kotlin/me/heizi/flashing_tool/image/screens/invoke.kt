package me.heizi.flashing_tool.image.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnStart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.heizi.flashing_tool.image.Component
import me.heizi.flashing_tool.image.ViewModel
import me.heizi.kotlinx.shell.ProcessingResults
import me.heizi.kotlinx.shell.Shell


class InvokeComponent(
    context: ComponentContext,
    updateTitle:(String)->Unit,
    updateSubTitle:(String)->Unit,
    shell: Shell
) : ComponentContext by context, Component<InvokeViewModel> {
    override val title: String = "运行中"
    override val subtitle: String = "请耐心等待执行完成。"
    override val viewModel: InvokeViewModel = object : AbstractInvokeViewModel(shell) {
        override fun updateTitle(string: String) { updateTitle(string) }
        override fun updateSubTitle(string: String) { updateSubTitle(string) }
    }
    @Composable
    override fun render() {
        rememberViewModel().RunScreen()
    }
    init {
        lifecycle.doOnStart {
            CoroutineScope(Default).launch {
                (viewModel as AbstractInvokeViewModel)
                    .collect()
            }
        }
    }

}

@Composable
fun InvokeViewModel.RunScreen() {
    if (progress<1f) Column {
        if (progress!=0.11f)
             LinearProgressIndicator(progress, Modifier.fillMaxWidth())
        else LinearProgressIndicator(Modifier.fillMaxWidth())
        Text(text)
    } else {
        OutlinedTextField(
            text,
            onValueChange = {},
            label = { Text("结果") },
            modifier = Modifier.padding(3.dp).fillMaxWidth().fillMaxHeight(),
        )
    }
}
interface InvokeViewModel:ViewModel {
    val text:String
    val progress:Float
    fun updateTitle(string: String)
    fun updateSubTitle(string: String)
}
private abstract class AbstractInvokeViewModel(
    private val shell: Shell
):InvokeViewModel {
    final override var text by mutableStateOf("")
    final override var progress: Float by mutableStateOf(0f)
    suspend fun collect() {
        progress = 0.11f
        shell.collect {when(it) {
            is ProcessingResults.CODE ->  runBlocking {
                progress = 0.91f
                repeat(3) {
                    delay(500)
                    progress+=0.03f
                }
                ("执行" + if (it.code == 0) "成功" else "失败")
                    .let(::updateTitle)
                progress = 1.1f
            }

            is ProcessingResults.Message -> {
                text+="\n::${it.message}"
            }
            is ProcessingResults.Error -> {
                text+="\n!!${it.message}"
            }

            else -> updateSubTitle("执行完成，如果有什么不对我也不知道为什么。")

        } }
    }

}