@file:JvmName("Main")
package me.heizi.flashing_tool.fastboot


import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.heizi.flashing_tool.fastboot.screen.noticeOfAlpha



fun run() {

    noticeOfAlpha()
    runBlocking {
        delay(10)
    }
    composeApplication()
}
fun main() {
    run()
}