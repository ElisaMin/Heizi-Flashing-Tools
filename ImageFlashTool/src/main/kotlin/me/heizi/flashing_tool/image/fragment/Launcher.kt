package me.heizi.flashing_tool.image.fragment

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.heizi.flashing_tool.image.Style
import me.heizi.kotlinx.compose.desktop.core.components.ChipCheckBox
import me.heizi.kotlinx.logger.debug


class Launcher:LauncherViewModel,CheckboxesViewModel, Fragment<LauncherViewModel>(_content =  @Composable {
    title = file.name
    subtitle = "你想要刷入哪个分区里面?"
    viewModel.checkInput()
    launcherScreen(viewModel)
}) {


    override var partition by mutableStateOf("")

    override val error: MutableState<String> = mutableStateOf("")
    override var hasNext: Boolean by mutableStateOf(false)
    override var isDropDown: Boolean by mutableStateOf(true)

    override val _a: MutableState<Boolean> = mutableStateOf(false)
    override val _b: MutableState<Boolean> = mutableStateOf(false)
    override val disableAVB: MutableState<Boolean> = mutableStateOf(false)


    @Composable
    override fun checkInput() {
        error.value = when {
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
        hasNext = partition.isNotEmpty() && error.value.isEmpty()
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
        "_a" to _a.value,
        "_b" to _b.value,
        "disable_avb" to disableAVB.value,
    ) else arrayOf("launchMode" to "boot")).let {
        handler.go(DeviceSelector::class,*it)

    }

    override val checkbox: CheckboxesViewModel = this
    override val viewModel: LauncherViewModel = this
}
//@Composable
//fun check
@Composable
fun launcherScreen(viewModel: LauncherViewModel){
    val errorText = viewModel.error.value
    val hasNext = viewModel.hasNext


    Column {
        Column (
            modifier = Modifier.fillMaxWidth(),
        ){
            TextField(
                viewModel.partition,
                modifier = Modifier.fillMaxWidth(),
                isError = errorText.isNotEmpty(),
                onValueChange = { viewModel.partition= it },
                label = { Text("分区名称") },
                trailingIcon = {
                    val text = if (viewModel.isDropDown)
                        "▼" else "◀"
                    TextButton(onClick = { with(viewModel){isDropDown = !isDropDown}
                        "Extent".debug("clicked",viewModel.isDropDown) },) {
                        Text(text)
                    }
                }
            )

            DropdownMenu(viewModel.isDropDown,{
                viewModel.isDropDown = false
            },modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),) {
                for (s in arrayOf("system", "boot", "vbmeta", "vendor")) {
                    DropdownMenuItem(onClick = {
                        viewModel.partition = s
                        viewModel.isDropDown = false
                    }) {
                        Text(s)
                    }
                }
            }
        }

        if (!hasNext) Text(errorText)
        Box(Style.Padding.bottom)
        //=====
        checkboxes(viewModel.checkbox)
        //=====
        Button(
            onClick = { if (hasNext) viewModel.onNextStepBtnClick() },
            modifier = Style.Padding.vertical.align(Alignment.End),
            enabled = hasNext
        ) {
            Text("选择设备刷入")
        }

        //-----------
        Text("其他功能?", modifier = Style.Padding.bottom)
        OutlinedButton(onClick = {
            viewModel.onBootBtnClick()
        }) {
            Text("启动镜像")
        }
    }
}
interface LauncherViewModel:ViewModel {
    var partition: String
    val error: State<String>
    val hasNext:Boolean
    val checkbox:CheckboxesViewModel
    var isDropDown:Boolean
    @Composable
    fun checkInput()
    fun onNextStepBtnClick()
    fun onBootBtnClick()
}


interface CheckboxesViewModel {
    val _a: MutableState<Boolean>
    val _b: MutableState<Boolean>
    val disableAVB: MutableState<Boolean>
}

@Composable
fun checkboxes(
    viewModel: CheckboxesViewModel
) = Row {
    var _a by remember { viewModel._a }
    var _b by remember { viewModel._b }
    var _d by remember { viewModel.disableAVB }
    ChipCheckBox(_a,"_a", modifier = Style.Padding.end,onCheck = {
        _a = !it
    })
    ChipCheckBox(_b,"_b", modifier = Style.Padding.end,onCheck = {
        _b = !it
    })
    ChipCheckBox(_d,"disable avb", modifier = Style.Padding.end) {
        _d = !it
    }
}