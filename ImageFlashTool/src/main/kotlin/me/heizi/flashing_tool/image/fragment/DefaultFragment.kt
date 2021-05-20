package me.heizi.flashing_tool.image.fragment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import lib.Style
import me.heizi.kotlinx.compose.desktop.core.fragment.AbstractFragment
import me.heizi.kotlinx.compose.desktop.core.value
import kotlin.reflect.KProperty


abstract class Fragment <VM:ViewModel> (
    _content:@Composable Fragment<VM>.() -> Unit
) : AbstractFragment() {
    var title by value(_title)
    var subtitle by value(_subtitle)
    abstract val viewModel:VM
    final override val content = @Composable {
        contentContainer {
            _content(this@Fragment)
        }
    }

    companion object {
        private val _title: MutableState<String> = mutableStateOf("")
        private val _subtitle: MutableState<String> = mutableStateOf("")
        @Composable
        private fun contentContainer(block:@Composable ()->Unit) = Column {
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