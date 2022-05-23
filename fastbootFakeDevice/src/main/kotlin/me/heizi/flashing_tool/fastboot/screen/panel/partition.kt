package me.heizi.flashing_tool.fastboot.screen.panel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tunjid.me.core.ui.dragdrop.dropTarget
import me.heizi.flashing_tool.fastboot.repositories.DeviceRunner
import me.heizi.flashing_tool.fastboot.repositories.FastbootDevice
import me.heizi.flashing_tool.fastboot.repositories.PartitionInfo
import me.heizi.flashing_tool.fastboot.repositories.PartitionType
import me.heizi.flashing_tool.fastboot.screen.defaultPathPartitionInfo
import me.heizi.flashing_tool.fastboot.screen.dialogOfFlashing

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun panelPartition(partitionInfos:List<PartitionInfo>, device: FastbootDevice) {
    val isDialogShow = mutableStateOf(false)
    var type by remember { mutableStateOf("") }
    if (type.contains('\n')) {
        type=type.replace("\n","")
        isDialogShow.value = true
    }


    dialogOfFlashing(
        mutableStateOf(PartitionInfo(type, PartitionType.Typing,0.0f)).value
        ,device.runner,isDialogShow)

    Column(
        modifier = Modifier.padding(top=8.dp).padding(horizontal = 8.dp)
            .fillMaxSize()
    ) {

        OutlinedCard (modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.padding(16.dp).padding(vertical = 14.dp).weight(1f)) {
                    if (type.isEmpty()) Text("输入分区名称搜索或点击小飞机直接刷入分区，将文件拖拽到下方分区按钮可快速启动刷入操作。", overflow = TextOverflow.Ellipsis, maxLines = 1)
                    BasicTextField(type,onValueChange = { type = it }, modifier = Modifier.fillMaxWidth())
                }
                IconButton({
                    isDialogShow.value =true
                }) {
                    Icon(Icons.Default.Send,"刷入")
                }
            }

        }

//        OutlinedTextField(type, onValueChange = { type = it }, modifier = Modifier.fillMaxWidth().background(Color.White), placeholder = {
//            Text("搜索分区或输入分区名称")
//        }, trailingIcon = {
//            IconButton({
//                isDialogShow.value =true
//            }) {
//                Icon(Icons.Default.Send,"刷入")
//            }
//        })
        Box(Modifier.padding(top = 8.dp))
//        var scrollstate by remember { mutableStateOf(0f) }
//        Box(
//            modifier = Modifier.fillMaxSize()
//                .scrollable(ScrollableState { scrollstate }, Orientation.Vertical)
//                .border(3.dp,Color.LightGray)
//        ) {
//            val other = type.lowercase().replace(" ", "_")
//            FlowRow(
//                modifier = Modifier
//                    .border(6.dp,Color.Black).matchParentSize()
////                .scrollable(rememberScrollState(), Orientation.Vertical)
//            )  {
//                for (it in list.filter { it.name.lowercase().contains(other) }) { partition(it) }
//                if (type.isNotEmpty()) partition(PartitionInfo(type, PartitionType.Typing,0.0f,fastbootDevice))
//            }
//        }



        LazyVerticalGrid(GridCells.Adaptive(if (device.info.value.simple.isFastbootd == true) 160.dp else 132.dp)) {

            val displayingPartitions = type.lowercase().replace(" ", "_").trim().let { other->
                var dps = partitionInfos.filter { it.name.lowercase().contains(other)  }
                if (other.isNotEmpty()) dps = buildList {
                    dps.forEach(::add)
                    add(PartitionInfo(type, PartitionType.Typing,0.0f))
                }
                dps
            }
            items(displayingPartitions) {
                partition(it, device.runner, Modifier.animateItemPlacement())
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun partition(info: PartitionInfo, runner: DeviceRunner, modifier: Modifier = Modifier,) {
    var isSelected by remember { mutableStateOf(false) }
    val isFlashDialogShowState: MutableState<Boolean> = mutableStateOf(false)
    dialogOfFlashing(info, runner, isFlashDialogShowState)
    fun erase() {
        runner run "erase ${info.name}"
    }
    val background = if (isSelected) CardDefaults.cardColors(MaterialTheme.colorScheme.primary,MaterialTheme.colorScheme.onPrimary) else CardDefaults.cardColors()

    var extend by remember { mutableStateOf(false) }
    Card(
        Modifier
            .padding(3.dp)
            .clickable {
                extend = true
            }
            .dropTarget(
                onDropped = {uri,_->
                    if (uri.size==1) defaultPathPartitionInfo.value = uri[0].path
                    isFlashDialogShowState.value = true
                    true
                },
                onDragEntered = {
                    isSelected = true
                }, onDragEnded = {
                    isSelected = false
                },
                onDragStarted = { _, _ ->
                    true
                }, onDragExited = {
                    isSelected = false
                }
            )
            .then(modifier),
        colors = background
//        elevation = 2.dp
    ) {
        Row(Modifier.padding(8.dp)) {
            Column {
                Text(info.name, fontWeight = FontWeight.Bold)
                Text("${info.size}MB ${info.type} ${if (info.isLogic==true) "动态分区" else ""}")
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
        }
    }
}