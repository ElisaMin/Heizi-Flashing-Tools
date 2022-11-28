package me.heizi.flashing_tool.sideloader

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toPainter
import kotlinx.coroutines.flow.MutableStateFlow
import me.heizi.flashing_tool.adb.ADBDevice
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


object Resources {
    operator fun get(name:String): URL? = this::class.java.classLoader.getResource(name)

    val iconASTUgly = ImageIO.read(this["ic_ast_ugly.png"]!!).toPainter()
}

fun main(args: Array<String>) {

}