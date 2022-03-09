package me.heizi.flashing_tool.image.fragment


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import me.heizi.flashing_tool.image.style
import me.heizi.kotlinx.compose.desktop.core.fragment.AbstractFragment
import me.heizi.kotlinx.compose.desktop.core.fragment.FragmentContainer
import me.heizi.kotlinx.compose.desktop.core.fragment.handlerOf
import java.io.File
/**
 * 程序片里面的界面和数据交流的对象 通常里面存在很多State
 * 并且对象实例是接近单例的
 */
interface ViewModel
/**
 * Fragment 程序片
 *
 * @param _content 程序片展示的内容
 */
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
        /**
         * 启动窗口
         *
         * @param file 要刷入的文件的完整路径 非相对路径
         */
        fun start(file: File) {
            this.file = file
            singleWindowApplication(title = "",icon = style.Image.flashable.toPainter(), state = WindowState(size = DpSize(600.dp,460.dp))) {
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

        /**
         * Fragment的标题和副标题
         *
         * @param block
         */
        @Composable
        private fun contentContainer(block:@Composable ()->Unit) = Column(
            Modifier.padding(16.dp)
        ) {
            val title by remember { _title }
            val subtitle by remember { _subtitle }
            title(title,subtitle)
            Box(style.padding.bottom)
            block()
        }
        @Composable
        private fun title(title:String,subtitle:String) {
            Text(title, style = style.font.h1, modifier = style.padding.bottom)
            if (subtitle.isNotEmpty()) Text(subtitle, modifier = style.padding.bottom)
        }

    }
}