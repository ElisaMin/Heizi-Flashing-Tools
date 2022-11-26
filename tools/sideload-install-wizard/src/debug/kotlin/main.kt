@file:JvmName("SideloadDebug")
package debug.heizi.flashing_tool.sideloader

import androidx.compose.foundation.layout.*
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.heizi.flashing_tool.adb.ADB
import me.heizi.flashing_tool.adb.ADBDevice
import me.heizi.flashing_tool.adb.disconnect
import me.heizi.flashing_tool.sideloader.Context
import me.heizi.flashing_tool.sideloader.InnerDeviceContextState
import me.heizi.flashing_tool.sideloader.isSideload
import me.heizi.flashing_tool.sideloader.screens.ViewModel
import me.heizi.flashing_tool.sideloader.screens.invoke
import net.dongliu.apk.parser.bean.ApkIcon
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi



@OptIn(ExperimentalSplitPaneApi::class, ExperimentalComposeUiApi::class)
fun main() {
    singleWindowApplication {
        val viewModel = remember {
            object :ViewModel {

                override var devices: List<ADBDevice> by mutableStateOf(listOf(
                    ADB.test
                ))
                override val selected: MutableSet<String> get() =  Context.selected
                override var isWaiting: Boolean by mutableStateOf(false)
                override val packageDetails: Map<String, Array<String>>
                    = mapOf("details" to arrayOf("test","test"))
                override val icon: ApkIcon<*>? = null
                override val titleName: String = "名字"
                override val packageName: String = "包名"
                override val version: String = "版本名称"
                override val snacks: SnackbarHostState
                    = SnackbarHostState()

                override fun addDevice(serial: String): Boolean {
                    return true
                }

                override fun onConnectRequest(contextState: InnerDeviceContextState) {
                    Context.scope.launch {
                        isWaiting = true
                        snacks.showSnackbar(contextState.toString(), duration = SnackbarDuration.Indefinite)
                        delay(1000)
                        isWaiting = false
                        snacks.currentSnackbarData?.dismiss()
                    }
                }

                override fun switchMode() {
                    isSideload = !isSideload
                }

                override fun nextStep() {

                }

                override suspend fun CoroutineScope.onLaunching() {
                    delay(3000)
                    devices.first().disconnect()
                    devices = listOf(devices.first())
                }

                override fun onOut() {

                }

            }

        }
        viewModel()


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