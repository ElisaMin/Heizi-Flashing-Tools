package me.heizi.flashing_tool.image

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import me.heizi.kotlinx.logger.debug
import me.heizi.kotlinx.logger.println
import me.heizi.kotlinx.shell.CommandResult
import me.heizi.kotlinx.shell.Shell
import java.io.File


object Fastboot {
    val scope = CoroutineScope(Dispatchers.IO)
    var error by mutableStateOf("")

    private val devicesCache = MutableStateFlow(arrayOf<String>())

    val deviceSerials get() = devicesCache.stateIn(scope, SharingStarted.Lazily, devicesCache.value)
    val scannerJob get() = deviceScanner()

    private fun deviceScanner() = scope.launch {
        while (isActive) {
            this@Fastboot.debug("starting another collect job")
            Shell("fastboot devices").also {
                delay(1000)
                if (it.isActive) it.cancel()
            }.await().let { result ->
                when (result) {
                    is CommandResult.Failed -> {
                        error = result.toString()
                    }
                    is CommandResult.Success -> {

                        result.message.lineSequence()
                            .filter { it != "fastboot devices" && it.endsWith("fastboot") }
                            .map {
                                it.replace("fastboot", "").trim()
                            }.toList()
                            .toTypedArray()
                            .let {
                                devicesCache.emit(it)
                            }
                    }
                }
                val cache = devicesCache.value
                this@Fastboot.println("caught device:", cache.size, cache.joinToString())
                delay(2000)
            }
        }
    }
    fun withContext(context: Context):Shell {
        if (context.path.isEmpty() || !File(context.path).exists()
            || context.devices.isEmpty() || context.devices.contains("")
        ) error("文件或设备是空的")
        return when (context) {
            is Context.Boot -> {
                val commands = context.devices.map {
                    "fastboot -s $it boot ${context.path}"
                }.toTypedArray()
                Shell(*commands, startWithCreate = false)
            }
            is Context.Flash -> {
                if (context.partitions.isEmpty() || context.partitions.contains(""))
                    error("分区是空的")
                val commands = context.devices.map {
                    "fastboot -s $it ${if(context.disableAVB) "--disable-verification --disable-verity" else ""} flash "
                }.flatMap { command ->
                    context.partitions.map { "$command $it ${context.path}" }
                }.toTypedArray()
                Shell(*commands, startWithCreate = false)
            }
            else -> error("错误很怪\$withContext:$context")
        }
    }
}