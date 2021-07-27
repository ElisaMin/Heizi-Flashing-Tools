package me.heizi.flashing_tool.vd.fb

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.v1.DialogProperties
import java.awt.image.BufferedImage

//fun main() = Window {
////    fastbootCommand(FastbootCommandViewModel("ping google.com",FakeDeviceInfo.serialID))
//    var path by remember { mutableStateOf("") }
//    fileInput(path,modifier = Modifier.fillMaxWidth().padding(16.dp)) {path = it }
//}

val dialogProperties = DialogProperties(
    title = "",
    size = IntSize(500, 700),
    icon = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).also { it.setRGB(0, 0, 0) }
)
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
    Text(text,fontSize = 28.sp)
}


@Composable
fun extendableCard(
    title:String = "Nothings",
    initExtend: Boolean = false,
    content:@Composable ()->Unit
)  = me.heizi.kotlinx.compose.desktop.core.components.extendableCard(content = content,initExtend = initExtend,title = {
    Title(title)
})
