package me.heizi.flashing_tool.sideloader.contexts

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.heizi.flashing_tool.adb.ADB
import me.heizi.flashing_tool.adb.ADBDevice
import me.heizi.flashing_tool.adb.install
import me.heizi.flashing_tool.sideloader.*
import me.heizi.kotlinx.logger.debug
import me.heizi.kotlinx.shell.ProcessingResults
import me.heizi.kotlinx.shell.Shell
import java.io.File
import kotlin.math.roundToLong

// runtime error
//sealed
interface Context {

    val files:List<File>

    object Ready: Context {
        override val files: List<File>
            get() = emptyList()
    }

    interface Invoke: Context {
        val smallTitle:String
        val message:String
        val isSuccess:Boolean?
        val isDone:Boolean?
        fun start()
    }

    interface Done: Invoke

     @OptIn(FlowPreview::class)
     class Invoking constructor(
         private val parent: Context
     ) : Context by parent, Invoke {
         override var smallTitle by mutableStateOf("正在预热中...")
             private set
         override var message by mutableStateOf("")
             private set
         override var isSuccess:Boolean? by mutableStateOf(null)
             private set
         override var isDone:Boolean? by mutableStateOf(null)
             private set
         private var current = 1
         private val devices = Companion.devices.filter {
             it.serial in selected && it.state.toContext() is InnerDeviceContextState.Connected
         }

         override fun start() {
             scope.launch {
                 delay(300)
                 isDone = false
                 delay(300)
                 updateSubTitle("开始执行。\n")
                 start(parent)
             }
         }
         private suspend inline fun start(crossinline collector: (File,ADBDevice,String)->Shell) = devices.flatMapConcat {device->
             files.map { file ->
                 "file:${file.name}-to-device:${device.serial}" to collector(file,device,"\"${file.absolutePath}\"")
             }.asFlow()
         }.start()
         private suspend fun start(context: Context) {
             val isApk = context.isApk
                 ?:error("unexpected context:$context, is not apk or sideload")
             if (!isApk) start { _, device,path ->
                 device.executeWithResult("sideload",path,isStart = false)
             } else     start { file,device,path->
                 require(file is Install.Info) {
                     "unexpected context:$file is not info"
                 }
                 with(file as Install.Info){
                     device.install(
                         apk = path,
                         resultNeeding = true,
                         isStart = false,
                         isReplaceExisting=isReplaceExisting,
                         isDebugAllow = isDebugAllow,
                         isGrantAllPms = isGrantAllPms,
                         isInstant = isInstant,
                         isTestAllow = isTestAllow,
                         abi = abi,
                     )!!
                 }
             }
         }
         fun updateSubTitle(msg:String) = msg.let {
             smallTitle=it+"\n"
             message+=it
         }

         private suspend fun Flow<Pair<String, Shell>>.start() =
             map { (info, shell) ->
                 updateSubTitle("${parent::class.simpleName?.lowercase()}#$current-$info")
                 return@map shell
             }.map { shell->
                 shell.start()
                 var code = -1
                 shell.takeWhile {
                    if (it is ProcessingResults.CODE) code = it.code.also { this@Invoking.debug("collect","return code $it")}
                     it !is ProcessingResults.Closed || it !is ProcessingResults.CODE
                 }.collect {r->
                     message+="\n"
                     when(r) {
                         is ProcessingResults.Error ->
                             message += "* ${r.message}"
                         is ProcessingResults.Message ->
                             message += "  ${r.message}"
                         else -> Unit
                     }
                 }
                 message+="\n"
                 this@Invoking.debug("shell","#${shell.id} is done")
                 code == 0
             }.map { it.also { isSuccess = it } }.toList().let { list ->
                buildString {
                    append("所有任务完成")
                    if (list.size>1)
                        append("，共有${list.size }个任务")
                    append(list.count { !it }
                        .takeIf { it!=0 && list.size!=it }
                        ?.let { "，失败${it}个" }
                        ?: ("，全部" + if (false in list) "失败" else "成功")
                    )
                    append("。")
                }
            }.let {
                 context.value = object : Invoke by this@Invoking, Done {}
                 isDone = true
                 updateSubTitle(it)
            }

    }
    companion object {

        val scope = CoroutineScope(CoroutineName("InvokeScope")+Dispatchers.IO)

        val devices = flow {
            ADB.devices.toList().forEach {
                emit(it)
            }
            ADB.savedDevices.collect(::emit)
        }
        val selected: MutableList<String> = mutableStateListOf()
        fun List<String>.deviceFilter(devices:List<ADBDevice>) =
            mapNotNull { serial ->
                serial.takeIf {
                    devices[serial]?.isContextConnected == true
                }
            }

        suspend operator fun invoke(file: File) =
            Install(file).takeIf { it.isApk }
                ?: Sideload(file)

    }
}
val Context.isApk get() = when (this) {
    is Sideload -> false
    is Install -> true
    else -> null
}

/**
 * return the size with suffix Byte\KB\MB\GB\TB
 *
 * @throws NoSuchFileException 负数会炸
 */
val File.size:String get() {
    var origin = length()
    var tmp:Float
    for (s in arrayOf("Byte","KB","MB","GB","TB")) {
        if (origin == 0L)
            return "0${s}"
        tmp = origin/1024F
        if (tmp<1)
            return "$origin$s"
        origin=tmp.roundToLong()
    }
    throw NoSuchFileException(this, reason = "file size sense not allow")
}
val File.fileName:String get() = absolutePath.split('/','\\').last()
