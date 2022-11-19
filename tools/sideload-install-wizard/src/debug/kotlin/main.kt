@file:JvmName("SideloadDebug")
package debug.heizi.flashing_tool.sideloader

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.singleWindowApplication
import me.heizi.flashing_tool.sideloader.screens.addDeviceDialog


fun main() {
    singleWindowApplication {
        var isOpen by remember { mutableStateOf(true) }
        if (isOpen) addDeviceDialog {
            isOpen=false
        }
        else Button({isOpen=true}){ Text("open") }
    }
}