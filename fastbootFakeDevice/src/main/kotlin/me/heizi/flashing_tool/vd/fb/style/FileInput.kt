package me.heizi.flashing_tool.vd.fb.style

import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.heizi.flashing_tool.vd.fb.scope
import kotlinx.coroutines.launch
import me.heizi.kotlinx.filedialog.FileDialogs

/**
 * a packaged text field for filepath
 *
 * @param path
 * @param modifier
 * @param title
 * @param hint
 * @param dialogName
 * @param onFilePathChange
 */
@Composable
fun fileInput(
    path:String,
    modifier: Modifier = Modifier,
    title: String = "文件路径",
    hint:String = "请输入路径...",
    dialogName: String ="选择文件",
    onFileDialogDismiss: () -> Unit ={},
    onFileDialogOpen: () -> Unit ={},
    onFilePathChange:(String)->Unit
) {
    TextField(path, onFilePathChange, modifier, trailingIcon = {
        TextButton(
            onClick = {
                scope.launch {
                    onFileDialogOpen()
                    when (val file = FileDialogs.choose(
                        title = dialogName,
                    )) {
                        is FileDialogs.Result.Single -> {
                            onFilePathChange(file.result.canonicalPath)
                        }
                    }
                    onFileDialogDismiss()
                }
            },
        ) {
            Text("选择文件")
        }
    }, placeholder = {
        Text(hint)
    }, label = {
        Text(title)
    })
}