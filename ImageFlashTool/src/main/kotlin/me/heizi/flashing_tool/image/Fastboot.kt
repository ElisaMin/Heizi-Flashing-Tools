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
    val Context.command get() = when(this) {

        is Context.Flash -> devices.map {
            "fastboot -s $it ${if(disableAVB) "--disable-verification --disable-verity" else ""} flash "
        }.flatMap { command -> partitions.map { "$command $it $path" } }

        is Context.Boot -> devices.map {
            "fastboot -s $it boot $path"
        }

        else -> error("错误很怪\$command:$this")

    }

    fun withContext(context: Context):Shell {
        if (context.path.isEmpty() || !File(context.path).exists()
            || context.devices.isEmpty() || context.devices.contains("")
        ) error("文件或设备不存在")
        if (context is Context.Flash)
            if (context.partitions.isEmpty() || context.partitions.contains(""))
                error("分区是空的")
        return Shell(*context.command.toTypedArray(), startWithCreate = false)
    }
}