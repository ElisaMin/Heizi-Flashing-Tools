@file:JvmName("Main")
package me.heizi.flashing_tool.fastboot


import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.heizi.flashing_tool.fastboot.screen.noticeOfAlpha


@ExperimentalMaterialApi
@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    noticeOfAlpha()
    runBlocking {
        delay(10)
    }
    composeApplication()
}
