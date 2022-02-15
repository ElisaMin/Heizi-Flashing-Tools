package me.heizi.flashing_tool.vd.fb

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.runBlocking
import me.heizi.flashing_tool.vd.fb.fastboot.FastbootCommandViewModel
import me.heizi.flashing_tool.vd.fb.info.PartitionInfo


interface FastbootDevice {
    val serialID:String
    val partitions:List<PartitionInfo>
    val isUnlocked:Boolean
    val isMultipleSlot:Boolean
    val currentSlotA:Boolean?
    val asMap:Map<String,String>
    val fastbootCommandPipe: SharedFlow<FastbootCommandViewModel>
    suspend fun executeFastboot(command:String,onDone: () -> Unit ={})
    infix fun run(command: String) = runBlocking {
        executeFastboot(command)
    }
    fun run(command: String,onDone: () -> Unit) = runBlocking {
        executeFastboot(command,onDone)
    }
    fun refreshInfo():Job
//    suspend fun flash(partitionInfo: PartitionInfo,file:File) =
//        executeFastboot("flash ${partitionInfo.name} $file ")

}


