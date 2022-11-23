package me.heizi.flashing_tool.sideloader.screens

import androidx.compose.runtime.Composable
import me.heizi.flashing_tool.adb.ADBDevice
import me.heizi.flashing_tool.sideloader.Context



interface ViewModel:Context {

    val devices:List<ADBDevice>
    val isWaiting:Boolean
    fun addDevice(serial:String):Boolean
    // do it on compose
    // fun onConnectRequest(contextState: InnerDeviceContextState)

}

@Composable
fun ViewModel.Full() {

}

@Composable
fun Snackbar() {}