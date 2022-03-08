package me.heizi.flashing_tool.image.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import com.arkivanov.decompose.ComponentContext
import me.heizi.flashing_tool.image.Component
import me.heizi.flashing_tool.image.ViewModel
import me.heizi.flashing_tool.image.style
import me.heizi.kotlinx.compose.desktop.core.components.ChipCheckBox


/**
 * 首页，展示分区输入框、继续刷入、启动镜像按钮。
 *
 * @property launchFlash
 * @property launchBoot
 * @param context
 * @param fileName
 */
class LauncherComponent(
    context:ComponentContext,
    fileName:String,
    private val launchFlash:(partitions:Array<String>,disableAVB:Boolean)->Unit,
    private val launchBoot:()->Unit,
) :ComponentContext by context,Component<LauncherViewModel> {
    override val title: String = fileName
    override val subtitle: String = "你想要刷入哪个分区里面?"

    override val viewModel:LauncherViewModel = object : AbstractLauncherViewModel() {
        override fun onNextStepBtnClick()
                = launchFlash(
            buildList {
                if (checkBox[0]) add("${inputPartition}_a")
                if (checkBox[1]) add("${inputPartition}_b")
                if (!checkBox[0]&&!checkBox[1]) add(inputPartition)
            }.toTypedArray(),checkBox[2]
        )
        override fun onBootBtnClick()
                = launchBoot()
    }

    @Composable
    override fun render() {
        rememberViewModel().LauncherScreen()
    }

}

@Composable
fun LauncherViewModel.LauncherScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth()) {
            TextField(
                inputPartition, ::onPartitionInput, Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (!isDropDown) IconButton(
                        content = { Icon(Icons.Default.ArrowDropDown, "展开") },
                        onClick = ::onDropDownBtnClicked,
                    )
                },
                isError = error.isNotEmpty(), label = { Text("分区名称") },
            )
            DropdownMenu(
                isDropDown,
                ::onDropDownDismiss,
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
            ) {
                for (s in arrayOf("system", "boot", "vbmeta", "vendor", "recovery")) {
                    DropdownMenuItem(
                        content = { Text(s) },
                        onClick = { onDropDownDismiss();onPartitionInput(s) })
                }
            }
        }
        if (error.isNotEmpty()) Text(error)
        Box(style.padding.bottom)
        Row {
            for ((index, item) in arrayOf("_a", "_b", "disable avb").withIndex()) {
                ChipCheckBox(checkBox[index], item, style.padding.end) {
                    onCheckBoxClicked(index, it)
                }
            }
        }
        Button(
            content = { Text("选择设备刷入") },
            onClick = ::onNextStepBtnClick,
            modifier = style.padding.vertical.align(Alignment.End),
            enabled = hasNext
        )
        Text("其他功能?", modifier = style.padding.bottom)
        OutlinedButton(
            content = { Text("启动镜像") },
            onClick = ::onBootBtnClick
        )
    }
}

interface LauncherViewModel: ViewModel {
    val inputPartition:String
    val error: String
    fun onPartitionInput(input:String)
    val isDropDown:Boolean
    fun onDropDownBtnClicked()
    fun onDropDownDismiss()
    val checkBox: Array<Boolean>
    fun onCheckBoxClicked(index:Int,oB:Boolean)
    val hasNext:Boolean
    fun onNextStepBtnClick()
    fun onBootBtnClick()
}
fun main() {
    singleWindowApplication {
        remember {
            object : AbstractLauncherViewModel() {
                override fun onNextStepBtnClick() {}
                override fun onBootBtnClick() {}
            }
        }.LauncherScreen()
    }
}

private abstract class AbstractLauncherViewModel:LauncherViewModel {
    final override var inputPartition: String by mutableStateOf("")
    final override var error: String by mutableStateOf("")
    final override var checkBox: Array<Boolean> by mutableStateOf(arrayOf(false,false,false))
    final override var hasNext: Boolean by mutableStateOf(false)
    final override var isDropDown: Boolean by mutableStateOf(false)

    private fun legalInputChecker(input: String) {
        var flag = false
        for (c in "_, \\/") {
            if (input.contains(c)) {
                error = "错误!分区不可包含‘$c’字符"
                flag = true
            }
        }
        if (!flag) error = ""
        hasNext = input.isNotEmpty() && error.isEmpty()
    }
    final override fun onPartitionInput(input: String) {
        legalInputChecker(input)
        inputPartition = input
    }
    final override fun onDropDownBtnClicked() { isDropDown = !isDropDown }
    final override fun onDropDownDismiss() { isDropDown = false }
    final override fun onCheckBoxClicked(index: Int, oB: Boolean) {
        checkBox = Array(3) { i ->
            if (index == i) !oB else checkBox[i]
        }
    }
}