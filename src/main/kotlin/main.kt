import androidx.compose.desktop.Window
import androidx.compose.material.MaterialTheme
import java.io.File


fun main() = Window(
    title = "刷机工具",
    icon = Style.Image.flashable
) { MaterialTheme {
    println(File("src/main/resource/icon.png").absoluteFile)
    Statues.face(File( "D:\\Downloads\\ofox_1.8.img"))
} }







