package me.heizi.flashing_tool

import androidx.compose.material.ExperimentalMaterialApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

//fun main() {
//    val viewModel = object: DeviceManagerViewModelImpl("LMV600TM4bf4e87d") {
//        override val device: FastbootDevice = object : DeviceInfo(serialID = "LMV600TM4bf4e87d") {
//            override suspend fun getvarAll() {
//                this.onGetvarMessage(fake2)
//            }
//        }
//    }
//    singleWindowApplication {
//        DeviceManagerScreen(viewModel)
//    }
//}
//val fakeScannerViewModel = object : ScannerViewModel {
//    override val devices: List<String>
//        get() = listOf("LMV600TM4bf4e87d")
//
//
//    override fun onDeviceSelected(serial: String) {
////            open(serial)
//    }
//
//}

@OptIn(ExperimentalMaterialApi::class)
fun main() {
    runBlocking {
        val job = launch {
            me.heizi.flashing_tool.vd.fb.main()
        }


        job.join()
    }

}