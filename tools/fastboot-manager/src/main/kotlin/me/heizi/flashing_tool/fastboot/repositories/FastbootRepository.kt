package me.heizi.flashing_tool.fastboot.repositories

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow
import me.heizi.flashing_tool.fastboot.screen.FastbootCommandViewModel
import me.heizi.kotlinx.logger.debug
import me.heizi.kotlinx.logger.println
import java.text.DecimalFormat


/**
 * Device Runner
 *   run getvar -> Device Info
 *   run command -> Command Info :
 *     1.user click btn to active program
 *     2.cache the user command (or not)
 *     3.collector collect that command
 *     4.show a dialog and user said yes
 *     5.run
 *       1.remove cached command
 *       2.start fastboot progress
 *       3.collect results and reactive
 *     6.end
 */
interface DeviceRunner {
    val serialId: String
    /**
     * don't show anything if null or show a dialog
     */
//    val currentCommand: State<FastbootCommandViewModel?>
    infix fun run(command: String)
    infix fun run(viewModel: FastbootCommandViewModel)
    suspend fun getvar():String?
    @Composable
    fun start()
}


interface FastbootDeviceInfo {
    val simple:Simple
    val partitionInfos:List<PartitionInfo>
    fun toArray():Array<Array<String>>
    operator fun get(s: String): String?
    interface Simple {
        val isUnlocked:Boolean
        val isMultipleSlot:Boolean
        val isFastbootd:Boolean?
        val currentSlotA:Boolean?
    }
    companion object {
        val empty:FastbootDeviceInfo = EmptyFastbootDeviceInfo
    }
}
object EmptyFastbootDeviceInfo : FastbootDeviceInfo {
    override val simple: FastbootDeviceInfo.Simple = object :FastbootDeviceInfo.Simple {
        override val isUnlocked: Boolean = true
        override val isMultipleSlot: Boolean = true
        override val isFastbootd: Boolean? = null
        override val currentSlotA: Boolean = true
    }
    override val partitionInfos: List<PartitionInfo> = emptyList()
    override fun toArray(): Array<Array<String>> = emptyArray()
    override fun get(s: String): String? =null
}
interface FastbootDevice {
    val serialId:String
    val runner: DeviceRunner
    val info: StateFlow<FastbootDeviceInfo>
    suspend fun getInfo(): FastbootDeviceInfo {
        return runner.getvar()?.let(::FastbootDeviceInfoImpl) ?: EmptyFastbootDeviceInfo
    }
    suspend fun updateInfo()
}

private class FastbootDeviceInfoImpl(
    private val cache: Array<Array<String>>,
):FastbootDeviceInfo {

    override val simple = object: FastbootDeviceInfo.Simple {
        override val isFastbootd = get("is-userspace") == "yes"
        override val currentSlotA = when (val slot = get("current-slot")) {
            null -> null
            "a" -> true
            "b" -> false
            else -> throw IllegalStateException("设备的current slot不在ab里:$slot")
        }
        override val isMultipleSlot = (get("slot-count")?.toIntOrNull() ?: -1) > 2
        override val isUnlocked = get("unlocked") == "yes"

    }
    override val partitionInfos by lazy { buildList {
        val formatter = DecimalFormat("0.00")
        val pCache = HashMap<String,Triple<String,Long,Boolean?>>()
        for (it in cache) {
            val prefix = it[0]
            val ptsName = it.getOrNull(1)
            val data = it.getOrNull(2)
            if (ptsName==null) continue
            val info = pCache[ptsName]
            if (simple.isFastbootd == true && prefix == "is-logical") {
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

        for ((name,data,) in pCache) {
            val (type,sizes,isLogic) = data
            val size = formatter.format(sizes.toFloat()/(1024f*1024f)).toFloat()
            val pType = when(type.lowercase()) {
                "ext4" -> PartitionType.EXT4
                "raw" -> PartitionType.RAW
                "f2fs" -> PartitionType.F2FS
                else -> error(type)
            }
            this.add(PartitionInfo(name, pType, size,isLogic))
        }
        debug("partition count",this.size)
    } }
    override operator fun get(s: String): String? {
        for (strings in cache) {
            if (strings.size == 2 && strings[0] == s) return strings[1]
            else if (strings.dropLast(1).joinToString(":") == s) return strings.last()
        }
        return null
    }

    override fun toArray(): Array<Array<String>> = cache

    private fun initialization() {
        updatePartition()
    }
    private fun updatePartition() {
        partitionInfos
    }

    init {
        initialization()
    }
    constructor(getvar:String) : this(buildList {
        debug("dealing getvar",getvar.count { it == '\n'  })
        getvar.lineSequence().map {
            it.replaceFirst("(bootloader)","").split(":")
        }.filter {
            it.size >= 2
        }.forEach { list ->
            add(list.map { it.trim() }.toTypedArray())
        }
    }.toTypedArray())

}


