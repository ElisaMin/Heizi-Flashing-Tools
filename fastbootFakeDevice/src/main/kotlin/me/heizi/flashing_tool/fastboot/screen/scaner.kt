package me.heizi.flashing_tool.fastboot.screen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import me.heizi.flashing_tool.fastboot.fastbootIconBuffered
import me.heizi.flashing_tool.fastboot.repositories.Fastboot
import me.heizi.flashing_tool.fastboot.screen.Trays.Companion.isRunning
import me.heizi.kotlinx.compose.desktop.core.components.AboutExtendCard
import kotlin.system.exitProcess

@Composable
@Preview
private fun preview() {
    object : ScannerViewModel {
        override val traysViewModel: TraysViewModel = object : TraysViewModel() {
            override fun exit() {}
            override fun onTrayIconSelected() {}
            override fun onStopCollecting() {}
            override fun onStartCollecting() {}
        }
        override val devices: List<String> get() = listOf("LMV600TM4bf4e87d")
        override fun onDeviceSelected(serial: String) { exitProcess(0) }
        override fun onInit() {}
    } .ScannerScreen()
}

fun main() {
    singleWindowApplication {
        preview()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ScannerDialog(viewModel: ScannerViewModel,exitApp:()->Unit = {},onCloseRequest:()->Unit) {
    var closing by remember { mutableStateOf(false) }
    val state = DialogState(size = DpSize(500.dp,600.dp))
    Dialog({ closing = true },title = "????????????",icon = fastbootIconBuffered.toPainter(), state = state) {
        if (closing) AlertDialog(
        title = {
            Text("???????????????")
        }, text = {
            Text("    ????????????????????????????????????????????????FB?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????")
        }, confirmButton = {
            Button(onCloseRequest) { Text("????????????") }
        }, dismissButton ={
            TextButton(exitApp) { Text("????????????") }
            TextButton({ closing = false }) { Text("??????") }
        }, onDismissRequest = {
            closing = true
        }, modifier = Modifier.defaultMinSize(300.dp))
        viewModel.ScannerScreen()
    }
}
abstract class FlowCollectedScannerViewModel:ScannerViewModel {
    final override val devices = mutableStateListOf<String>()
    private var gotDeviceCollected = false
    override fun onInit() {
        CoroutineScope(Default).launch {
            Fastboot.deviceSerials.collect {
                devices.clear()
                devices.addAll(it)
                if (!gotDeviceCollected) {
                    if (it.isNotEmpty())  {
                        onFirstTimeCollected()
                        gotDeviceCollected = true
                    }
                }
            }
        }
    }

    open fun onFirstTimeCollected() {}
}
interface ScannerViewModel {
    val traysViewModel:TraysViewModel
    val devices:List<String>
    fun onDeviceSelected(serial:String)
    fun onInit()
}
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScannerViewModel.ScannerScreen(
) {


//    Image(fastbootIconBuffered.toImage().toComposeImageBitmap(),"background",modifier = Modifier.fillMaxHeight().fillMaxWidth())


    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)) {


        val isRunning by remember {
            isRunning
        }
        SmallTopAppBar(title = {
            Row {
                Text("???????????????:${devices.size}",style = MaterialTheme.typography.displayMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
//                Text(":${devices.size}",style = MaterialTheme.typography.displayLarge)
            }
        }, actions = {
            Button({
                with(traysViewModel) {
                    if (isRunning) onStopCollecting() else onStartCollecting()
                }
            }) {
                if (isRunning) {
                    Text("???????????????")
                } else {
                    Text("???????????????")
                }
            }

        })
        if (isRunning) {
            LinearProgressIndicator(Modifier.padding(horizontal = 16.dp).height(6.dp).fillMaxWidth())
        } else Spacer(Modifier.height(6.dp))

        AboutExtendCard(true)

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(devices) {
                Card(modifier = Modifier.padding(top=8.dp)) {
                    ListItem(modifier = Modifier.clickable {
                        onDeviceSelected(it)
                    }.padding(4.dp)) { Text(it) }
                }
            }
        }


    }
    LaunchedEffect(this) {
        onInit()
    }
}