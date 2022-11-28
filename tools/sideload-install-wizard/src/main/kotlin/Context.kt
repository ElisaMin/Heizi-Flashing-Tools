package me.heizi.flashing_tool.sideloader

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.heizi.flashing_tool.adb.ADB
import me.heizi.flashing_tool.adb.ADBDevice
import me.heizi.flashing_tool.adb.install
import me.heizi.kotlinx.shell.ProcessingResults
import me.heizi.kotlinx.shell.Shell
import java.io.File
import java.time.Instant
import kotlin.math.roundToLong


sealed interface Context {

    val files:List<File>

    object Ready:Context {
        override val files: List<File>
            get() = emptyList()
    }

    interface Invoke:Context {
        val smallTitle:String
        val message:String
        val isSuccess:Boolean?
        val isDone:Boolean?
        fun start()
    }

     @OptIn(FlowPreview::class)
     class Invoking constructor(
         private val parent: Context
     ) :Context by parent,Invoke {
         override var smallTitle by mutableStateOf("正在预热中...")
             private set
         override var message by mutableStateOf("")
             private set
         override var isSuccess:Boolean? by mutableStateOf(null)
             private set
         override var isDone:Boolean? by mutableStateOf(null)
             private set
         private var current = 1
         private val devices = Context.devices.filter {
             it.serial in selected && it.state.toContext() is InnerDeviceContextState.Connected
         }

         override fun start() {
             scope.launch {
//                 context.value = Ready
                 delay(500)
                 updateSubTitle("开始执行。")
                 start(parent)
                 isDone = false
             }
         }
         private suspend fun start(context: Context) {
             val isApk = context.isApk
                 ?:error("unexpected context:$context, is not apk or sideload")
             if (isApk) start { file,device->
                 require(file is Install.Info) {
                     "unexpected context:$file is not info"
                 }
                 with(file as Install.Info){
                     device.install(
                         apk = file,
                         resultNeeding = true,
                         isReplaceExisting=isReplaceExisting,
                         isDebugAllow = isDebugAllow,
                         isGrantAllPms = isGrantAllPms,
                         isInstant = isInstant,
                         isTestAllow = isTestAllow,
                         abi = abi,
                     )!!
                 }

             } else start { file, device ->
                 device.executeWithResult("sideload",file.absolutePath,isStart = false)
             }
         }
         fun updateSubTitle(msg:String) = msg.let {
             smallTitle=it+"\n"
             message+=it
         }
         private suspend inline fun start(crossinline collector: (File,ADBDevice)->Shell) = devices.flatMapConcat {device->
             files.map { file ->
                 "device:${device.serial},file:${file.name}" to collector(file,device)
             }.asFlow()
         }.start()


         private suspend fun Flow<Pair<String, Shell>>.start() =
             flatMapConcat { (info,s)->
                current=s.id
                 updateSubTitle("正在执行#${current}-info-$info")
                s
            }.mapNotNull r@{ r->
                when(r) {
                    is ProcessingResults.Error ->
                        message += "+ ${r.message}"
                    is ProcessingResults.Message ->
                        message += "  ${r.message}"
                    is ProcessingResults.Closed ->
                        message += "  完成#${current}"
                    is ProcessingResults.CODE ->
                        return@r (r.code == 0).also { isSuccess = it }
                }
                null
            }.toList().let { list ->
                buildString {
                    append("所有任务完成")
                    if (list.size>1)
                        append("，共有${list.size }个任务，")
                    append(list.count { !it }
                        .takeIf { it!=0 && list.size!=it }
                        ?.let { "，失败${it}个" }
                        ?: "，全部失败"
                    )
                    append("。")
                }
            }.let {
                isDone = true
                 updateSubTitle(it)
            }

    }
    companion object {

        val scope = CoroutineScope(CoroutineName("InvokeScope")+Dispatchers.IO)

        val devices = flow {
            ADB.devices.collect(::emit)
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
            Install(file).takeIf { it.apk!=null }
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
