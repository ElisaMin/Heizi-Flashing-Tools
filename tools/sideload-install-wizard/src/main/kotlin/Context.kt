package me.heizi.flashing_tool.sideloader

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.heizi.flashing_tool.adb.ADB
import me.heizi.flashing_tool.adb.ADBDevice
import me.heizi.flashing_tool.adb.install
import me.heizi.kotlinx.shell.ProcessingResults
import me.heizi.kotlinx.shell.Shell
import java.io.File
import java.util.StringJoiner
import kotlin.math.roundToLong


// TODO make it close to [ViewModel]
// TODO new class of files
sealed interface Context {
    val files:List<File>

    private class AbstractContext(
        override val files: List<File>
    ):Context {
        constructor(filePath: String) : this(listOf(File(filePath)))
    }


    abstract class Sideload
    private constructor(filePath: String):Context by AbstractContext(filePath) {

    }
    //TODO Apk detail map,invoke prm
    abstract class Install private constructor(
    ):Context {
        //TODO turn it as composable
        interface Info {
            @Text("")
            val isReplaceExisting:Boolean
            val isTestAllow:Boolean
            val isDebugAllow:Boolean
            val isGrantAllPms:Boolean
            val isInstant:Boolean
            val abi:String?
            private annotation class Text(val name:String)
        }

    }


     @OptIn(FlowPreview::class)
     class Invoking private constructor(
         context: Context
     ) :Context by context {
         var subTitle by mutableStateOf("正在预热中...")
             private set
         var message by mutableStateOf("")
             private set
         var isSuccess:Boolean? by mutableStateOf(null)
             private set
         var isDone:Boolean? by mutableStateOf(null)
             private set
         private var current = 1
         private val devices = Context.devices.filter { it.serial in selected }

         fun updateSubTitle(msg:String) = msg.let {
             subTitle=it+"\n"
             message+=it
         }

         private suspend fun start(context: Context) {
             val isApk = context.isAPk
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


         init {
             scope.launch {
                 delay(500)
             }
             updateSubTitle("开始执行。")
             isDone = false

         }


    }
    companion object {

        val scope = CoroutineScope(CoroutineName("InvokeScope")+Dispatchers.IO)

        val current = MutableSharedFlow<Context>()

        val devices = flow {
            ADB.devices.collect(::emit)
            ADB.savedDevices.collect(::emit)
        }
        private val fakeSelected: SnapshotStateMap<String, Nothing?> = mutableStateMapOf()
        val selected: MutableSet<String> = object : MutableSet<String>,Collection<String> by fakeSelected.keys {
            inline fun withTrue(crossinline block:()->Unit):Boolean {
                block()
                return true
            }
            override fun add(element: String): Boolean = withTrue { fakeSelected[element] = null }
            override fun addAll(elements: Collection<String>): Boolean = withTrue { fakeSelected.putAll(elements.map { Pair(it,null) }) }
            override fun clear() = fakeSelected.clear()
            override fun iterator(): MutableIterator<String> = object : MutableIterator<String> {
                val iterable = fakeSelected.iterator()
                override fun hasNext(): Boolean = iterable.hasNext()
                override fun next(): String = iterable.next().key
                override fun remove() = iterable.remove()
            }
            override fun retainAll(elements: Collection<String>): Boolean { throw NotImplementedError() }
            override fun removeAll(elements: Collection<String>): Boolean = withTrue { elements.forEach(fakeSelected::remove) }
            override fun remove(element: String): Boolean =withTrue { fakeSelected.remove(element) }
        }
    }
}
val Context.isAPk get() = when (this) {
    is Context.Sideload -> false
    is Context.Install -> true
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
