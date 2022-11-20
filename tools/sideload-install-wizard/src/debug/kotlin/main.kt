@file:JvmName("SideloadDebug")
package debug.heizi.flashing_tool.sideloader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane


@OptIn(ExperimentalSplitPaneApi::class, ExperimentalComposeUiApi::class)
fun main() {
    singleWindowApplication {
//        var isOpen by remember { mutableStateOf(true) }
//        if (isOpen) addDeviceDialog {
//            isOpen=false
//        }
//        else Button({isOpen=true}){ Text("open") }
        HorizontalSplitPane {
            first(120.dp) {
                Box(Modifier.background(MaterialTheme.colorScheme.primary).fillMaxSize())
            }
            second {
                Box(Modifier.background(MaterialTheme.colorScheme.secondary).fillMaxSize())
            }
            splitter {
                visiblePart {
                    var handlerSize by mutableStateOf(0.0f)

                    Box(
                        Modifier
                            .width(10.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = handlerSize),)
                            .onPointerEvent(PointerEventType.Enter) {
                                handlerSize = 0.3f
                            }
                            .onPointerEvent(PointerEventType.Exit) {
                                handlerSize = 0f
                            }
                    )
                }
                handle {
                    Box(
                        Modifier
                            .markAsHandle()
                            .width(3.dp)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}