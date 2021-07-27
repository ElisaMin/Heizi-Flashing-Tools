package me.heizi.flashing_tool.vd.fb.fastboot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import me.heizi.flashing_tool.vd.fb.scope
import me.heizi.kotlinx.shell.CommandResult.Companion.waitForResult
import me.heizi.kotlinx.shell.shell
import java.nio.charset.Charset

class FastbootCommandViewModel(
    val command: String,val serialID: String,var onDone:()->Unit = {}
) {
    var isRunning:Boolean? by mutableStateOf(false)
    var log by mutableStateOf("正在执行中~!\n")

    operator fun invoke() {
        scope.launch {
            isRunning = true

            val command = String(command.toByteArray(Charsets.UTF_8), Charset.forName("GBK"))
            shell("fastboot -s $serialID $command").waitForResult(
                onMessage = {
                    log+="$it\n"
                },onError = {
                    log+="$it\n"
                },onResult = {
                    isRunning = null
                    log += "\n\n"+when (it) {
                        is me.heizi.kotlinx.shell.CommandResult.Failed -> {
                            "指令执行似乎失败了"
                        }
                        is me.heizi.kotlinx.shell.CommandResult.Success ->{
                            "指令似乎执行成功了"
                        }
                    }
                    onDone()
                }
            )
        }
    }
}
