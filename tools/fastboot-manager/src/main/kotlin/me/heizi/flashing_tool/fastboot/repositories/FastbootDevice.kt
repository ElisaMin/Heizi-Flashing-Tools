package me.heizi.flashing_tool.fastboot.repositories

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import me.heizi.flashing_tool.fastboot.fake2
import me.heizi.flashing_tool.fastboot.fake3
import me.heizi.flashing_tool.fastboot.screen.FastbootCommandViewModel
import me.heizi.flashing_tool.fastboot.screen.fastbootCommand
import me.heizi.kotlinx.logger.debug
import me.heizi.kotlinx.shell.CommandResult
import me.heizi.kotlinx.shell.ProcessingResults
import me.heizi.kotlinx.shell.Shell
import kotlin.coroutines.EmptyCoroutineContext


object FastbootDevices {
    private val singletons = mutableListOf<DeviceAndRunner>()
    fun getSingletonBySerialOrNull(serial:String):FastbootDevice? {
        if (Fastboot.isTesting) return when (serial) {
            "LMV600TM9a483380" -> fake2
            "LMG710ULM785d0fea" -> me.heizi.flashing_tool.fastboot.fake
            else -> fake3
        }.let {
            fake(serial,it)
        }

        if (Fastboot.deviceSerials.value.contains(serial)) {
            return singletons.find { it.serialId == serial }
                ?: FastbootDeviceImpl(serial)
                    .also(singletons::add)
        }
        return null
    }
    val deviceScope = CoroutineScope(Fastboot.scope.coroutineContext+EmptyCoroutineContext+IO)


    private fun fake(serialId: String,getvar:String) = object : FastbootDeviceImpl(serialId) {
        override suspend fun getvar(): String = getvar
    }

    private interface DeviceAndRunner:FastbootDevice,DeviceRunner
    private open class FastbootDeviceImpl(
        override val serialId: String
    ):DeviceAndRunner {
        override val runner: DeviceRunner get() = this
        override val info: MutableStateFlow<FastbootDeviceInfo> = MutableStateFlow(EmptyFastbootDeviceInfo)

        private val scope by lazy { deviceScope+job }
        private val job by lazy {
            val job = Fastboot.scope.launch {
                while (serialId in Fastboot.deviceSerials.value && isActive) {
                    debug("has next")
                    val job = launch {
                        info.emit(getInfo())
                    }
                    launch {
                        delay(460)
                        if(job.isActive) job.cancel()
                    }
                    job.join()
                    if (info.value == EmptyFastbootDeviceInfo)
                        delay(3000)
                    else delay(30000)
                    debug("next refresh")
                }
            }
            job.invokeOnCompletion {
                singletons.remove(this)
            }
            job
        }

        val currentCommand: MutableState<FastbootCommandViewModel?> = mutableStateOf(null)

        override fun run(command: String) {
            scope.launch {
                currentCommand.value =
                    FastbootCommandViewModel(this@FastbootDeviceImpl,command)
            }
        }
        override fun run(viewModel: FastbootCommandViewModel) {
            scope.launch {
                Shell("fastboot -s $serialId ${viewModel.command}").collect { r-> when (r) {
                    is ProcessingResults.Error ->
                        viewModel.onMessage(r.message)
                    is ProcessingResults.Message ->
                        viewModel.onMessage(r.message)
                    is ProcessingResults.CODE -> {
                        viewModel.onResult(r.code==0)
                    }
                    else -> {}
                } }
            }

        }

        override suspend fun getvar(): String? = Shell("fastboot -s $serialId getvar all", isMixingMessage = true).await().let {
            if (it is CommandResult.Success && it.message.isNotEmpty()) it.message
            else null
        }

        var times = 0
        override suspend fun updateInfo() {
            debug("updating info",times++)
            info.emit(getInfo())
        }

        @Composable
        override fun start() {
            var command by remember { currentCommand }
            command?.let { fastbootCommand(it) {
                command = null
            } }
        }

        init { job }
    }

}
