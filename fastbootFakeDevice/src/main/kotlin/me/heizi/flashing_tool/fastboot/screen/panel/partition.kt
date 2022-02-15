package me.heizi.flashing_tool.fastboot.screen.panel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.heizi.flashing_tool.fastboot.screen.dialogOfFlashing
import me.heizi.flashing_tool.vd.fb.FastbootDevice
import me.heizi.flashing_tool.vd.fb.info.PartitionInfo
import me.heizi.flashing_tool.vd.fb.info.PartitionType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun panelPartition(list:List<PartitionInfo>,fastbootDevice: FastbootDevice) {
    val isDialogShow = mutableStateOf(false)
    var type by remember { mutableStateOf("") }
    if (type.contains('\n')) {
        type=type.replace("\n","")
        isDialogShow.value = true
    }

    dialogOfFlashing(
        mutableStateOf(PartitionInfo(type, PartitionType.Typing,0.0f,fastbootDevice)).value
        ,isDialogShow)

    Column(
        modifier = Modifier.padding(top=8.dp).padding(horizontal = 8.dp)
            .fillMaxWidth()
    ) {
        OutlinedTextField(type, onValueChange = { type = it }, modifier = Modifier.fillMaxWidth().background(Color.White), placeholder = {
            Text("搜索分区或输入分区名称")
        }, trailingIcon = {
            IconButton({
                isDialogShow.value =true
            }) {
                Icon(Icons.Default.Send,"刷入")
            }
        })
        Box(Modifier.padding(top = 8.dp))
//        Box(modifier = Modifier.().scrollable(scroll,Orientation.Vertical)) {
//            FlowRow  {
//                for (it in list.filter { it.name.lowercase().contains(other) }) { partition(it) }
//                if (type.isNotEmpty()) partition(PartitionInfo(type, PartitionType.Typing,0.0f,fastbootDevice))
//            }
//        }


        LazyVerticalGrid(GridCells.Adaptive(132.dp)) {
            val other = type.lowercase().replace(" ", "_")
            items(list.filter { it.name.lowercase().contains(other,)  }) { partition(it) }
            if (type.isNotEmpty()) item { partition(PartitionInfo(type, PartitionType.Typing,0.0f,fastbootDevice)) }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun partition(info: PartitionInfo, modifier: Modifier = Modifier,) {
    val isFlashDialogShowState: MutableState<Boolean> = mutableStateOf(false)
    dialogOfFlashing(info,isFlashDialogShowState)
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
            .then(modifier),
        elevation = 2.dp
    ) {
        Row(Modifier.padding(8.dp)) {
            Column {
                Text(info.name, fontWeight = FontWeight.Bold)
                Text("${info.size}MB ${info.type}")
                DropdownMenu(extend, onDismissRequest = { extend = false }) {
                    Text(info.name, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp, end = 4.dp))
                    DropdownMenuItem({
                        extend = false
                        isFlashDialogShowState.value = true
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

//            Column(Modifier.weight(1f, false)) {
//
//            }
        }

    }
}