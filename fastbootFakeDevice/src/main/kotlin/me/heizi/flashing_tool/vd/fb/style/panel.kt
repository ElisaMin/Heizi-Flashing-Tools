package me.heizi.flashing_tool.vd.fb.style

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.heizi.flashing_tool.vd.fb.dialogProperties
import me.heizi.flashing_tool.vd.fb.info.PartitionInfo
import me.heizi.kotlinx.compose.desktop.core.components.ChipCheckBox

val defaultPathPartitionInfo = mutableStateOf("")
@Composable
fun partition(info: PartitionInfo, modifier: Modifier = Modifier,) {
    var isFlashDialogShow by remember { mutableStateOf(false) }
    var path by defaultPathPartitionInfo
    if (isFlashDialogShow) {
        var isAVBEnable by remember { mutableStateOf(false) }
        fun flash() {
            val parName = info.name
            info.device run " ${if (isAVBEnable) " --disable-verity --disable-verification" else "" } flash $parName $path"
        }
        AlertDialog(onDismissRequest = {
            isFlashDialogShow = false
        }, confirmButton = {
            OutlinedButton(onClick = {
                flash()
                isFlashDialogShow = false
            }) {
                Text("确认")
            }
        }, title = { Text("选择文件刷入") }, text = {
            Column {
                fileInput(
                    path,
                    Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    onFileDialogDismiss = { isFlashDialogShow = true },
                    onFileDialogOpen = { isFlashDialogShow = false }) { path = it }
                if (info.name.contains("vbmeta")) ChipCheckBox(
                    isAVBEnable,
                    "disable verity/verification"
                ) { isAVBEnable = it }
            }
        }, properties = dialogProperties.copy(size = IntSize(700, 320)))
    }
    fun erase() {

        info.device run "erase ${info.name}"
    }
    var extend by remember { mutableStateOf(false) }
    Card(
        Modifier
            .padding(3.dp)
            .clickable {
                extend = true
            }
//        .height(82.dp)
//        .width(128.dp)
            .then(modifier),
        elevation = 3.dp
    ) {
        Row(Modifier.padding(8.dp)) {
            Column(Modifier.weight(3f)) {
                Text(info.name, fontWeight = FontWeight.Bold)
                Text("${info.size}MB ${info.type}")
            }

            Column(Modifier.weight(1f, false)) {
                DropdownMenu(extend, onDismissRequest = { extend = false }) {
                    Text(info.name, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp, end = 4.dp))
                    DropdownMenuItem({
                        extend = false
                        isFlashDialogShow = true
                    }) {
                        Text("选择文件刷入")
                    }
                    DropdownMenuItem({
                        extend = false
                        erase()
                    }) {
                        Text("清空分区")
                    }
                }
            }
        }

    }
}

@Composable
fun panelAbSlotSwitch(isSlotA:Boolean,onClick :(Boolean)->Unit) = TextButton(onClick = {
    onClick.invoke(isSlotA)
}, modifier = Modifier.padding(16.dp)) {
    val bottom = Modifier.align(Alignment.Bottom)

    @Composable
    fun text(text: String, enable: Boolean) = if (enable)
        Text(text, fontSize = 72.sp, fontWeight = FontWeight.Bold, modifier = bottom) else
        Text(text, fontSize = 47.sp, modifier = bottom.padding(bottom = 6.dp), color = Color.LightGray)

    text("A", isSlotA)
    text("B", !isSlotA)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun panelPartition(list:List<PartitionInfo>) {
    var type by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        OutlinedTextField(type, onValueChange = { type = it }, modifier = Modifier.fillMaxWidth(), placeholder = {
            Text("搜索分区")
        })
        Box(Modifier.padding(top = 8.dp))
        LazyVerticalGrid(GridCells.Adaptive(132.dp)) {
            val other = type.lowercase().replace(" ", "_")
            items(list.filter { it.name.lowercase().contains(other) }) { partition(it) }
        }
    }
}