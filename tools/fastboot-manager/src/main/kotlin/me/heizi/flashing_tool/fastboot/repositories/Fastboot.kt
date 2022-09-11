package me.heizi.flashing_tool.fastboot.repositories

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
import java.util.*


object Fastboot {

    var isTesting = false
        set(value) {
            if (value) isScanning =false
            devicesCache.value = arrayOf("LMG710ULM785d0fea","LMV600TM9a483380","fake3")
            field = value
        }

    val scope = CoroutineScope(Dispatchers.IO)
    var error by mutableStateOf("")
    private val devicesCache = MutableStateFlow(arrayOf<String>())
    val deviceSerials get() = devicesCache.stateIn(scope, SharingStarted.Lazily, devicesCache.value)

    var isScanning:Boolean
        get() = !isTesting && collectJob.isActive
        set(start) {
            if (isTesting) {
                collectJob.cancel()
                return
            }
            debug("fastboot collect job changed",start)
            if (start) {
                collectJob.cancel()
                collectJob = deviceScanner()
                debug("started")
            }
            else collectJob.cancel()
        }

    private val onCollectDone: LinkedList<(Array<String>) -> Unit> = LinkedList()

    private var collectJob = deviceScanner()

    private fun deviceScanner() = scope.launch  {
        while (isActive) {
            this@Fastboot.debug("starting another collect job")
            Shell("fastboot devices").also {
                delay(1000)
                if (it.isActive) it.cancel()
            }.await().let { result -> when (result) {
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
                val cache =  devicesCache.value
                this@Fastboot.println("caught device:",cache.size, cache.joinToString())
                launch {
                    onCollectDone.forEach {
                        it.invoke(cache)
                    }
                }
                this@Fastboot.debug("await")
                delay(3000)
                this@Fastboot.debug("next await")
                if (devicesCache.value.isNotEmpty()) {
                    this@Fastboot.debug("isNotEmpty")
                    delay(60000)

                }
            }

        }
    }
}