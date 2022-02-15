package me.heizi.flashing_tool.fastboot.screen.panel

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun panelAbSlotSwitch(isSlotA:Boolean,onClick :(Boolean)->Unit) = TextButton(onClick = {
    onClick.invoke(isSlotA)
}, modifier = Modifier.padding(16.dp)) {
    val bottom = Modifier.align(Alignment.Bottom)

    @Composable
    fun text(text: String, enable: Boolean) = if (enable)
        Text(text, fontSize = 72.sp, fontWeight = FontWeight.Bold, modifier = bottom) else
        Text(text, fontSize = 47.sp, modifier = bottom.padding(bottom = 6.dp), color = Color.LightGray)

    text("A", isSlotA)
    text("B", !isSlotA)
}
