package me.heizi.flashing_tool.vd.fb.fastboot

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.system.exitProcess


@Preview
@Composable
fun previewFastbootCommand() {
    Box(modifier = Modifier.fillMaxSize()){
        fastbootCommand(FastbootCommandViewModel("flash looooog-name-partitions with/loooooong/pathname/like/this","n4635554uget",))
    }
}

fun main() = application {
    Window(onCloseRequest = { exitProcess(0)},) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            var showDialog by remember { mutableStateOf(true) }
            Button({showDialog = !showDialog}) {
                Text("fuck")
            }
            if (showDialog) {
                fastbootCommand(FastbootCommandViewModel("flash looooog-name-partitions with/loooooong/pathname/like/this","n4635554uget",),onDismiss = {showDialog=!showDialog})
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun fastbootCommand(viewModel: FastbootCommandViewModel, onDismiss:()->Unit={}) {

    val log = remember { viewModel.log }

    AlertDialog({},title = {
        Text("设备:${viewModel.serialID}")
    },text = {
        Column {
            Text(text = "正在请求执行:\n${viewModel.command}",modifier = Modifier.padding(vertical = 6.dp).alpha(
                ContentAlpha.medium))
            if (viewModel.isRunning==true) LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical = 6.dp))
            if (viewModel.isRunning!=false) Text(log,modifier = Modifier.padding(vertical = 6.dp).alpha(
                ContentAlpha.medium))
        }
    },confirmButton = {
        if (viewModel.isRunning==false) OutlinedButton(onClick = { viewModel() }){ Text("下一步") }
    },dismissButton = {
        if (viewModel.isRunning!=true) TextButton(onDismiss) { Text("关闭")}
    },
        modifier = Modifier.defaultMinSize(300.dp,200.dp)
    )

//    Dialog(onCloseRequest = onDismiss) {
//        window.title = ""
//        window.iconImages = emptyList()
//
//        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(16.dp).verticalScroll(ScrollState(0))) {
//            Text(style = MaterialTheme.typography.subtitle1,text = viewModel.serialID,modifier = Modifier.padding(vertical = 6.dp))
//            Text(style = MaterialTheme.typography.body2,text = "正在请求执行:\n${viewModel.command}",modifier = Modifier.padding(vertical = 6.dp).alpha(
//                ContentAlpha.medium))
//            if (viewModel.isRunning==false) OutlinedButton(onClick = { viewModel() },modifier = Modifier.align(
//                Alignment.End)){ Text("下一步") }
//            if (viewModel.isRunning==true) LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical = 6.dp))
//            if (viewModel.isRunning!=false) Text(viewModel.log,style = MaterialTheme.typography.body2,modifier = Modifier.padding(vertical = 6.dp).alpha(
//                ContentAlpha.medium))
//        }
//
//
//
//    }
}