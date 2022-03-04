@file:JvmName("Main")
package me.heizi.flashing_tool.fastboot


import me.heizi.flashing_tool.fastboot.screen.noticeOfAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

val openedDeviceDialog = mutableListOf<String>()

@ExperimentalMaterialApi
@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    noticeOfAlpha()
    runBlocking {
        delay(10)
    }
    composeApplication()
}
