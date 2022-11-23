package me.heizi.flashing_tool.sideloader

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toPainter
import java.net.URL
import javax.imageio.ImageIO

/**
 * false if apk parse success in initializing progress
 */
var isSideload by mutableStateOf(false)


object Resources {
    operator fun get(name:String): URL? = this::class.java.classLoader.getResource(name)

    val iconASTUgly = ImageIO.read(this["ic_ast_ugly.png"]!!).toPainter()
}