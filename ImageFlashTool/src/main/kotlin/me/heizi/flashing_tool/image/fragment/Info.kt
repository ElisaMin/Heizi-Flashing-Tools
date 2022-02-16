package me.heizi.flashing_tool.image.fragment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.heizi.flashing_tool.image.Style
import me.heizi.kotlinx.compose.desktop.core.fragment.Event

@Suppress("UNCHECKED_CAST")
class InfoFragment :InfoViewModel, Fragment<InfoViewModel>(_content = @Composable {
    showInfoScreen(viewModel)
}) {

    override var bools: Triple<Boolean, Boolean, Boolean> by mutableStateOf(Triple(false,false,false))
    override var partition: String by mutableStateOf("")
    override var device =  mutableListOf<String>()
    override val file: String = Fragment.file.toString()

    init {
        on(Event.Create) {
            GlobalScope.launch {
                delay(300)
                bools = Triple(
                    args["_a"] as Boolean,
                    args["_b"] as Boolean,
                    args["disable_avb"] as Boolean
                )
                partition = args["partition"] as String
                device.addAll(
                    args["devices"] as Iterable<String>
                )
            }
        }
    }

    override fun onNextStepBtnClicked() {
        handler.go(InvokeCommand::class,*args.toList().toTypedArray(),"bools" to bools)
    }

    override val viewModel: InfoViewModel = this
}

interface InfoViewModel:ViewModel {
    //_a _b disableAVB
    val bools:Triple<Boolean,Boolean,Boolean>
    val partition:String
    val device:List<String>
    val file:String
    val data get() = HashMap<String,String>().apply {
        this["💿文件"] = file
        this["📱设备"] = device.joinToString(",")
        this["🍰分区"] = buildString {
            val (a,b,disable) = bools
            fun afterPtt(string: String) {
                append(partition)
                append(string)
                append(" ")
            }
            if (!a && !b) afterPtt("")
            if (a) afterPtt("_a")
            if (b) afterPtt("_b")
            if (disable) append(" disable verity/verification")
        }
    }
    fun onNextStepBtnClicked()
}
@Composable
fun showInfoScreen(viewModel: InfoViewModel) = Column {
    val modifier = Modifier.fillMaxWidth()
    for ((key,value) in viewModel.data) {
        showInfo(key,value,modifier)
    }
    Box(Style.Padding.bottom)
    Button(
        onClick = { viewModel.onNextStepBtnClicked() },
        modifier = Modifier.align(Alignment.End),
    ) {
        Text("下一步")
    }
}
@Composable
fun showInfo(title:String, content: String,modifier: Modifier) = OutlinedTextField(
    onValueChange = { },
    value = content,
    label = { Text(title) },
    enabled = false,modifier = modifier
)
