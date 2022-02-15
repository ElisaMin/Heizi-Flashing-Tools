package me.heizi.flashing_tool.vd.fb

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.awt.image.BufferedImage

//fun main() = Window {
////    fastbootCommand(FastbootCommandViewModel("ping google.com",FakeDeviceInfo.serialID))
//    var path by remember { mutableStateOf("") }
//    fileInput(path,modifier = Modifier.fillMaxWidth().padding(16.dp)) {path = it }
//}

val emptyIcon = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).also { it.setRGB(0, 0, 0) }
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
    Text(text, style = MaterialTheme.typography.h5)
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
