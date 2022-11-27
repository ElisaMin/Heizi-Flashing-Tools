package me.heizi.flashing_tool.sideloader.screens

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.*
import me.heizi.flashing_tool.adb.ADB
import me.heizi.flashing_tool.adb.ADBDevice
import me.heizi.flashing_tool.sideloader.*
import me.heizi.flashing_tool.sideloader.Context.Companion.deviceFilter
import me.heizi.kotlinx.shell.CommandResult
import net.dongliu.apk.parser.bean.ApkIcon

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
    fun addDevice(serial:String):Boolean
    fun onConnectRequest(contextState: InnerDeviceContextState, serial:String)

    fun switchMode()

    fun nextStep()

    suspend fun CoroutineScope.onLaunching()
    fun onOut()


}

/**
 * Impl device reconnect witch part of [HomeViewModel]
 * Impl selecting [Context.selected]
 */
abstract class AbstractHomeViewModel:HomeViewModel {


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
    private suspend fun connect(contextState: InnerDeviceContextState, serial: String) = coroutineScope {
        val head = "-s $serial"
        when(contextState) {
            InnerDeviceContextState.AndroidEvenRebootNeed ->
                ADB execute "$head reboot"
            InnerDeviceContextState.Unavailable ,
            InnerDeviceContextState.Reconnect ->
                ADB execute "reconnect "
            InnerDeviceContextState.SideloadRebootNeed ->
                ADB execute "$head reboot sideload"
            InnerDeviceContextState.Connected -> null
        }?.let {s->
            isWaiting = true

            launch {
                snacks.showSnackbar("正在连接$serial，请稍后。", actionLabel = "关闭")
            }

            checkTheBar()
            delay(300)
            checkTheBar()
            val msg = s.await().let {r->
                if (r is CommandResult.Failed) {
                    """|连接失败：${r.code}
                       |${r.processingMessage}
                       ${r.errorMessage?.let { "|$it" }?:""}
                    """.trimMargin()
                } else "连接成功"
            }
            checkTheBar()
            launch {
                cutTheBar()
            }
            snacks.showSnackbar(msg, duration = SnackbarDuration.Short)
            checkTheBar()
            isWaiting = false

        }
    }
    final override fun onConnectRequest(contextState: InnerDeviceContextState, serial: String)  {
        scope.launch { connect(contextState,serial) }
    }
}