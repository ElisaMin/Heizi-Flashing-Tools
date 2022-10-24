package me.heizi.flashing_tool.sideloader

import androidx.compose.ui.graphics.toPainter
import java.net.URL
import javax.imageio.ImageIO

object Resources {
    operator fun get(name:String): URL? = this::class.java.classLoader.getResource(name)

    val iconASTUgly = ImageIO.read(this["ic_ast_ugly.pg"]!!).toPainter()

}