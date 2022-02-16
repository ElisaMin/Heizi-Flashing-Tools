package me.heizi.flashing_tool.vd.fb.info

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import me.heizi.flashing_tool.vd.fb.FastbootDevice
import me.heizi.flashing_tool.vd.fb.fastboot.FastbootCommandViewModel
import me.heizi.flashing_tool.vd.fb.scope
import me.heizi.kotlinx.logger.debug
import me.heizi.kotlinx.shell.CommandResult
import me.heizi.kotlinx.shell.Shell
import java.text.DecimalFormat
import kotlin.coroutines.EmptyCoroutineContext

open class DeviceInfo(
    override val serialID: String
): FastbootDevice {


    override val partitions: MutableList<PartitionInfo> = mutableStateListOf()
    override var isUnlocked: Boolean by mutableStateOf(false)
    override var isMultipleSlot: Boolean by mutableStateOf(false)
    override var isFastbootd: Boolean? by mutableStateOf(null)
    override var currentSlotA: Boolean? by mutableStateOf<Boolean?>(null)
    override val fastbootCommandPipe: MutableSharedFlow<FastbootCommandViewModel> = MutableSharedFlow()

    override val cache = ArrayList<Array<String>>()
    override fun get(s: String): String? {
        for (strings in cache) {
            if (strings.size == 2 && strings[0] == s) return strings[1]
            else if (strings.dropLast(1).joinToString(":") == s) return strings.last()
        }
        return null
    }



    override fun refreshInfo()
        = refreshInfo {  }

    open suspend fun getvarAll() = Shell("fastboot -s $serialID getvar all",isMixingMessage = true, runCommandOnPrefix = true).await().let {
        if (it is CommandResult.Success) onGetvarMessage(it.message)
    }


    open fun refreshInfo(onDone: () -> Unit) :Job {
        debug("refresh called")
        return scope.launch {
            getvarAll()
            onDone()
        }
    }
    override suspend fun executeFastboot(command: String,onDone:()->Unit) {
        fastbootCommandPipe.emit(FastbootCommandViewModel(command, serialID,onDone = onDone))
    }

    private fun updateFiled() {
        isFastbootd = this["is-userspace"] == "yes"

        currentSlotA = when (val slot = this["current-slot"]) {
            null -> null
            "a" -> true
            "b" -> false
            else -> throw IllegalStateException("设备的current slot不在ab里:$slot")
        }
        isMultipleSlot = (this["slot-count"]?.toIntOrNull() ?: -1) > 2
        isUnlocked = this["unlocked"] == "yes"
    }
    private fun updatePartitions() {
        val formatter = DecimalFormat("0.00")
        val pCache = HashMap<String,Triple<String,Long,Boolean?>>()
        for (it in cache) {
            val prefix = it[0]
            val ptsName = it.getOrNull(1)
            val data = it.getOrNull(2)
            if (ptsName==null) continue
            val info = pCache[ptsName]
            if (isFastbootd == true && prefix == "is-logical") {
                val isLogical:Boolean? = when (data) {
                    "yes" -> true
                    "no" -> false
                    "",null-> null
                    else -> {
                        println("unexpected filed $data isLogical ")
                        null
                    }
                }
                pCache[ptsName] = pCache[ptsName]?.copy(third = isLogical) ?: Triple("Nothings!",Long.MAX_VALUE,isLogical)

            } else if (prefix.startsWith("partition-")) when(prefix.replace("partition-", "")) {
                "type" -> {
                    pCache[ptsName] = info?.copy(first = data!!) ?: Triple(data!!,Long.MAX_VALUE,false)
                }
                "size" -> {
                    java.lang.Long.parseLong(data!!.drop(2), 16).let {
                        pCache[ptsName] = info?.copy(second = it) ?:Triple("Nothings!", Long.MAX_VALUE,false)
                    }

                }
            }
        }

        pCache.map { (name,data,) ->
            val (type,sizes,isLogic) = data
            val size = formatter.format(sizes.toFloat()/(1024f*1024f)).toFloat()
            val pType = when(type.lowercase()) {
                "ext4" -> PartitionType.EXT4
                "raw" -> PartitionType.RAW
                "f2fs" -> PartitionType.F2FS
                else -> error(type)
            }
            PartitionInfo(name, pType, size, this,isLogic)
        }.let {
            partitions.clear()
            partitions.addAll(it)
            partitions.sortBy { it.name }
            debug("partition count",partitions.size)
        }
    }
    fun onGetvarMessage(message:String) {
        debug("dealing message",message.count { it == '\n'  })
        cache.clear()
        message.lineSequence().map {
            it.replaceFirst("(bootloader)","").split(":")
        }.filter {
            it.size >= 2
        }.forEach { list ->
            cache.add(list.map { it.trim() }.toTypedArray())
        }
        updateFiled()
        updatePartitions()
    }

    fun getvarLoop() = CoroutineScope(EmptyCoroutineContext).launch {
        while (isActive) {
            refreshInfo {
                debug("refresh done")
            }.join()
            delay(30000)
        }
    }

    init {
        getvarLoop()

    }

}