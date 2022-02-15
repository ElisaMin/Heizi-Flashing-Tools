package me.heizi.flashing_tool.fastboot.screen

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.window.Window
import fastbootIconBuffered
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.heizi.flashing_tool.vd.fb.FastbootDevice
import me.heizi.flashing_tool.vd.fb.fastboot.FastbootCommandViewModel
import me.heizi.flashing_tool.vd.fb.fastboot.fastbootCommand
import me.heizi.flashing_tool.vd.fb.info.DeviceInfo
import me.heizi.flashing_tool.vd.fb.scope

@Composable
fun DeviceManagerWindow(
    viewModel: DeviceManagerViewModel,
    onExit:()->Unit,
) {
    Window(onExit,title = viewModel.device.serialID,icon = fastbootIconBuffered.toPainter()) {
        DeviceManagerScreen(viewModel)
    }
}
interface DeviceManagerViewModel {

    val device: FastbootDevice
    val isSlotA: Boolean?
    val deviceSimpleInfo: Map<String, String>
    @Composable
    fun collectPipe()
    @Composable fun setUpSimpleInfo()
    fun switchPartition(isSlotA: Boolean)

}

annotation class FastbootOperate(val name:String)



open class DeviceManagerViewModelImpl(
    serialID: String
): DeviceManagerViewModel {


    private var fastbootCommandBuffer: MutableState<FastbootCommandViewModel?> = mutableStateOf(null)


    override val device: FastbootDevice = DeviceInfo(serialID)
    override val isSlotA: Boolean? get()  = device.currentSlotA
    override val deviceSimpleInfo: MutableMap<String, String> = mutableStateMapOf()

    @Composable
    override fun setUpSimpleInfo() {

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

    @FastbootOperate("OEM解锁") fun oemUnlock() {
        device run "oem unlock"
    }
    @FastbootOperate("重启") fun reboot() {
        device run "reboot"
    }
    @FastbootOperate("重置") fun wipe() {
        device run "-w"
    }
    @FastbootOperate("重启到Fastbootd") fun rebootToFastbootd() {
        device run "reboot fastboot"
    }
    //    @FastbootOperate("重启到Rec") fun rebootToFastbootd() {
//        device run "reboot fastboot"
//    }
    //        println("boot")
    @Composable
    override fun collectPipe() {
        scope.launch {
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

