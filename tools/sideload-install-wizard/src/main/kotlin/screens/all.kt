package me.heizi.flashing_tool.sideloader.screens

import androidx.compose.animation.core.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState

fun main() {
    singleWindowApplication {
//        apk()
//        zip()
        split()
    }
}
@Preview
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSplitPaneApi::class)
@Composable
fun split() = MaterialTheme {
    Scaffold {
        HorizontalSplitPane(Modifier.padding(it), rememberSplitPaneState(0.6f)) {
            first(178.dp,) {
                Column() {
                    Card(Modifier.fillMaxSize().padding( 8.dp)) {
                        Column(modifier = Modifier.paddingButBottom(16.dp),) {
                            BoxWithConstraints {
                                val content = @Composable {padding:PaddingValues->
                                    Card(Modifier.size(126.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary)) {  }
                                    Column(Modifier.padding(padding).sizeIn(maxHeight = 126.dp)) {
                                        val scrollableState = rememberScrollState()
                                        val adapter = rememberScrollbarAdapter(scrollableState)
                                        Box(Modifier.horizontalScroll(scrollableState)) { Text("微信(测试版)", style = MaterialTheme.typography.displayLarge, maxLines = 1) }
                                        HorizontalScrollbar(adapter,)

                                        Text("com.tencent.wechat")
                                        Text("8.0.25-test01")
                                    }
                                }
                                if (maxWidth > 178.dp)
                                    Row() {
                                        content(PaddingValues(start=16.dp))
                                    }
                                else
                                    Column() {
                                        content(PaddingValues())
                                    }
                            }
                            Spacer(Modifier.padding(8.dp))
                        Text("大小", style = MaterialTheme.typography.labelMedium)
                        Text("1024MB")

                        Text("SDK", style = MaterialTheme.typography.labelMedium)
                        Text("min-14")
                        Text("target-29")
                        }
                    }
                }
            }
            second(100.dp) {
                Column(Modifier.fillMaxSize()){
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
            splitter {
                visiblePart {

//                    val alpha by remember {
//                        flow {
//                            var corrent = 0f
//                            while (true) {
//                                delay(1000)
//                                emit(0.3f)
//                                while (corrent<=0.3f) {
//                                    delay(41)
//                                    corrent+=0.02f
//                                    emit(corrent)
//                                }
//                                delay(1000)
//                                while (corrent >= 0.01f) {
//                                    delay(50)
//                                    corrent-=0.02f
//                                    emit(corrent)
//                                }
//                                corrent = 0f
//
//                            }
//                        }.collectAsState(0f)
//                    }
//                    var alpha by remember { mutableStateOf(0f) }
//                    val animated by rememberInfiniteTransition().
//                    animateFloat(
//                        0.0f,
//                        0.3f,
//                        infiniteRepeatable(keyframes {
//                            durationMillis = 10000
//                            0.0f at 0
//                            0.01f at 3000
//                            0.3f at 10000
//                        }, repeatMode = RepeatMode.Reverse)
//                    )
//                    Box(Modifier.padding(vertical = 16.dp).background(SolidColor(Color.LightGray), alpha = animated).fillMaxHeight().width(4.dp))
                }
                handle {
                    Box(Modifier.markAsHandle().fillMaxHeight().width(4.dp).padding(vertical = 8.dp).clickable {
                    })
                }
            }
        }

    }
}

fun Modifier.paddingButBottom(all:Dp)
    = padding(all,all,all,0.dp)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun apk(
) = MaterialTheme {  Scaffold( floatingActionButton = {
    Row {
        ExtendedFloatingActionButton({ Text("这是个刷机包") }, icon = { Icon(Icons.Default.Refresh,"start") },{})
        Spacer(Modifier.padding(8.dp))
        ExtendedFloatingActionButton({ Text("安装") }, icon = { Icon(Icons.Default.Send,"start") },{})
    }
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun zip(
) = MaterialTheme {  Scaffold( floatingActionButton = {
    Row {
        ExtendedFloatingActionButton({ Text("这是个APK") }, icon = { Icon(Icons.Default.Refresh,"start") },{})
        Spacer(Modifier.padding(8.dp))
        ExtendedFloatingActionButton({ Text("安装") }, icon = { Icon(Icons.Default.Send,"start") },{})
    }
}) {

    Row(Modifier.fillMaxSize().padding(8.dp)) {
        Card(Modifier.defaultMinSize(minWidth = 372.dp).fillMaxHeight().fillMaxWidth(0.45f)) {
            Row(Modifier.padding(16.dp)) {
                Column(Modifier.padding(start = 16.dp).sizeIn(maxHeight = 126.dp)) {
                    val scrollableState = rememberScrollState()
                    val adapter = rememberScrollbarAdapter(scrollableState)
                    Box(Modifier.horizontalScroll(scrollableState)) { Text("Magisk2233.zip", style = MaterialTheme.typography.displayLarge, maxLines = 1) }
                    HorizontalScrollbar(adapter,)

                }
            }
            Column(Modifier.paddingButBottom(16.dp)) {
                Text("大小", style = MaterialTheme.typography.labelMedium)
                Text("7MB")
                Spacer(Modifier.padding(8.dp))
                Text("路径", style = MaterialTheme.typography.labelMedium)
                Text("c:/user/heizi/download/Magisk2233.zip")
                Spacer(Modifier.padding(8.dp))
            }
        }
        Column(Modifier.fillMaxSize().widthIn(max = 333.dp)){
            TextButton({}, Modifier.paddingButBottom(8.dp).fillMaxWidth()) {
                Icon(Icons.Default.Add,"add device")
                Text("添加设备")
            }
            LinearProgressIndicator(Modifier.padding(horizontal = 16.dp).fillMaxWidth())

            Card(Modifier.paddingButBottom(8.dp),colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiaryContainer)) {
                Row(Modifier.padding(16.dp,8.dp).fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("LMG710iiiiwsrgasfg", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("安卓设备 | 点击重启至REC", maxLines = 1, overflow = TextOverflow.Visible)
                }
            }

            Card({},Modifier.paddingButBottom(8.dp),colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiaryContainer)) {
                Row(Modifier.padding(16.dp,8.dp).fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("PXL333aagfryt551", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("安卓设备 | 点击重启至REC", maxLines = 1, overflow = TextOverflow.Visible)
                }
            }

            Card({},Modifier.paddingButBottom(8.dp),colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiaryContainer,), enabled = false) {
                Row(Modifier.padding(16.dp,8.dp).fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("192.168.1.2:5556", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("不可用", maxLines = 1, overflow = TextOverflow.Visible)
                }
            }

            Card({ println("clicked") },Modifier.paddingButBottom(8.dp)) {
                Row(Modifier.padding(16.dp,8.dp).fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("ZX24hsa339", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Recovery", maxLines = 1, overflow = TextOverflow.Visible)
                }
            }
        }

    }

} }