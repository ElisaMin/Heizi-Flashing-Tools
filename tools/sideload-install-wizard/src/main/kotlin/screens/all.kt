package me.heizi.flashing_tool.sideloader.screens

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication

fun main() {
    singleWindowApplication {
        preview()
    }
}

fun Modifier.paddingButBottom(all:Dp)
    = padding(all,all,all,0.dp)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun preview(
) = MaterialTheme {  Scaffold( floatingActionButton = {
    ExtendedFloatingActionButton({ Text("安装") }, icon = { Icon(Icons.Default.Send,"start") },{})
}) {

    Row(Modifier.fillMaxSize().padding(8.dp)) {
        Card(Modifier.defaultMinSize(minWidth = 372.dp).fillMaxHeight().fillMaxWidth(0.45f)) {
            Row(Modifier.padding(16.dp)) {
                Card(Modifier.size(126.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary)) {  }
                Column(Modifier.padding(start = 16.dp).sizeIn(maxHeight = 126.dp)) {
                    val scrollableState = rememberScrollState()
                    val adapter = rememberScrollbarAdapter(scrollableState)
                    Box(Modifier.horizontalScroll(scrollableState)) { Text("微信(测试版)", style = MaterialTheme.typography.displayLarge, maxLines = 1) }
                    HorizontalScrollbar(adapter,)

                    Text("com.tencent.wechat")
                    Text("8.0.25-test01")
                }
            }
            Column(Modifier.paddingButBottom(16.dp)) {
                Text("大小", style = MaterialTheme.typography.labelMedium)
                Text("1024MB")
                Spacer(Modifier.padding(8.dp))
                Text("SDK", style = MaterialTheme.typography.labelMedium)
                Text("min-14")
                Text("target-29")
            }
//            ListItem(overlineText = {
//                Text("大小")
//            }, headlineText = {
//                Text("1024MB")
//            }, colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primary))
        }
        Column(Modifier.fillMaxSize().widthIn(max = 333.dp)){
            TextButton({}, Modifier.paddingButBottom(8.dp).fillMaxWidth()) {
                Icon(Icons.Default.Add,"add device")
                Text("添加设备")
            }
            LinearProgressIndicator(Modifier.padding(horizontal = 16.dp).fillMaxWidth())

            Card(Modifier.paddingButBottom(8.dp),colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondary)) {
                Row(Modifier.padding(16.dp,8.dp).fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("LMG710iiiiwsrgasfg", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("已选中", maxLines = 1, overflow = TextOverflow.Visible)
                }
            }

            Card({},Modifier.paddingButBottom(8.dp),colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer)) {
                Row(Modifier.padding(16.dp,8.dp).fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("PXL333aagfryt551", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("设备", maxLines = 1, overflow = TextOverflow.Visible)
                }
            }

            Card({},Modifier.paddingButBottom(8.dp),colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiaryContainer)) {
                Row(Modifier.padding(16.dp,8.dp).fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("192.168.1.2:5556", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("未连接", maxLines = 1, overflow = TextOverflow.Visible)
                }
            }

            Card({ println("clicked") },Modifier.paddingButBottom(8.dp),enabled = false) {
                Row(Modifier.padding(16.dp,8.dp).fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("ZX24hsa339", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Recovery", maxLines = 1, overflow = TextOverflow.Visible)
                }
            }
        }

    }

} }