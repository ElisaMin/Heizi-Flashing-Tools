package me.heizi.flashing_tool.sideloader.screens

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.heizi.flashing_tool.sideloader.isSideload

@Composable
fun Info(
    modifier: Modifier = Modifier.defaultMinSize(minWidth = 372.dp).fillMaxHeight().fillMaxWidth(0.45f),
    packageDetail:Map<String,Array<String>>,
    icon:Any?=null,
    titleName:String,
    packageName:String?=null,
    version:String?=null,
) {
    val isApk = !isSideload
    Card(modifier) {
        Row(Modifier.padding(16.dp)) {
            if (isApk) Card(Modifier.size(126.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary)) {

            }
            Column(Modifier.padding(start = 16.dp).sizeIn(maxHeight = 126.dp)) {
                val scrollableState = rememberScrollState()
                val adapter = rememberScrollbarAdapter(scrollableState)
                Box(Modifier.horizontalScroll(scrollableState)) { Text(titleName, style = MaterialTheme.typography.displayLarge, maxLines = 1) }
                HorizontalScrollbar(adapter,)
                if (packageName != null)
                    Text(packageName)
                if (version != null)
                    Text(version)
            }
        }
        Detail(packageDetail)
    }
}
@Composable
private fun Detail(
    data:Map<String,Array<String>>
) = Column(Modifier.paddingButBottom(16.dp)) {
    for ((title,detail) in data) {
        if (title == "name") continue
        Text(title, style = MaterialTheme.typography.labelMedium)
        for (s in detail) {
            Text(s)
        }
    }
}