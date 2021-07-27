package me.heizi.flashing_tool.vd.fb.fastboot

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.v1.Dialog
import me.heizi.flashing_tool.vd.fb.dialogProperties

@Composable
fun fastbootCommand(viewModel: FastbootCommandViewModel, onDismiss:()->Unit={}) {

    Dialog(onDismissRequest = onDismiss,properties = dialogProperties){
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(16.dp).verticalScroll(ScrollState(0))) {
            Text(style = MaterialTheme.typography.subtitle1,text = viewModel.serialID,modifier = Modifier.padding(vertical = 6.dp))
            Text(style = MaterialTheme.typography.body2,text = "正在请求执行:\n${viewModel.command}",modifier = Modifier.padding(vertical = 6.dp).alpha(
                ContentAlpha.medium))
            if (viewModel.isRunning==false) OutlinedButton(onClick = { viewModel() },modifier = Modifier.align(Alignment.End)){ Text("下一步") }
            if (viewModel.isRunning==true) LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical = 6.dp))
            if (viewModel.isRunning!=false) Text(viewModel.log,style = MaterialTheme.typography.body2,modifier = Modifier.padding(vertical = 6.dp).alpha(
                ContentAlpha.medium))
        }
    }
}