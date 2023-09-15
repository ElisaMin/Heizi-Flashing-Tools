package me.heizi.flashing_tool.sideloader

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toPainter
import kotlinx.coroutines.flow.MutableStateFlow
import me.heizi.flashing_tool.adb.ADBDevice
import me.heizi.flashing_tool.sideloader.contexts.Context
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

val context: MutableStateFlow<Context?> =
    MutableStateFlow(Context.Ready)

val colors: ColorScheme
    @Composable
    get() {
        return MaterialTheme.colorScheme
    }
val ColorScheme.current get() = this

var contextReady by mutableStateOf(false)

/**
 * false if apk parse success in initializing progress
 */
var isSideload by mutableStateOf(false)

val files = MutableStateFlow(listOf<File>())

object Resources {
    operator fun get(name:String): URL? = this::class.java.classLoader.getResource(name)
    object URLs {
        val iconASTUgly = Resources["ic_ast_ugly.png"]!!
    }
    @JvmStatic
    val iconASTUgly by lazy { ImageIO.read(URLs.iconASTUgly).toPainter() }
}

operator fun List<ADBDevice>.get(serial:String) = find { it.serial == serial }

