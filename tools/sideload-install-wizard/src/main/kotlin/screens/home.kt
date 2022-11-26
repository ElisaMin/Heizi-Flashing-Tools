package me.heizi.flashing_tool.sideloader.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.ModifierLocalReadScope
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import me.heizi.flashing_tool.adb.ADBDevice
import me.heizi.flashing_tool.sideloader.Context
import me.heizi.flashing_tool.sideloader.InnerDeviceContextState
import net.dongliu.apk.parser.bean.ApkIcon
import org.jetbrains.compose.splitpane.*

@Stable
interface ViewModel {

    val devices:List<ADBDevice>
    val selected:MutableSet<String>
    val isWaiting:Boolean

    val packageDetails:Map<String,Array<String>>
    val icon:ApkIcon<*>?
    val titleName:String
    val packageName:String?
    val version:String?

    val snacks:SnackbarHostState

    fun addDevice(serial:String):Boolean
    fun onConnectRequest(contextState: InnerDeviceContextState)

    fun switchMode()

    fun nextStep()

    suspend fun CoroutineScope.onLaunching()
    fun onOut()

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
operator fun ViewModel.invoke() {
    LaunchedEffect("launchOnHome") {
        onLaunching()
    }
    DisposableEffect("DisposableOnHome") {
        onDispose {
            onOut()
        }
    }
    Scaffold(
        floatingActionButton = { fab() },
        content = { content(it) },
        snackbarHost = { snackbar() }
    )
}
@Composable
fun ViewModel.snackbar() {
    SnackbarHost(snacks)
}

@Composable
fun ViewModel.fab() {
    if (!isWaiting) ExtendedFloatingActionButton(
        onClick = ::nextStep,
        text = { Text("安装") },
        icon = { Icon(Icons.Default.Send,"start") },
    )
}

@OptIn(ExperimentalSplitPaneApi::class) @Composable
fun ViewModel.content(padding:PaddingValues) = BoxWithConstraints(Modifier.padding(padding)) {
    if (maxWidth> 380.dp) HorizontalSplitPane(splitPaneState = rememberSplitPaneState(0.6f)) {
        first(176.dp,) { first() }
        defaultSplitter()
        second { second(PaddingValues(8.dp)) }
    } else VerticalSplitPane {
        first((176+ 36).dp) { first() }
        defaultSplitter()
        second { second(PaddingValues(horizontal = 8.dp)) }
    }

}
@Composable
fun ViewModel.first() {
    Info(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        packageDetail =packageDetails,
        icon = icon,
        titleName = titleName,
        packageName = packageName,
        version = version,
        switchMode = ::switchMode
    )
}
@Composable
fun ViewModel.second(padding:PaddingValues){
    DeviceScreen(
        modifier = Modifier.padding(padding),
        devices = devices,
        selected = selected,
        isWaiting = isWaiting,
        addDevice = ::addDevice,
        onConnectRequest = ::onConnectRequest
    )
}


