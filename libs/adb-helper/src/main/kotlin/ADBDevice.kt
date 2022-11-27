package me.heizi.flashing_tool.adb

import kotlinx.coroutines.delay
import me.heizi.kotlinx.shell.Shell
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KProperty


fun ADBDevice.resultNeeding(vararg command: String,resultNeeding: Boolean = false,isStart: Boolean=true):Shell?
    = if (resultNeeding) executeWithResult(*command,isStart=isStart) else {
        execute(*command)
        null
    }

fun ADBDevice.reconnect()
        = execute("reconnect")
fun ADBDevice.disconnect()
        = execute("disconnect",serial)

infix fun ADBDevice.shell(command:String)
        = execute("shell",command)

infix fun ADBDevice.reboot(mode: DeviceMode)
        = execute("reboot",mode.rebootTo)

infix fun ADBDevice.sideload(zip: File)
    = execute()

fun ADBDevice.install(
    apk: File,
    isReplaceExisting:Boolean = false,
    isTestAllow:Boolean = false,
    isDebugAllow:Boolean = false,
    isGrantAllPms:Boolean = false,
    isInstant:Boolean = false,
    abi:String?=null,
    resultNeeding: Boolean = false,
) = buildList {
    add("install")
    if (isReplaceExisting) add("-r")
    if (isTestAllow) add("-t")
    if (isDebugAllow) add("-d")
    if (isGrantAllPms) add("-g")
    if (isInstant) add("--instant")
    abi?.let {abi->
        addAll(arrayOf("--abi",abi))
    }
    add(apk.absolutePath)
}.toTypedArray().let {
    resultNeeding(*it,resultNeeding = resultNeeding)
}

suspend fun ADBDevice.blockingStateChecking() {
    while (true) {
        delay(5000)
        ADBDevice.DeviceState(executeWithResult("get-state").await().successMessageOrThrow().trim())
    }
}

suspend fun ADBDevice.state()
    = ADBDevice.DeviceState(executeWithResult("get-state").await().successMessageOrThrow().trim())

/**
 * Abstract a struct as a REAL adb device in code to read info and execute command for a device 抽象一个真实存在的ADB设备成
 * 为代码解构——读取和执行指令。
 * only impl see [ADB].
 */
interface ADBDevice {

    /**
     * use for command `adb -s $seral`, it can be the host port or device id
     */

    val serial:String

    /**
     * device connection state
     * see [DeviceState]
     */

    val state:DeviceState

    /**
     * return a connecting state as boolean. false means reconnect-able
     */

    var isConnected:Boolean

    /**
     * it will be created by calling `adb -s $[serial] $[command]` for full command
     * running in Async way see [ADB.AsyncExecute.results]
     *
     * @param command see `adb help` command to list all the command
     */

    fun execute(vararg command: String)
    /**
     * returns a shell object, so you can await the result or collect when running
     * it will be created by calling `adb -s $[serial] $[command]` for full command
     *
     * @param command see `adb help` command to list all the command
     */

    fun executeWithResult(vararg command: String,isStart: Boolean = true) : Shell

    fun live(coroutineContext: CoroutineContext):Live

    /**
     * device connection state
     */
    @JvmInline
    value class DeviceState private constructor(val code: Int = -1 ) {

        companion object {
            val notfound = DeviceState()
            val host = DeviceState(0)
            val authorizing = DeviceState(1)
            val offline = DeviceState(2)
            val bootloader = DeviceState(3)
            val device = DeviceState(4)
            val recovery = DeviceState(5)
            val sideload = DeviceState(6)
            operator fun invoke(mode:String): DeviceState =
                if (mode.isEmpty()) notfound else this::class.members
                    .filterIsInstance<KProperty<DeviceState>>()
                    .find {
                        it.name == mode
                    }?.call(this)?: host

            @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
            fun DeviceState.toString():String = this::class.members
                .filterIsInstance<KProperty<DeviceState>>()
                .find {
                    it.call(this@Companion) == this
                }?.name ?:"notfound"

            fun  DeviceState.isConnected() = code in 3..6
        }

    }

    /**
     * Updatable
     *
     */
    interface Live:ADBDevice {

        suspend fun refresh()
    }
}
