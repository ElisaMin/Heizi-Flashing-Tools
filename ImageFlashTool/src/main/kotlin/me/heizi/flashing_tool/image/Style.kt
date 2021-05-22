package me.heizi.flashing_tool.image

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object Style {
    object Font {
        val default get() = TextStyle.Default.copy(
            fontSize = 16.sp
        )
        val h1 get() = default.copy(
            fontSize = 24.sp,
            fontWeight = FontWeight.W600
        )
    }
    object Padding {
        val bottom get() = Modifier.padding(bottom = 12.dp)
        val end get() = Modifier.padding(end = 12.dp)
        val vertical get() = Modifier.padding(vertical = 12.dp)
    }
    object Image {
        val flashable = ImageIO.read(javaClass.classLoader.getResource("icon.png"))
        val emptyIcon = BufferedImage(1,1, BufferedImage.TYPE_INT_ARGB)
    }
}