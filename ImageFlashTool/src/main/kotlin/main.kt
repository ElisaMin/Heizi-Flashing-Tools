
//import lib.status.Statues
import androidx.compose.desktop.Window
import androidx.compose.material.MaterialTheme
import lib.Style
import me.heizi.kotlinx.logger.println
import java.io.File


val types get() = arrayOf("bin","img")

fun window(file: File) = Window(
    title = "刷机工具",
    icon = Style.Image.flashable
) { MaterialTheme {

//    var isNormalImageFormat by remember { mutableStateOf(false) }
//    if (file.exists()) file.name.split(".").let {
//        if (it.size > 1) if (types.contains(it.last())) {
//            isNormalImageFormat = true
//        }
//    }
//    if (!isNormalImageFormat) {
//        Dialog(onDismissRequest = {}) {
//
//        }
//    }
//    Statues.face(file)
} }



fun main(file: File) {
    "env".println("file",file)
    "env".println("running")
//    Khell.env = hashMapOf(
//        "path" to "%path%;lib/"
//    )
    "env".println("env args")
    "env".println("launching window")
    window(file)
}
fun main() {
    main(File("src/main/resource/icon.png"))
}


fun main(args: Array<String>) {
    main(File(args[0]))
}







