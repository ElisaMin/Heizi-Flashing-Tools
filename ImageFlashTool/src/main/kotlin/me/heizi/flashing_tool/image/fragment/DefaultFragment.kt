package me.heizi.flashing_tool.image.fragment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import lib.Style
//import me.heizi.flashing_tool.image.Fragment

open class Fragment {
    @Composable
    open fun setContent(block:@Composable () -> Unit):Unit {}
}
abstract class DefaultFragment <VM:ViewModel> : Fragment() {
    abstract val title:String
    abstract val subtitle:String
    abstract val viewModel:VM
    abstract val content:@Composable ColumnScope.(VM)->Unit
    @Composable
    final override fun setContent(block:@Composable () -> Unit):Unit = Column {
        title(title,subtitle)
        content.invoke(this, viewModel)
    }

    companion object {
        @Composable
        fun title(title: String,subtitle: String) {
            Text(title, style = Style.Font.h1, modifier = Style.Padding.bottom)
            Text(subtitle, modifier = Style.Padding.bottom)
        }
    }
}