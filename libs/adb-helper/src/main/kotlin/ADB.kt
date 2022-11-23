@file:Suppress("NAME_SHADOWING")

package me.heizi.flashing_tool.adb

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.heizi.kotlinx.logger.error
import me.heizi.kotlinx.shell.CommandResult
import me.heizi.kotlinx.shell.Shell



object ADB {

    private val scope = CoroutineScope(Dispatchers.IO)

    infix fun execute(command:String)
        = Shell("adb $command", runCommandOnPrefix = true)

    object AsyncExecute {
        val commands by lazy {
            MutableStateFlow("")
        }
        val results by lazy {
            commands
                .filter { it.isNotEmpty() }
                .map { Shell(
                    "adb $it",
                    startWithCreate = true,
                    runCommandOnPrefix = true,
                ) }
        }
        init {
            scope.launch {
                results.collect {

                }
            }
        }
    }



    val devices: Flow<ADBDevice> = flow {
        Shell("adb devices").await().let {
            require(it is CommandResult.Success) {
                "its not ready to collecting devices!"
            }
            it.message
        }.lineSequence()
        .filter {
            val it = it.trim()
            it.isNotEmpty() && it.first()!='*'&& it !in arrayOf("List of devices attached","adb devices")
        }.map {
            it.split(' ','	')
        }.filter {
            it.size == 2
        }.map {
            AdbDeviceImpl(it.first(), ADBDevice.DeviceState(it.last()))
        }.forEach {
            emit(it)
        }
    }
    val savedDevices:Flow<ADBDevice> = LocalDevice.asFlow().map {
        AdbDeviceImpl(it,ADBDevice.DeviceState.host)
    }

    suspend fun wireless(host:String)
        = "connected" in execute("connect $host").await().let {
            when(it) {
                is CommandResult.Success -> it.message
                is CommandResult.Failed -> {
                    error("connect command execute failed ${it.code}",it.processingMessage,it.errorMessage)
                    ""
                }
            }
        }



    private data class AdbDeviceImpl(
        override val serial: String,
        override val state: ADBDevice.DeviceState
    ):ADBDevice {
        override var isConnected: Boolean?
            get() = TODO("it seams not working")
            set(value) {
                TODO("it seams not working")
            }

        override fun execute(vararg command: String) {
            AsyncExecute.commands.value = (command.joinToString(" ", truncated = " "))
        }

        override fun executeWithResult(vararg command: String): Shell
            = ADB.execute(command.joinToString(" ", truncated = " "))

    }

}


