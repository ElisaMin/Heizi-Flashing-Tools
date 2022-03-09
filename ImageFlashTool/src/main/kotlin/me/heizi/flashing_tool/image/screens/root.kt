package me.heizi.flashing_tool.image.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.heizi.flashing_tool.image.RootComponent
import me.heizi.flashing_tool.image.style

@Composable
inline fun RootComponent.RootVM.Container(crossinline content:@Composable ()->Unit) {
    MaterialTheme {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, modifier = style.padding.bottom)
            Text(text = subtitle, modifier = style.padding.bottom)
            content()
        }
    }
}