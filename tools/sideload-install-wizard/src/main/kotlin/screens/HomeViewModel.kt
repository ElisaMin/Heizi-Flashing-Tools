package me.heizi.flashing_tool.sideloader.screens

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.heizi.flashing_tool.adb.ADB
import me.heizi.flashing_tool.adb.ADBDevice
import me.heizi.flashing_tool.sideloader.*
import me.heizi.flashing_tool.sideloader.Context.Companion.deviceFilter
import me.heizi.kotlinx.logger.debug
import me.heizi.kotlinx.shell.CommandResult
import net.dongliu.apk.parser.bean.IconResource
import java.nio.charset.Charset

@Composable
operator fun SingleFileContext.invoke() {
    val viewModel:HomeViewModel = remember {
        object :StateHomeViewModel(this){}
    }
    SideEffect {
        viewModel.onUpdateEffect()
    }
    viewModel()
}
abstract class StateHomeViewModel(initContext: SingleFileContext):AbstractHomeViewModel() {
    var currentContext by mutableStateOf(initContext)

    override var isWaiting: Boolean by mutableStateOf(false)
    // loop
    override var devices: List<ADBDevice> by mutableStateOf(listOf())
        protected set
    var isAlive = true
        protected set
    val job = Context.scope.launch(Dispatchers.IO,start = CoroutineStart.LAZY) {
        isAlive = true
        val innerJob = launch {
            context.shareIn(this, started = SharingStarted.Eagerly)
                .takeWhile { it is SingleFileContext || it is Context.Ready }
                .filterIsInstance<SingleFileContext>()
                .collect {
                    isWaiting = true
                    currentContext = it
                    isSideload = it is Sideload
                    isWaiting = false
                }
            isAlive = false
            runCatching { cancel() }
        }
        while (isAlive) {
            isWaiting = true
            devices = Context.devices.toList()
            delay(1000)
            isWaiting = false
            delay(5000)
        }
        runCatching {
            cancel()
        }
        innerJob.runCatching { cancel() }
        innerJob.join()

    }

    final override var icon: IconResource? by mutableStateOf(null)
        protected set
    final override var version: String? by mutableStateOf(null)
        protected set
    final override var packageName: String? by mutableStateOf(null)
        protected set
    final override var titleName: String by mutableStateOf("")
        protected set
    final override var packageDetails: Map<String, Array<String>> by mutableStateOf(mapOf())
        protected set

    final override fun switchMode() {
         currentContext.toApkOrSideload().let {
             context.value = it
             currentContext = it
        }
        onUpdateEffect()
    }

    final override fun nextStep() {
        isAlive = false
        runBlocking {
            job.runCatching {
                cancel()
            }
            job.join()
            context.value = Context.Invoking(currentContext)
        }
    }

    final override suspend fun CoroutineScope.onStart() {
        job.start()
    }


    override fun onUpdateEffect() {
        icon = currentContext.icon?.also { it.debug("image",it.data::class.simpleName) }
        version = currentContext.version
        packageName = currentContext.packageName
        titleName = currentContext.name
        packageDetails = currentContext.details
    }


    final override fun onStop() {
        isAlive = false
        job.runCatching {
            cancel()
        }
    }


}

@Stable
interface HomeViewModel {

    val scope get() = Context.scope

    val devices:List<ADBDevice>
    @get:Composable
    val selected:List<String>
    val isWaiting:Boolean


    val packageDetails:Map<String,Array<String>>
    val icon: ApkIcon<*>?
    val titleName:String
    val packageName:String?
    val version:String?

    val snacks: SnackbarHostState

    fun onSelecting(serial: String):Boolean
    fun addDevice(host:String):Boolean
    fun onConnectRequest(contextState: InnerDeviceContextState, serial:String)

    fun switchMode()

    fun nextStep()

    suspend fun CoroutineScope.onStart()
    fun onUpdateEffect()
    fun onStop()
}


/**
 * Impl device reconnect witch part of [HomeViewModel]
 * Impl selecting [Context.selected]
 * Impl add device [Context.devices]
 * Impl Loop Devices
 */
abstract class AbstractHomeViewModel:HomeViewModel {


    // add device
    final override fun addDevice(host: String): Boolean {
        if (host.isBlank()) {
            return true
        }
        scope.launch {
            connect({ ADB.wireless(host)  },serial = host)
        }
        return true
    }

    // selected
    @get:Composable
    final override val selected: List<String>
        get() = Context.selected.deviceFilter(devices)

    final override fun onSelecting(serial: String) = when {
        serial in Context.selected -> {
            Context.selected-=serial
            false
        }
        validate(serial) -> {
            Context.selected += serial
            true
        }
        else -> false
    }

    private fun validate(serial: String,connect: Boolean = true):Boolean {
        return devices[serial]?.state?.toContext()?.let {
            val r = it is InnerDeviceContextState.Connected
            if (!r&&connect) {
                runBlocking {
                    connect(it,serial)
                }
                return validate(serial,false)
            }
            r
        }  ?: return false
    }
    // SnackBar
    abstract override var isWaiting:Boolean
        protected set
    final override val snacks = SnackbarHostState()
    private suspend fun checkTheBar() {
        snacks.currentSnackbarData?.run {
            if (visuals.message.first()=='正')
                dismiss()
            else {
                delay(300)
                checkTheBar()
            }
        }
    }
    private suspend fun cutTheBar() {
        delay(1000)
        snacks.currentSnackbarData?.run {
            if (visuals.message.first()=='连')
                dismiss()
        }
    }
    private suspend fun connect(contextState: InnerDeviceContextState, serial: String) {
        val head = "-s $serial"
        when(contextState) {
            InnerDeviceContextState.Unavailable,
            InnerDeviceContextState.Reconnect ->
                ADB execute "reconnect "
            InnerDeviceContextState.Unconnected->
                ADB execute "connect $serial"
            InnerDeviceContextState.AndroidEvenRebootNeed ->
                ADB execute "$head reboot"
            InnerDeviceContextState.SideloadRebootNeed ->
                ADB execute "$head reboot sideload"
            InnerDeviceContextState.Connected -> null
        }?.let { connect({ it.await() },serial) }
    }
    private suspend fun connect(result:suspend ()->CommandResult, serial: String) = coroutineScope {
        isWaiting = true

        launch {
            snacks.showSnackbar("正在连接$serial，请稍后。", actionLabel = "关闭")
        }

        checkTheBar()
        delay(500)
        checkTheBar()
        val r = result()
        val msg = (if (r is CommandResult.Failed)
            """|连接失败：${r.code}
                      |${r.processingMessage}
                       ${r.errorMessage?.let { "|$it" }?:""}
                      """.trimMargin()
        else
            "连接成功"+((r as CommandResult.Success).message
                .takeIf {
                    it.isNotBlank()
                }?.let { "\n"+it } ?:"")).let {
            String(it.toByteArray(Charset.forName("GBK")),Charsets.UTF_8) }

        checkTheBar()
        launch {
            cutTheBar()
        }
        snacks.showSnackbar(msg, duration = SnackbarDuration.Short)
        checkTheBar()
        isWaiting = false
        r
    }
    final override fun onConnectRequest(contextState: InnerDeviceContextState, serial: String)  {
        scope.launch { connect(contextState,serial) }
    }
}