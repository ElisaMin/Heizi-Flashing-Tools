package me.heizi.kotlinx.compose.desktop.core.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable
fun ExtendableCard(
    initExtend: Boolean = false,
    modifier: Modifier = Modifier,
//    elevation: Dp = 3.dp,
    title:@Composable ()->Unit,
    content:@Composable ()->Unit
)  {
    ExtendableCard(mutableStateOf(initExtend),modifier, title, content)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtendableCard(
    states:MutableState<Boolean> = mutableStateOf(false),
    modifier: Modifier = Modifier,
//    elevation: Dp = 3.dp,
    title:@Composable ()->Unit,
    content:@Composable ()->Unit
) {
    var state by remember { states }
    val padding = Modifier.padding(8.dp)

    ElevatedCard(modifier = padding.defaultMinSize(minWidth = 127.dp).then(modifier)
        .width(180.dp),
//        elevation = elevation
    ) {
        Column(modifier = padding.fillMaxWidth()) {
            Box(contentAlignment = Alignment.CenterStart,modifier = padding.fillMaxWidth()) {
                title()
                TextButton(onClick = {state=!state},modifier = Modifier.padding(end = 2.dp).align(Alignment.CenterEnd)) { Text(if (!state)"◀" else "▼") }
            }
            if (state) {
                content()
            } else {
                Box(Modifier.fillMaxWidth().height(0.dp).padding(2.dp).border(1.dp, Color.Gray))
            }
        }
    }
}