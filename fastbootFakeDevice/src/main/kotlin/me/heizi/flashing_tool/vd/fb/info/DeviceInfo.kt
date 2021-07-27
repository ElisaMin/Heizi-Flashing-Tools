package me.heizi.flashing_tool.vd.fb.info

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import me.heizi.flashing_tool.vd.fb.FastbootDevice
import me.heizi.flashing_tool.vd.fb.fastboot.FastbootCommandViewModel
import me.heizi.flashing_tool.vd.fb.scope
import me.heizi.kotlinx.logger.debug
import me.heizi.kotlinx.shell.CommandResult
import me.heizi.kotlinx.shell.CommandResult.Companion.waitForResult
import me.heizi.kotlinx.shell.shell
import java.text.DecimalFormat

open class DeviceInfo(
    override val serialID: String
): FastbootDevice {


    override val partitions: MutableList<PartitionInfo> = mutableStateListOf()
    override val isUnlocked: Boolean get() = _isUnlocked.value
    override val isMultipleSlot: Boolean get() = _isMultipleSlot.value
    override val currentSlotA: Boolean? get() = _currentSlotA.value
    override val asMap:HashMap<String,String> = hashMapOf()
    override val fastbootCommandPipe: MutableSharedFlow<FastbootCommandViewModel> = MutableSharedFlow()

    private var _currentSlotA = mutableStateOf<Boolean?>(null)
    private var _isUnlocked = mutableStateOf(false)
    private var _isMultipleSlot = mutableStateOf(false)

    override fun refreshInfo() {
        refreshInfo {  }
    }

    fun refreshInfo(onDone: () -> Unit) {
        debug("refresh called")
        scope.launch {
            shell("fastboot -s $serialID getvar all",isMixingMessage = true).waitForResult(onResult = {
                if (it is CommandResult.Success) onGetvarMessage(it.message)
            })
            _currentSlotA.value = when (val slot = asMap["current-slot"]) {
                null -> null
                "a" -> true
                "b" -> false
                else -> throw IllegalStateException("设备的current slot不在ab里:$slot")
            }
            _isMultipleSlot.value = asMap["slot-count"] == "2"
            _isUnlocked.value = asMap["unlocked"] == "yes"
            onDone()
        }
    }
    override suspend fun executeFastboot(command: String,onDone:()->Unit) {
        fastbootCommandPipe.emit(FastbootCommandViewModel(command, serialID,onDone = onDone))
    }

    open fun onGetvarMessage(message:String) {

        val formatter = DecimalFormat("0.00")
        val cache = HashMap<String,Pair<String,Long>>()


        message.lineSequence().map {
            it.replaceFirst("(bootloader) ","").split(":")
        }.filter {
            it.size >= 2
        }.forEach { list ->

            if (list.size == 2) {
                asMap[list[0]] = list[1]
            } else {
                val key = list.subList(0, list.lastIndex - 1).joinToString(":")
                asMap[key] = list.last()
            }
            //分区名称
            if (list[0].startsWith("partition-")) {
                val ptsName = list[1]
                val value = list[2].trim()
                val info = cache[ptsName]
                when (list[0].replace("partition-", "")) {
                    "type" -> {
                        cache[ptsName] = when {
                            info == null -> {
                                value to Long.MAX_VALUE
                            }
                            info.first == "Nothings!" -> info.copy(first = value)
                            else -> error("$ptsName $value")
                        }
                    }
                    "size" -> {
                        val value = java.lang.Long.parseLong(value.drop(2), 16)
                        cache[ptsName] = when {
                            info == null -> {
                                "Nothings!" to Long.MAX_VALUE
                            }
                            info.second == Long.MAX_VALUE -> info.copy(second = value)
                            else -> error("$ptsName $value")
                        }
                    }
                }
            }
        }
        cache.map {(name,data)->
            val (type,sizes) = data
            val size = formatter.format(sizes.toFloat()/(1024f*1024f)).toFloat()
            val pType = when(type.toLowerCase()) {
                "ext4" -> PartitionType.EXT4
                "raw" -> PartitionType.RAW
                else -> error(type)
            }
            PartitionInfo(name, pType, size, this)
        }.let {
            partitions.clear()
            partitions.addAll(it)
            partitions.sortBy { it.name }
        }
    }

    init {
        scope.launch {
            while(true) {
                delay(30000)
                refreshInfo()
            }
        }
    }

}