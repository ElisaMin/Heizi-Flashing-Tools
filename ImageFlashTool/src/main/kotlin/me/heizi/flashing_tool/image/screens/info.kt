package me.heizi.flashing_tool.image.screens

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ComponentContext
import me.heizi.flashing_tool.image.Component
import me.heizi.flashing_tool.image.Context
import me.heizi.flashing_tool.image.Fastboot.command
import me.heizi.flashing_tool.image.ViewModel
import me.heizi.flashing_tool.image.style


fun Context.toMap() = buildMap {
    this["\uD83D\uDCDC指令"] = command.joinToString("\n")
    this["💿文件"] = path
    this["📱设备"] = devices.joinToString(", ")

    this["\uD83C\uDFF7模式"] = when(this@toMap) {
        is Context.Boot -> "启动镜像"
    is Context.Flash -> {
        this["\uD83C\uDF70分区"] = partitions.joinToString(", ")+if (disableAVB)
            ", disable avb" else ""
        "刷入镜像"
    }
    else -> error("shouldn't in this mode")
}  }

class InfoComponent(
    context: ComponentContext,
    fastbootContext: Context,
    onNextStep:()->Unit
) : ComponentContext by context, Component<InfoViewModel> {
    override val title: String = "信息确认"
    override val subtitle: String = "请确认一下你正在做的操作。"
    override val viewModel: InfoViewModel = object : InfoViewModel {
        override val infos: Map<String, String> = fastbootContext.toMap()
        override fun onNextStepBtnClicked() { onNextStep() }
    }

    @Composable
    override fun render() {
        rememberViewModel().Display()
    }

}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun InfoViewModel.Display() = Column {
    Box(style.padding.bottom.fillMaxWidth().weight(1f)) {
        val scroll = rememberLazyListState()
        LazyColumn(Modifier.fillMaxWidth().padding(end = 10.dp),scroll) { items(infos.toList()) { (title,text) ->
            ListItem(
                text={ Text(title) },
                secondaryText = { Text(text) },
                singleLineSecondaryText = false,
            )
        } }
        VerticalScrollbar(rememberScrollbarAdapter(scroll),Modifier.fillMaxHeight().align(Alignment.CenterEnd))
    }
    Button(
        content = {Text("下一步")},
        onClick = ::onNextStepBtnClicked,
        modifier = Modifier.align(Alignment.End),
    )
}

interface InfoViewModel:ViewModel {
    val infos:Map<String,String>
    fun onNextStepBtnClicked()
}
