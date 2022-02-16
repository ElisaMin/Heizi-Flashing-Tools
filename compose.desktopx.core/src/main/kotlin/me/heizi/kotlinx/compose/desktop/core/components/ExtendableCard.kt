package me.heizi.kotlinx.compose.desktop.core.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ExtendableCard(
    initExtend: Boolean = false,
    modifier: Modifier = Modifier,
    elevation: Dp = 3.dp,
    title:@Composable ()->Unit,
    content:@Composable ()->Unit
) {
    var state by remember { mutableStateOf(initExtend) }
    val padding = Modifier.padding(8.dp)

    Card(modifier = padding.defaultMinSize(minWidth = 127.dp).then(modifier)
        .width(180.dp),elevation = elevation) {
        Column(modifier = padding.fillMaxWidth()) {

            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                title()
                TextButton(onClick = {state=!state}) { Text(if (!state)"◀" else "▼") }
            }
            if (state) {
                content()
            } else {
                Box(Modifier.fillMaxWidth().height(0.dp).padding(2.dp).border(1.dp, Color.Gray))
            }
        }
    }
}