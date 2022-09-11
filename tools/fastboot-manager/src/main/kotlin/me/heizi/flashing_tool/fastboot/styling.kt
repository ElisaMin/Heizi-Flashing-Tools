package me.heizi.flashing_tool.fastboot

import androidx.compose.material.TextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import me.heizi.kotlinx.filedialog.FileDialogs
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO

//fun main() = Window {
////    fastbootCommand(FastbootCommandViewModel("ping google.com",FakeDeviceInfo.serialID))
//    var path by remember { mutableStateOf("") }
//    fileInput(path,modifier = Modifier.fillMaxWidth().padding(16.dp)) {path = it }
//}

//val emptyIcon = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).also { it.setRGB(0, 0, 0) }
//val dialogProperties = DialogProperties(
//    title = "",
//    size = IntSize(500, 700),
//    icon = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).also { it.setRGB(0, 0, 0) }
//)
//@Composable
//fun alertDialog(title:String?=null,text:String?=null,onDismiss: () -> Unit={},onConfirm:(()->Unit)?=null) {
//    AlertDialog(onDismiss,properties = dialogProperties,
//        confirmButton = {
//            if (onConfirm != null) TextButton(onClick = onConfirm) { Text("确认")}
//        },text = {
//            if (text!=null) Text(text)
//        },title = {
//            if (title != null) Text(title)
//        }
//    )
//}
//
//




@Composable
fun Title(text:String) {
    Text(text, style = MaterialTheme.typography.titleLarge)
}


@Composable
fun extendableCard(
    title:String = "Nothings",
    initExtend: Boolean = false,
    modifier: Modifier = Modifier,
    content:@Composable ()->Unit
)  = me.heizi.kotlinx.compose.desktop.core.components.ExtendableCard(content = content,initExtend = initExtend,title = {
    Title(title)
}, modifier =  modifier)

//@Composable
//fun extendableCard(
//    initExtend: Boolean = false,
//    modifier: Modifier = Modifier,
//    title:@Composable ()->Unit = {},
//    content:@Composable ()->Unit
//)  = me.heizi.kotlinx.compose.desktop.core.components.ExtendableCard(content = content,initExtend = initExtend,title = {
//    Title(title)
//}, modifier =  modifier)

/**
 * a packaged text field for filepath
 *
 * @param path
 * @param modifier
 * @param title
 * @param hint
 * @param dialogName
 * @param onFilePathChange
 */
@Composable
fun fileInput(
    path:String,
    modifier: Modifier = Modifier,
    title: String = "文件路径",
    hint:String = "请输入路径...",
    dialogName: String ="选择文件",
    onFileDialogDismiss: () -> Unit ={},
    onFileDialogOpen: () -> Unit ={},
    onFilePathChange:(String)->Unit
) {
    TextField(path, onFilePathChange, modifier, trailingIcon = {
        TextButton(
            onClick = {
                CoroutineScope(Default).launch {
                    onFileDialogOpen()
                    when (val file = FileDialogs.choose(
                        title = dialogName,
                    )) {
                        is FileDialogs.Result.Single -> {
                            onFilePathChange(file.result.canonicalPath)
                        }
                        else -> {}
                    }
                    onFileDialogDismiss()
                }
            },
        ) {
            Text("选择文件")
        }
    }, placeholder = {
        Text(hint)
    }, label = {
        Text(title)
    })
}

object Resources {
    private fun getResources(name:String): URL? = this::class.java.classLoader.getResource(name)
    object Urls {
        val disconnect get() = getResources("ic_disconnect.png")
        val connected get() = getResources("ic_connected.png")
        val fastboot get() = getResources("ic_fastboot.png")
    }
//    object Images {
//        val disconnect by lazy {
//            ImageIO.read(Urls.connected)
//        }
//    }
}
val fastbootIconBuffered: BufferedImage = ImageIO.read(Resources.Urls.fastboot!!)