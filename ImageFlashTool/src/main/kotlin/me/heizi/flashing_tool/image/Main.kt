//@file:JvmName("Main")
package me.heizi.flashing_tool.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.router.Router
import com.arkivanov.decompose.router.router
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import me.heizi.flashing_tool.image.fragment.Fragment.Companion.start
import me.heizi.flashing_tool.image.screens.Screens
import java.io.File
//
//fun main() {
//
//}

fun checkArgsHasFile(args: Array<String>):File =
     if (args.isEmpty()) error("请输入文件地址")
    else args[0].let {
        if (!it.matches(".+\\.(bin|img)".toRegex()))
            println("非正常镜像")
        getFileOrEnd(it)
    }

fun getFileOrEnd(file:String) =
    File(file)
//        .takeIf { it.exists() }
//        ?: error("文件不存在")
@Composable
fun start(file: File) {
    val lifecycle = remember { LifecycleRegistry() }
    val context = remember { DefaultComponentContext(lifecycle)}
    val router:Router<Screens,Any> = context.router(
        initialConfiguration = Screens.Launcher(file),
        childFactory = { c, _ -> c}
    )
//    Children(
//        router.state
//    ) {
//    }
}


object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        start(checkArgsHasFile(args))
    }
}
interface ViewModel
interface Component<T:ViewModel> {
    val title:String
    val subtitle:String
    val viewModel:T
    @Composable
    fun rememberViewModel() = remember {
        viewModel
    }
    @Composable
    fun render()
}

//fun startApplication(
//    component: Component
//){
//    singleWindowApplication(title = "",icon = style.Image.flashable.toPainter(), state = WindowState(size = DpSize(600.dp,460.dp))) {
//        MaterialTheme {
//            DisplayComponent(component)
//        }
//    }
//}
//@Composable
//fun DisplayComponent(component: Component) = Column(Modifier.fillMaxSize().padding(16.dp)) {
//    Text(text = component.title, style = MaterialTheme.typography.titleMedium, modifier = style.padding.bottom)
//    Text(text = component.subtitle, modifier = style.padding.bottom)
//    component.render()
//}