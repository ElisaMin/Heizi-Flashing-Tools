package me.heizi.flashing_tool.vd.fb

import androidx.compose.runtime.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.heizi.flashing_tool.vd.fb.fastboot.FastbootCommandViewModel
import me.heizi.flashing_tool.vd.fb.fastboot.fastbootCommand
import me.heizi.flashing_tool.vd.fb.info.DeviceInfo


class ViewModelImpl(
    serialID: String
): ViewModel {


    private var fastbootCommandBuffer: MutableState<FastbootCommandViewModel?> = mutableStateOf(null)


    override val device: FastbootDevice = DeviceInfo(serialID)
    override val deviceSimpleInfo: MutableMap<String, String> = mutableStateMapOf()

    @Composable
    fun setUpSimpleInfo() {
        deviceSimpleInfo["是否有多个SLOT"] = if (device.isMultipleSlot) "多个" else "单个"
        deviceSimpleInfo["是否BL已解锁"]   = if (device.isUnlocked) "已解锁" else "未解锁"
    }


    override fun switchPartition(isSlotA: Boolean) {
        device.run("--set-active=${if (isSlotA) "b" else "a" } ") {
            runBlocking {
                delay(100)
            }
            device.refreshInfo()
        }
    }

    @FastbootOperate("重启") fun reboot() {
        device run "reboot"
    }
    @FastbootOperate("重置") fun wipe() {
        device run "-w"
    }
    //        println("boot")
    @Composable
    override fun collectPipe() {
        GlobalScope.launch {
            device.fastbootCommandPipe.collect {
                fastbootCommandBuffer.value = it
            }
        }
        val buffer by fastbootCommandBuffer
        if (buffer!=null) {
            fastbootCommand(buffer!!) {
                fastbootCommandBuffer.value = null
            }
        }
    }

//    @FastbootOperate("启动镜像") fun boot(){
//    }

}

interface ViewModel {

    val device: FastbootDevice
    val isSlotA: Boolean?
        get() = device.currentSlotA
    @Composable
    fun collectPipe()
    val deviceSimpleInfo: Map<String, String>
    fun switchPartition(isSlotA: Boolean)
}

annotation class FastbootOperate(val name:String)