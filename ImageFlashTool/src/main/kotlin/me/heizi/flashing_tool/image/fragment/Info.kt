package me.heizi.flashing_tool.image.fragment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import lib.Style

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
class InfoFragment:DefaultFragment<InfoViewModel>() {
    override val title: String
        get() = TODO("Not yet implemented")
    override val subtitle: String
        get() = TODO("Not yet implemented")
    override val viewModel: InfoViewModel
        get() = TODO("Not yet implemented")
    override val content: ColumnScope.(InfoViewModel) -> Unit
        get() = TODO("Not yet implemented")
}

@Composable
fun showInfo(title:String, content: String,modifier: Modifier) = OutlinedTextField(
    onValueChange = { },
    value = content,
    label = { Text(title) },
    enabled = false,modifier = modifier
)
@Composable
fun showInfoScreen(viewModel: InfoViewModel) = Column {
    val modifier = Modifier.fillMaxWidth()
//    viewModel.data.forEach { key, value ->
//        showInfo(key,value,modifier)
//    }
    Box(Style.Padding.bottom)
    Button(
        onClick = { viewModel.onNextStepBtnClicked() },
        modifier = Modifier.align(Alignment.End),
    ) {
        Text("下一步")
    }
}