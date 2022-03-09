package me.heizi.flashing_tool.image.fragment

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.heizi.flashing_tool.image.style
import me.heizi.kotlinx.compose.desktop.core.components.ChipCheckBox
import me.heizi.kotlinx.logger.debug

class Launcher:LauncherViewModel, Fragment<LauncherViewModel>(_content =  @Composable {
    title = file.name
    subtitle = "你想要刷入哪个分区里面?"
    viewModel.checkInput()
    viewModel.launcherScreen()
}) {


    override var partition by mutableStateOf("")

    override var error by mutableStateOf("")
    override var hasNext: Boolean by mutableStateOf(false)
    override var isDropDown by mutableStateOf(false)



    @Composable
    override fun checkInput() {
        error = when {
            partition.contains("_",) -> {
                "错误!包含'_'字符"
            }
            partition.contains(",",) -> {
                "错误!包含','字符"
            }
            partition.contains(" ",) -> {
                "错误!包含' '字符"
            }
            else -> { "" }
        }
        hasNext = partition.isNotEmpty() && error.isEmpty()
    }

    override fun onNextStepBtnClick() {
        toNextPage(true)
    }

    override fun onBootBtnClick() {
        toNextPage(false)
    }
    fun toNextPage(isNextStep:Boolean) = (if (isNextStep) arrayOf(
        "launchMode" to "flash",
        "partition" to partition,
        "_a" to checkbox._a.value,
        "_b" to checkbox._b.value,
        "disable_avb" to checkbox.disableAVB.value,
    ) else arrayOf("launchMode" to "boot")).let {
        handler.go(DeviceSelector::class,*it)

    }

    override val checkbox: CheckboxesViewModel = CheckboxesViewModel()
    override val viewModel: LauncherViewModel = this
}

@Composable
@Preview
private fun preview() {
    val vm = object : LauncherViewModel {
        override var partition: String = "partitions"
        override var error: String = ""
        override var hasNext: Boolean = false
        override val checkbox: CheckboxesViewModel = CheckboxesViewModel()
        override var isDropDown: Boolean = false

        @Composable
        override fun checkInput() {

        }

        override fun onNextStepBtnClick() {

        }

        override fun onBootBtnClick() {
            TODO("Not yet implemented")
        }
    }
//    val vm:InfoViewModel = object : InfoViewModel {
//        override val bools: Triple<Boolean, Boolean, Boolean> = Triple(false,false,false)
//        override val partition: String = "preview"
//        override val device: List<String> = listOf("device1","device2")
//        override val file: String = "fileName"
//
//        override fun onNextStepBtnClicked() {
//
//        }
//    }
    vm.launcherScreen()
}
@Composable
fun LauncherViewModel.launcherScreen(){

    error = when {
        partition.contains("_",) -> {
            "错误!包含'_'字符"
        }
        partition.contains(",",) -> {
            "错误!包含','字符"
        }
        partition.contains(" ",) -> {
            "错误!包含' '字符"
        }
        else -> { "" }
    }
    hasNext = partition.isNotEmpty() && error.isEmpty()



    Column {
        Column (
            modifier = Modifier.fillMaxWidth(),
        ){
            TextField(
                partition,
                modifier = Modifier.fillMaxWidth(),
                isError = error.isNotEmpty(),
                onValueChange = { partition= it },
                label = { Text("分区名称") },
                trailingIcon = {
                    if (!isDropDown)
                    IconButton(onClick = {
                        isDropDown = !isDropDown
                        "Extent".debug("clicked",isDropDown) },) {
                        Icon(Icons.Default.ArrowDropDown,"展开")
                    }
                }
            )
            DropdownMenu(isDropDown,{
                isDropDown = false
            },modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                for (s in arrayOf("system", "boot", "vbmeta", "vendor","recovery")) {
                    DropdownMenuItem(onClick = {
                        partition = s
                        isDropDown = false
                    }) {
                        Text(s)
                    }
                }
            }
        }

        if (!hasNext) Text(error)
        Box(style.padding.bottom)
        //=====
        checkboxes(checkbox)
        //=====
        Button(
            onClick = { if (hasNext) onNextStepBtnClick() },
            modifier = style.padding.vertical.align(Alignment.End),
            enabled = hasNext
        ) {
            Text("选择设备刷入")
        }

        //-----------
        Text("其他功能?", modifier = style.padding.bottom)
        OutlinedButton(onClick = {
            onBootBtnClick()
        }) {
            Text("启动镜像")
        }
    }
}
interface LauncherViewModel:ViewModel {
    var partition: String
    var isDropDown:Boolean
    var error:String
    var hasNext:Boolean
    val checkbox:CheckboxesViewModel
    @Composable
    fun checkInput()
    fun onNextStepBtnClick()
    fun onBootBtnClick()
}

class CheckboxesViewModel {
    val _a: MutableState<Boolean> = mutableStateOf(false)
    val _b: MutableState<Boolean> = mutableStateOf(false)
    val disableAVB: MutableState<Boolean> = mutableStateOf(false)
}
@Composable
fun checkboxes(
    viewModel: CheckboxesViewModel
) = Row {
    var _a by remember { viewModel._a }
    var _b by remember { viewModel._b }
    var _d by remember { viewModel.disableAVB }
    ChipCheckBox(_a,"_a", modifier = style.padding.end,onCheck = {
        _a = !it
    })
    ChipCheckBox(_b,"_b", modifier = style.padding.end,onCheck = {
        _b = !it
    })
    ChipCheckBox(_d,"disable avb", modifier = style.padding.end) {
        _d = !it
    }
}