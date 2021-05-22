 package me.heizi.flashing_tool.image.fragment

import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.heizi.flashing_tool.image.Style
import me.heizi.kotlinx.compose.desktop.core.fragment.AbstractFragment
import me.heizi.kotlinx.compose.desktop.core.fragment.FragmentContainer
import me.heizi.kotlinx.compose.desktop.core.fragment.handlerOf
import java.io.File

 interface ViewModel


 abstract class Fragment <VM:ViewModel> (
    _content:@Composable Fragment<VM>.() -> Unit
) : AbstractFragment() {
    var title by _title
    var subtitle by _subtitle
    abstract val viewModel:VM
    final override val content:@Composable ()->Unit = {
        contentContainer {
            _content(this@Fragment)
        }
    }

    companion object {

        lateinit var file:File
        fun start(file: File) {
            this.file = file
            Window {
                MaterialTheme {
                    FragmentContainer(handlerOf(
                        Launcher::class,
                        InvokeCommand::class,
                        DeviceSelector::class,
                        InfoFragment::class,
                    ))
                }
            }
        }

        private val _title: MutableState<String> = mutableStateOf("")
        private val _subtitle: MutableState<String> = mutableStateOf("")

        @Composable
        private fun contentContainer(block:@Composable ()->Unit) = Column(
            Modifier.padding(16.dp)
        ) {
            val title by remember { _title }
            val subtitle by remember { _subtitle }
            title(title,subtitle)
            Box(Style.Padding.bottom)
            block()
        }
        @Composable
        private fun title(title:String,subtitle:String) {
            Text(title, style = Style.Font.h1, modifier = Style.Padding.bottom)
            if (subtitle.isNotEmpty()) Text(subtitle, modifier = Style.Padding.bottom)
        }

    }
}