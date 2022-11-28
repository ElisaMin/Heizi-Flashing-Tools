@file:JvmName("SideloadDebug")
package debug.heizi.flashing_tool.sideloader

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.CoroutineScope
import me.heizi.flashing_tool.adb.ADBDevice
import me.heizi.flashing_tool.sideloader.Context
import me.heizi.flashing_tool.sideloader.isSideload
import me.heizi.flashing_tool.sideloader.screens.AbstractHomeViewModel
import me.heizi.flashing_tool.sideloader.screens.invoke
import me.heizi.kotlinx.shell.Shell
import net.dongliu.apk.parser.bean.ApkIcon
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import java.io.File
import kotlin.coroutines.CoroutineContext

val shell = Shell("echo hello world")

class DebugDevice(
    override val serial: String = "device",
    override val state: ADBDevice.DeviceState,
) :ADBDevice {
    override var isConnected: Boolean = false
    override fun execute(vararg command: String) {
        println(command.joinToString())
    }

    override fun executeWithResult(vararg command: String, isStart: Boolean): Shell {
        println(command.joinToString())
        return shell
    }

    override fun live(coroutineContext: CoroutineContext): ADBDevice.Live {
        TODO("Not yet implemented")
    }
}

@OptIn(ExperimentalSplitPaneApi::class, ExperimentalComposeUiApi::class)
fun main() {
    singleWindowApplication {
        Context.Ready()
//        object : Context.Invoke {
//        override val smallTitle: String = "s"
//        override val message: String = "m"
//        override val isSuccess: Boolean? = null
//        override val isDone: Boolean? = true
//
//        override fun start() {
//
//        }
//
//        override val files: List<File> = emptyList()
//    }.invoke()
        val homeViewModel = remember {
            object :AbstractHomeViewModel() {
                override var devices: List<ADBDevice> = mutableStateListOf(
                    DebugDevice("android",ADBDevice.DeviceState.device),
                    DebugDevice("recovery",ADBDevice.DeviceState.recovery),
                    DebugDevice("offline",ADBDevice.DeviceState.offline),
                    DebugDevice("sideload",ADBDevice.DeviceState.sideload),
                )
                override var isWaiting: Boolean by mutableStateOf(false)
                override val packageDetails: Map<String, Array<String>>
                    = mapOf("details" to arrayOf("test","test"))
                override val icon: ApkIcon<*>? = null
                override val titleName: String = "名字"
                override val packageName: String = "包名"
                override val version: String = "版本名称"

                override fun addDevice(serial: String): Boolean {
                    return true
                }

                override fun switchMode() {
                    isSideload = !isSideload
                }

                override fun nextStep() {

                }

                override suspend fun CoroutineScope.onLaunching() {
                }

                override fun onOut() {

                }

            }

        }
//        homeViewModel()


//        var isOpen by remember { mutableStateOf(true) }
//        if (isOpen) addDeviceDialog {
//            isOpen=false
//        }
//        else Button({isOpen=true}){ Text("open") }



//        HorizontalSplitPane {
//            first(120.dp) {
//                Box(Modifier.background(MaterialTheme.colorScheme.primary).fillMaxSize())
//            }
//            second {
//                Box(Modifier.background(MaterialTheme.colorScheme.secondary).fillMaxSize())
//            }
//            splitter {
//                visiblePart {
//                    var handlerSize by mutableStateOf(0.0f)
//
//                    Box(
//                        Modifier
//                            .width(10.dp)
//                            .fillMaxHeight()
//                            .background(MaterialTheme.colorScheme.background.copy(alpha = handlerSize),)
//                            .onPointerEvent(PointerEventType.Enter) {
//                                handlerSize = 0.3f
//                            }
//                            .onPointerEvent(PointerEventType.Exit) {
//                                handlerSize = 0f
//                            }
//                    )
//                }
//                handle {
//                    Box(
//                        Modifier
//                            .markAsHandle()
//                            .width(3.dp)
//                            .fillMaxHeight()
//                    )
//                }
//            }
//        }
    }
}