package me.heizi.flashing_tool.image.fragment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import lib.ChipCheckBox
import lib.Style


class Launcher:LauncherViewModel,CheckboxesViewModel,Fragment<LauncherViewModel>(_content = @Composable {
    launcherScreen(viewModel)
}) {
    override val partition: MutableState<String> = mutableStateOf("")
    override val error: MutableState<String> = mutableStateOf("")
    override val _a: MutableState<Boolean> = mutableStateOf(false)
    override val _b: MutableState<Boolean> = mutableStateOf(false)
    override val disableAVB: MutableState<Boolean> = mutableStateOf(false)
    override fun onNextStepBtnClick() {
        TODO("Not yet implemented")
    }

    override fun onBootBtnClick() {
        TODO("Not yet implemented")
    }

    override val checkbox: CheckboxesViewModel = this
    override val viewModel: LauncherViewModel = this
}

@Composable
fun launcherScreen(viewModel: LauncherViewModel){
    var input by remember { viewModel.partition }
    val errorText by remember { viewModel.error }
    val hasNext = errorText.isEmpty()
    Column {
        TextField(
            input,
            modifier = Modifier.fillMaxWidth(),
            isError = errorText.isNotEmpty(),
            onValueChange = { input = it },
            label = { Text("分区名称") },
        )

        if (!hasNext) Text(errorText)
        Box(Style.Padding.bottom)
        //=====
        checkboxes(viewModel.checkbox)
        //=====
        Button(
            onClick = { if (hasNext) viewModel.onNextStepBtnClick() },
            modifier = Style.Padding.vertical.align(Alignment.End),
            enabled = errorText.isEmpty()&&input.isNotEmpty()
        ) {
            Text("下一步")
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
    val partition: MutableState<String>
    val error: State<String>
    val checkbox:CheckboxesViewModel
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