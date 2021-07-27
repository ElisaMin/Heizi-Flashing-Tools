package me.heizi.kotlinx.compose.desktop.core.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun extendableCard(
    initExtend: Boolean = false,
    title:@Composable ()->Unit,
    content:@Composable ()->Unit
) {
    var state by remember { mutableStateOf(initExtend) }
    val padding = Modifier.padding(8.dp)

    Card(modifier = padding.defaultMinSize(minWidth = 127.dp)
        .width(180.dp),elevation = 2.dp) {
        Column(modifier = padding) {

            Row {
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