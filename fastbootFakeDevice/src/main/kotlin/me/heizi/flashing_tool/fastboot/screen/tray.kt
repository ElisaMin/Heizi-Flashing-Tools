package me.heizi.flashing_tool.fastboot.screen

import Resources
import androidx.compose.foundation.layout.Row
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import me.heizi.flashing_tool.vd.fb.deviceList
import me.heizi.flashing_tool.vd.fb.scope
import me.heizi.kotlinx.logger.debug
import me.heizi.kotlinx.logger.println
import me.heizi.kotlinx.shell.ProcessingResults
import me.heizi.kotlinx.shell.Shell
import javax.imageio.ImageIO

abstract class TraysViewModel {
    val connectedState = mutableStateOf(false)
    val loopActiveState = mutableStateOf(true)
    var error by mutableStateOf("")
    var isLoopJobActive by loopActiveState
    abstract fun exit()
    abstract fun onTrayIconSelected()
    abstract fun onStopCollecting()
    abstract fun onStartCollecting()
}

abstract class Trays(
    private val applicationScope: ApplicationScope,

): TraysViewModel() {

    private var loopJobLocal:Job?=null
    private val loopJob get() = scope.launch {
        while (isActive) {
            deviceList.clear()
//            deviceList.add("unknowdevice")
            val job = Shell("fastboot devices")
            job.collect {
                when (it) {
                    is ProcessingResults.Message -> {
                        if (it.message != "fastboot devices" && it.message.endsWith("fastboot")) {
                            it.message.replace("fastboot","")
                                .trim().let(deviceList::add)
                        }
                    }
                    is ProcessingResults.Error -> error+=it.message
                    is ProcessingResults.CODE -> if (it.code==0) "DeviceCheck".println("caught device:", deviceList)
                    is ProcessingResults.Closed -> {
                        delay(3000)
                        if (deviceList.isNotEmpty()) delay(60000)
                    }
                }
            }
            job.join()
            if (!isLoopJobActive) cancel()
        }
    }


    @Composable private fun enableJob() {
        val loopActive by remember { loopActiveState }
        debug("job active",loopActive)
        loopJobLocal = if (loopActive) {
            debug("job",loopJobLocal?.isActive)
            loopJob
        } else {
            loopJobLocal?.cancel()
            null
        }
    }
    @Composable
    fun Render() {
        error()
        enableJob()
        Trays(applicationScope)
    }
    @OptIn(ExperimentalMaterialApi::class)
    @Composable fun error() {
//        var error by remember { this.error as MutableState<String>  }
        if (error.isNotEmpty()) AlertDialog(::exit,{
            Row {
                TextButton({error = ""}) {
                    Text("忽略")
                }
                Button(::exit) {
                    Text("关闭")
                }
            }
        }, title = {Text("错误!")}, text = { Text(error) })
    }

    final override fun onStartCollecting() {
        isLoopJobActive = true
    }

    final override fun onStopCollecting() {
        isLoopJobActive = false
    }
}

@Composable
private fun TraysViewModel.Trays(applicationScope: ApplicationScope) {
    val isConnected by remember { connectedState }
    val iconTray = ImageIO.read( if (isConnected) Resources.Urls.connected else Resources.Urls.disconnect).toPainter()
    val tooltip = if (isConnected) "设备已连接" else "Fastboot未连接设备"
    applicationScope.Tray(
        icon = iconTray,tooltip = tooltip, onAction = ::onTrayIconSelected
    ) {

        Item("Start Collection", enabled = !isLoopJobActive) {
            onStartCollecting()
        }
        Item("Stop Collecting", enabled = isLoopJobActive) {
            onStopCollecting()
        }
        Item("exit"){ exit() }
    }
}