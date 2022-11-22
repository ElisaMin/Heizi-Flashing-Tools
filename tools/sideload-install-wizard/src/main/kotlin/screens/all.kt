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
import me.heizi.flashing_tool.sideloader.Resources
import me.heizi.kotlinx.compose.desktop.core.components.AboutExtendCard
import org.jetbrains.compose.splitpane.*

fun main() {
    singleWindowApplication(title = "AndroidInstallWizard", icon = Resources.iconASTUgly) {
//        apk()
//        zip()
        split()
    }
}
@Preview
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSplitPaneApi::class)
@Composable
fun split() = MaterialTheme {
    Scaffold(
        snackbarHost = {
            val state = remember {
                SnackbarHostState()
            }
            SnackbarHost(state)
            LaunchedEffect("that just a state") {
                state.showSnackbar("正在连接设备",)

            }
        },
//        topBar = {
//             TopAppBar(title = { Text("APK/ZIP installation guide") })
//        },
        floatingActionButton = {
            Row {
                ExtendedFloatingActionButton({ Text("安装") }, icon = { Icon(Icons.Default.Send,"start") },{})
            }
        }
    ) {

        val first = @Composable {

            Column {
//                Text("文件信息:", modifier = Modifier.fillMaxWidth().paddingButBottom(8.dp), style = MaterialTheme.typography.labelMedium)
                Card(Modifier.fillMaxSize().padding(8.dp)) {
                    Column(modifier = Modifier.paddingButBottom(16.dp).verticalScroll(rememberScrollState()),) {
                        BoxWithConstraints {
                            val content = @Composable {padding:PaddingValues->
                                Card(Modifier.size(126.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary)) {  }
                                Column(Modifier.padding(padding).sizeIn(maxHeight = 126.dp)) {
//                                    val scrollableState = rememberScrollState()
//                                    val adapter = rememberScrollbarAdapter(scrollableState)
//                                    Box(Modifier.horizontalScroll(scrollableState)) {
                                        Text("微信(测试版)", style = MaterialTheme.typography.displayLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
//                                    }
//                                    HorizontalScrollbar(adapter,)

                                    Text("com.tencent.wechat", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("8.0.25-test01", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            if (maxWidth > 156.dp) Row {
                                content(PaddingValues(start=8.dp))
                            } else Column {
                                content(PaddingValues())
                            }
                        }
                        FilledTonalButton({}, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                            Text("这是刷机包?")
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
        val second = @Composable { padding:PaddingValues->
            Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.spacedBy(8.dp)){
                Text("选择要安装的设备:", modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.labelMedium)
                Text("请保证您的设备已经开启ADB调试或已经进入Rec模式。", modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.labelSmall)
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Card(Modifier,colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondary)) {
                    Row(Modifier.padding(16.dp,8.dp).fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("LMG710iiiiwsrgasfg", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("已选中", maxLines = 1, overflow = TextOverflow.Visible)
                    }
                }

                Card({},Modifier,colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer)) {
                    Row(Modifier.padding(16.dp,8.dp).fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("PXL333aagfryt551", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("设备", maxLines = 1, overflow = TextOverflow.Visible)
                    }
                }

                Card({},Modifier,colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiaryContainer)) {
                    Row(Modifier.padding(16.dp,8.dp).fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("192.168.1.2:5556", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("未连接", maxLines = 1, overflow = TextOverflow.Visible)
                    }
                }

                Card({ println("clicked") }, Modifier,enabled = false) {
                    Row(Modifier.padding(16.dp,8.dp).fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ZX24hsa339", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Recovery", maxLines = 1, overflow = TextOverflow.Visible)
                    }
                }
                TextButton({}, Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add,"add device")
                    Text("手动添加设备")
                }
                AboutExtendCard()
            }
        }
//        VerticalSplitPane {
//            first {
//                first()
//            }
//            second {
//                second()
//            }
//        }
        BoxWithConstraints(Modifier.padding(it)) {
            if (maxWidth> 380.dp)
                HorizontalSplitPane(splitPaneState = rememberSplitPaneState(0.6f)) {
                    first(176.dp,) {
                        first()
                    }
                    second {
                        second(PaddingValues(8.dp))
                    }
                    defaultSplitter()
                }
            else
                VerticalSplitPane {
                    first((176+
//                            24+
                            36).dp) {
                        first()
                    }
                    second {
                        second(PaddingValues(horizontal = 8.dp))
                    }
                    defaultSplitter()
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
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary))
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