package me.heizi.flashing_tool.fastboot.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.heizi.flashing_tool.fastboot.Resources
import javax.imageio.ImageIO


fun noticeOfAlpha() {
    CoroutineScope(Dispatchers.Default).launch {
        singleWindowApplication(
            title = "提示", icon = withContext(Dispatchers.IO) {
                ImageIO.read(Resources.Urls.fastboot!!)
            }.toPainter(), state = WindowState(size = DpSize(400.dp,200.dp)),
            exitProcessOnExit = false
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("欢迎使用预览版HFT-Fastboot设备管理器",  modifier = Modifier.padding(bottom = 8.dp), style = MaterialTheme.typography.titleMedium)
                Text("软件已经启动，在状态栏内可以找到软件的图标，该软件会在后台三秒一次轮询检测Fastboot设备，在使用完成请及时退出（关闭本窗口不可关闭程序）" +
                        "。在您的Fastboot设备电脑插入后，可以双击图标启动Fastboot设备管理器。")
            }
        }
    }
}