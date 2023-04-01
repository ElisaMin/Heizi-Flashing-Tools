package me.heizi.kotlinx.compose.desktop.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.primarySurface
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ChipCheckBox (
    check:Boolean,
    text:String?=null,
    modifier: Modifier = Modifier,
    onCheck:(Boolean)->Unit = {},
) {
    val light = colors.isLight
    val color = remember (colors,colors.isLight)  { if (light) Color.LightGray else Color.DarkGray }
//    color = remember(colors,check,text,light) {
//        if (light) color.copy(alpha = if (check) 1f else 0.3f)
//        else color.copy(alpha = if (check) 1f else 0.7f)
//    }

    OutlinedButton(
        colors = ButtonDefaults.buttonColors(
            containerColor = if (!check) color else colors.primarySurface.copy(alpha = 0.3f),
            contentColor = // if (enableState) Color.White else
                colors.onSurface
        ),
        shape = CircleShape,
        modifier = modifier,
        onClick = {
            onCheck(check)
        },border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Box(Modifier.width(24.dp).height(20.dp)) {
            Text( if(check)"\uD83D\uDC4C" else "\uD83D\uDC4B",modifier = Modifier,
//                fontSize = 12.sp
            )
        }
        Text(text?:"")
    }
}