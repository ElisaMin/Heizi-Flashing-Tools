package me.heizi.flashing_tool.fastboot.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import me.heizi.flashing_tool.fastboot.screen.panel.panelAbSlotSwitch
import me.heizi.flashing_tool.fastboot.screen.panel.panelPartition
import me.heizi.flashing_tool.vd.fb.extendableCard


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DeviceManagerScreen(viewModel: DeviceManagerViewModel) {
    viewModel.collectPipe()
    viewModel.setUpSimpleInfo()

    Column(modifier = Modifier
        .background(Color(0xffFdFdFd))
        .padding(horizontal = 16.dp)
        .padding(top = 16.dp)
        .fillMaxWidth()
        .fillMaxHeight()
    ) {
        Text(viewModel.device.serialID, style = MaterialTheme.typography.h2)
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ){

            LazyColumn(modifier = Modifier.defaultMinSize(minWidth = 172.dp)) {
                item {
                    extendableCard("设备信息") {
                        for ((key,value) in viewModel.deviceSimpleInfo) {
                            ListItem(text = { Text(key) },secondaryText = { Text(value) })
                        }
                    }
                }

                if (viewModel.isSlotA!=null) item {
                    extendableCard("Slot",true) {
                        panelAbSlotSwitch(viewModel.isSlotA!!,viewModel::switchPartition)
                    }
                }
            }
            Column {
                extendableCard("常规操作", modifier = Modifier.fillMaxWidth(), initExtend = false) {
                    val padding = Modifier.padding(4.dp)
                    FlowRow {
                        for (it in viewModel::class.java.methods.filter { it.annotations.isNotEmpty() }) {
                            val operate = it.getAnnotation(FastbootOperate::class.java)
                            if (operate != null) {
                                OutlinedButton(onClick = {
                                    it.invoke(viewModel)
                                }, modifier = padding) { Text(operate.name) }
                            }
                        }
                    }
                }
                panelPartition(viewModel.device.partitions,viewModel.device)

            }
        }
    }

}