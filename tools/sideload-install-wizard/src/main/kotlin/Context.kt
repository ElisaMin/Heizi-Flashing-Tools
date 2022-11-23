package me.heizi.flashing_tool.sideloader

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import me.heizi.flashing_tool.adb.ADB
import java.io.File
import kotlin.math.roundToLong

// TODO make it close to [ViewModel]
// TODO new class of files
interface Context {
    val filePath:String
    val fileName:String
    val file:File
    val selected:MutableSet<String>

    private class AbstractContext constructor(
        override val filePath: String,

    ):Context {
        override val file = File(filePath)
        override val fileName: String get() = filePath.split('/','\\').last()
        override val selected: MutableSet<String> get() = Context.selected
    }

    abstract class Sideload
    private constructor(filePath: String):Context by AbstractContext(filePath) {

    }
    //TODO Apk detail map,invoke prm
    abstract class Install private constructor(
    ):Context
     class Invoking private constructor(
         context: Context
     ) :Context by context {
         val message by mutableStateOf("")
         val isSuccess:Boolean? by mutableStateOf(null)
         init {
             TODO("Invoke it")
//             val command = when(context) {
//                 is Sideload -> ::sideload
//             }
//             require(context is Sideload || context is Install)
////             va
//             scope.launch {  }
         }


    }
    companion object {
        val scope = CoroutineScope(CoroutineName("InvokeScope")+Dispatchers.IO)
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
