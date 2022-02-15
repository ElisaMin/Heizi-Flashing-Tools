package me.heizi.flashing_tool.vd.fb

import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skiko.toImage
import java.net.URL
import java.nio.charset.Charset
import javax.imageio.ImageIO

fun read(url: URL) = ImageIO.read(url).toImage().toComposeImageBitmap()
fun String.toGBK() =
    String(toByteArray(), Charset.forName("GBK"))
