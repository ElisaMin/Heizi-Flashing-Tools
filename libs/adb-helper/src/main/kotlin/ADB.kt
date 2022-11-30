package me.heizi.flashing_tool.adb

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.heizi.flashing_tool.adb.ADBDevice.DeviceState.Companion.isConnected
import me.heizi.kotlinx.logger.debug
import me.heizi.kotlinx.logger.error
import me.heizi.kotlinx.shell.CommandResult
import me.heizi.kotlinx.shell.ProcessingResults
import me.heizi.kotlinx.shell.Shell
import kotlin.coroutines.CoroutineContext


object ADB {

    private val scope = CoroutineScope(Dispatchers.IO)

    @OptIn(FlowPreview::class)
    val devices: Flow<ADBDevice> get() = execute("devices").takeWhile {
        if (it is ProcessingResults.CODE) require(it.code == 0) {
            "its not ready to collecting devices!"
        }
        it !is ProcessingResults.Closed || it !is ProcessingResults.CODE
    }.filterIsInstance<ProcessingResults.Message>().flatMapConcat { message ->
        message.message.split("\n").asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() && it.isNotEmpty() }
            .filter {
                it.first()!='*'&& it !in arrayOf("List of devices attached","adb devices")
            }.map { it.split(' ','	') }.filter {
                it.size == 2
            }
            .asFlow()
    }.map {
        AdbDeviceImpl(it.first(), ADBDevice.DeviceState(it.last()))
    }
    val savedDevices:Flow<ADBDevice> = LocalDevice.asFlow().map {
        AdbDeviceImpl(it,ADBDevice.DeviceState.host)
    }

    fun execute(command:String,isStart:Boolean =true,coroutineContext: CoroutineContext= scope.coroutineContext): Shell {
        debug("executor","invoking $command")
        return Shell(
            coroutineContext = coroutineContext,
            startWithCreate = isStart,
            prefix = arrayOf("cmd","/c","adb $command"),
            onRun = {}
        )
    }

    infix fun execute(command:String)
            = execute(command,true)

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

    private fun CommandResult.wirelessSuccess() = "connected" in when(this) {
        is CommandResult.Success -> message
        is CommandResult.Failed -> {
            error("connect command execute failed $code",processingMessage,errorMessage)
            ""
        }
    }

    suspend fun wireless(host:String) = execute("connect $host").let{
        it.await().also { result -> if (result.wirelessSuccess()) LocalDevice+= host }
    }



    private class AdbDeviceImpl(
        override val serial: String,
        state: ADBDevice.DeviceState
    ):ADBDevice {
        override var state: ADBDevice.DeviceState = state
            private set

        override var isConnected: Boolean get()  = state.isConnected()
            set(connecting) {
                if (connecting) reconnect() else disconnect()
                scope.launch {
                    runCatching {
                        state()
                    }.getOrNull()?.let {
                        state = it
                    }
                }
            }

        override fun execute(vararg command: String) {
            AsyncExecute.commands.value = (command.afterThisDevice().joinToString(" ", truncated = " "))
        }

        override fun executeWithResult(vararg command: String,isStart: Boolean): Shell
            = execute(command.afterThisDevice().joinToString(" ", truncated = " "),isStart)

        override fun live(coroutineContext: CoroutineContext): ADBDevice.Live {
            TODO("实现更新state")
        }

        private fun Array<out String>.afterThisDevice():Array<out String>
                = arrayOf("-s",serial,*this)
    }
}


