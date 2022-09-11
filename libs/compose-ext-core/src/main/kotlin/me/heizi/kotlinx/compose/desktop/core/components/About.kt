package me.heizi.kotlinx.compose.desktop.core.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.Desktop
import java.net.URI

fun openLink (url:String):Boolean = (
        Desktop.isDesktopSupported() &&
                Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
).also {
        if (it) Desktop.getDesktop().browse(URI(url))
}

@Composable
fun DeviceCantFoundBtn() {
    var isDisplay by remember {
        mutableStateOf(false)
    }
    if (isDisplay) SelectionContainer(Modifier.fillMaxWidth().padding(4.dp)) {
        Text("https://bbs.lge.fun/thread-12824.htm")
    } else TextButton({
            isDisplay =  !openLink("https://bbs.lge.fun/thread-12824.htm") },Modifier.fillMaxWidth().padding(4.dp)) {
            Text("等了许久也找不到设备？")
    }

}

//@Preview
//@Composable
//private fun preview() {
//    AboutExtendCard()
//}
@Composable
fun AboutExtendCard(init: Boolean = false) {
    val isExtend = remember {
        mutableStateOf(init)
    }
    var failedUrl by remember {
        mutableStateOf<String?>(null)
    }
    fun launchUrlOrDisplay (url:String) {
        if (!openLink(url)) failedUrl = url
    }
    ExtendableCard(isExtend, modifier = Modifier.fillMaxWidth()
        , title = { Text("关于软件/捐赠/Bug") }
    ) {
        Column(Modifier.fillMaxWidth().padding(4.dp),) {
            if (failedUrl!=null)  SelectionContainer(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("启动失败! 请用浏览器访问 $failedUrl  ",)
            }
            Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Button({
                    launchUrlOrDisplay("https://dl.lge.fun/HeiziFlashTools/")
                }) {
                    Text("官网", fontSize = 32.sp, modifier = Modifier.padding(8.dp))
                }
                Button({
                    launchUrlOrDisplay("https://jq.qq.com/?_wv=1027&k=DXal6Iuc")
                }) {
                    Text("Q群", fontSize = 32.sp, modifier = Modifier.padding(8.dp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                    Text("刷机亡灵", fontSize = 30.sp)
                    Text("HeiziFlashTools", fontSize = 12.sp)
                }


            }
        }
        Spacer(Modifier.padding(4.dp))
        Row(horizontalArrangement =  Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Heizi", fontWeight = FontWeight.Bold, fontSize = 58.sp, modifier = Modifier.padding(horizontal = 8.dp))
            Column(Modifier.wrapContentSize()) {
                Text("         给这个傻逼买点草莓吃", fontWeight = FontWeight.Bold,)
                Button({
                    launchUrlOrDisplay("https://afdian.net/@heizi")
                },Modifier.fillMaxWidth()) {
                    Text("捐赠黑字")
                }
            }
        }
    }
}
