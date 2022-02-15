package me.heizi.flashing_tool.vd.fb.fastboot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.heizi.flashing_tool.vd.fb.scope
import me.heizi.kotlinx.logger.println
import me.heizi.kotlinx.shell.CommandResult.Companion.waitForResult
import me.heizi.kotlinx.shell.ProcessingResults
import me.heizi.kotlinx.shell.Shell


private val debugScope = CoroutineScope(Dispatchers.IO)

class FastbootCommandViewModel(
    val command: String,val serialID: String,var onDone:()->Unit = {}
) {
    var isRunning:Boolean? by mutableStateOf(false)
    var log by mutableStateOf("正在执行中~!\n")


    @Deprecated("you know its debug")
    private fun executeDebug() = debugScope.launch {

        Shell("fastboots -s $serialID $command").waitForResult(
            onMessage = {
                println("message on shell waiting result",it)
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
    operator fun invoke() =scope.launch {
        isRunning = true
        Shell("fastboot -s $serialID $command", runCommandOnPrefix = true).collect { r -> when(r) {
            is ProcessingResults.CODE -> log += "\n\n指令执行似乎" + if (r.code!=0) "失败了" else "成功了"
            is ProcessingResults.Closed -> {
                isRunning = null
                onDone()
            }
            is ProcessingResults.Error -> log+="${r.message}\n"
            is ProcessingResults.Message -> r.message.let {
                println("message on shell waiting result",it)
                log+="$it\n"
            }
        } }


    }
}
