@file:JvmName("Main")
package me.heizi.flashing_tool.sideloader

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.flow.MutableStateFlow
import me.heizi.flashing_tool.adb.ADBDevice
import me.heizi.flashing_tool.sideloader.screens.invoke
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

/**
 * false if apk parse success in initializing progress
 */
var isSideload by mutableStateOf(false)

val files = MutableStateFlow(listOf<File>())

val context: MutableStateFlow<Context> =
    MutableStateFlow(Context.Ready)
operator fun List<ADBDevice>.get(serial:String) = find { it.serial == serial }


val colors = compositionLocalOf {
    lightColorScheme()
}

object Resources {
    operator fun get(name:String): URL? = this::class.java.classLoader.getResource(name)

    val iconASTUgly = ImageIO.read(this["ic_ast_ugly.png"]!!).toPainter()
}

fun main(args: Array<String>) {
    println("官网: dl.lge.fun 或 tools.lge.fun\nQQ群: 549674080")
    requireAndStuck(args.size==1) {
        "请输入正确的文件地址"
    }
    files.value = listOf(File(args[0]))
    singleWindowApplication(
        title = "",
        icon = Resources.iconASTUgly,
        exitProcessOnExit = true,
        state = WindowState(position = WindowPosition(Alignment.Center))
    ){
        val scope = rememberCoroutineScope()
        val currentContext by context.collectAsState(context = scope.coroutineContext)
//        val primaryColor = MaterialTheme.colorScheme.primary

        LaunchedEffect("init", files) {
            // init color file
            files.value.getOrNull(0)?.let {
                context.emit(Context(it))
            }
        }
        when(val context = currentContext) {
            is Context.Ready -> {
                context()
            }
            is SingleFileContext -> {
                context.invoke()
            }
            is Context.Invoke -> {

            }
            else -> Unit
        }



    }

}

fun requireAndStuck(b: Boolean, function: () -> String) {
    if (!b) {
        println(function())
        while (true) Unit
    }
}

