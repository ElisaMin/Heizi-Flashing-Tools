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
    val scope = CoroutineScope(Dispatchers.IO)
    var error by mutableStateOf("")
    private val devicesCache = MutableStateFlow(arrayOf<String>())
    val deviceSerials get() = devicesCache.stateIn(scope, SharingStarted.Lazily, devicesCache.value)

    var isScanning:Boolean
        get() = collectJob.isActive
        set(start) {
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
    /**
     * fastboot runner
     *  get device runner
     *    run command |
     *  run command
     *    shell
     *      produce the state
     *      :flow
     */
//    private object Runner {
//
//        val flow = MutableSharedFlow<Statues?>()
//
//
//        sealed class Statues {
//            class Start(val command: String) :Statues() {
//                val id = System.currentTimeMillis()
//            }
//            class Running(val id:Long, val flow: Flow<ProcessingResults>) :Statues()
//            class Done (val id:Long,val result: CommandResult) :Statues()
//        }
//        init {
//            scope.launch {
//                flow.shareIn(this, SharingStarted.Eagerly).collect { when(it) {
//                    is Statues.Start -> {
//                        val shell = Shell(it.command)
//                        flow.emit(Statues.Running(it.id,shell))
//                        launch {
//                            flow.emit(Statues.Done(it.id,shell.await()))
//                        }
//                    }
//                    else -> {}
//                } }
//            }
//
//        }
//        @OptIn(ExperimentalMaterialApi::class)
//        @Composable
//        fun runnerDialog() {
//
//            AlertDialog({},title = {
//                Text("设备:${viewModel.serialID}")
//            },text = {
//                Column {
//                    Text(text = "正在请求执行:\n${viewModel.command}",modifier = Modifier.padding(vertical = 6.dp).alpha(
//                        ContentAlpha.medium))
//                    if (viewModel.isRunning==true) LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical = 6.dp))
//                    if (viewModel.isRunning!=false) Text(viewModel.log,modifier = Modifier.padding(vertical = 6.dp).alpha(
//                        ContentAlpha.medium))
//                }
//            },confirmButton = {
//                if (viewModel.isRunning==false) OutlinedButton(onClick = { viewModel() }){ Text("下一步") }
//            },dismissButton = {
//                if (viewModel.isRunning!=true) TextButton(onDismiss) { Text("关闭") }
//            },
//                modifier = Modifier.defaultMinSize(300.dp,200.dp)
//            )
//        }
//    }
}