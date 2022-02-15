package me.heizi.flashing_tool.fastboot.screen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.heizi.flashing_tool.vd.fb.info.PartitionInfo
import me.heizi.flashing_tool.vd.fb.style.fileInput
import me.heizi.kotlinx.compose.desktop.core.components.ChipCheckBox
import me.heizi.kotlinx.logger.debug

val defaultPathPartitionInfo = mutableStateOf("")

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun dialogOfFlashing(
    info: PartitionInfo,
    isFlashDialogShowState: MutableState<Boolean> = mutableStateOf(false),
) {
    var isFlashDialogShow by remember { isFlashDialogShowState }
    if (isFlashDialogShow) {
        var path by defaultPathPartitionInfo
        var isAVBEnable by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf("") }
        fun flash() {
            val parName = info.name

            info.device run " ${if (isAVBEnable) " --disable-verity --disable-verification" else "" } flash $parName \"$path\""
        }
        if (path.isEmpty()) error = "路径为空，请再次点击确定确定你要进行特殊操作。"
        AlertDialog(onDismissRequest = {
            isFlashDialogShow = false
        }, dismissButton = {
            TextButton({isFlashDialogShow = false}) {
                Text("关闭")
            }
        }, confirmButton = {
            OutlinedButton(onClick = {
                "confirmButton".debug(error)
                if (error.isEmpty()) {
                    flash()
                    path = ""
                    isFlashDialogShow = false
                } else {
                    error = ""
                }
            }) {
                Text("确认")
            }
        }, title = { Text("选择文件刷入") }, text = {
            Column {
                if (error.isNotEmpty()) Text(error)
                fileInput(
                    path,
                    Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    onFileDialogDismiss = { isFlashDialogShow = true },
                    onFileDialogOpen = { isFlashDialogShow = false }
                ) {
                    path = it
                }
                if (info.name.contains("vbmeta")) ChipCheckBox(
                    isAVBEnable,
                    "disable verity/verification"
                ) { isAVBEnable = it }
            }
        },
//            size = IntSize(700, 320)
        )
    }
}
