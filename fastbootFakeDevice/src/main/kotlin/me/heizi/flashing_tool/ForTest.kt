package me.heizi.flashing_tool

import androidx.compose.ui.window.application
import fake3
import me.heizi.flashing_tool.fastboot.screen.DeviceManagerViewModelImpl
import me.heizi.flashing_tool.fastboot.screen.DeviceManagerWindow
import me.heizi.flashing_tool.vd.fb.FastbootDevice
import me.heizi.flashing_tool.vd.fb.info.DeviceInfo

fun main() {
    val viewModel = object: DeviceManagerViewModelImpl("LMV600TM4bf4e87d") {
        override val device: FastbootDevice = object : DeviceInfo(serialID = "LMV600TM4bf4e87d") {
            override suspend fun getvarAll() {
                this.onGetvarMessage(fake3)
            }
        }
    }
    application {
        DeviceManagerWindow(viewModel,::exitApplication)
    }
}

//@OptIn(ExperimentalMaterialApi::class)
//fun main() {
//    runBlocking {
//        val job = launch {
//            me.heizi.flashing_tool.vd.fb.main()
//        }
//
//
//        job.join()
//    }
//
//}