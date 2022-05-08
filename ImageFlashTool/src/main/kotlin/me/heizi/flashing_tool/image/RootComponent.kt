package me.heizi.flashing_tool.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.jetbrains.Children
import com.arkivanov.decompose.router.push
import com.arkivanov.decompose.router.router
import me.heizi.flashing_tool.image.screens.*
import java.io.File

/**
 * 总
 *
 *
 * @param file
 * @param context
 */
class RootComponent(
    file: File,
    context: ComponentContext,
) : ComponentContext by context {


    private val viewModel = object : RootVM {
        override var title: String by mutableStateOf("")
        override var subtitle: String by mutableStateOf("")
    }

    @Composable
    fun render() {
        Children(router.state) {
            viewModel.Container {
                it.instance.render()
            }
        }
    }

    private val router = router<Screens, Component<out ViewModel>>(
        initialConfiguration = Screens.Launcher(file),
        childFactory = ::createChild
    )
    private fun createChild(screens: Screens, context: ComponentContext): Component<out ViewModel> = when (screens) {
        is Screens.Launcher -> {
            LauncherComponent(context,
                screens.file.name,
                screens.info,
                { partitions, disableAVB -> launchReady(partitions, disableAVB, screens.context) },
                { launchBoot(screens.context) }
            )
        }
        is Screens.DeviceChooser -> {
            SelectorComponent(context){launchByChooser(screens.context,it)}
        }

        is Screens.Invoke -> {
            InvokeComponent(context,
                updateTitle = {updateTitle(title=it)},
                updateSubTitle = {updateTitle(subtitle =it)},
                screens.shell
            )
        }
        is Screens.InfoCheck -> {
            InfoComponent(
                onNextStep = {launchByInfo(screens.context)},
                context = context,
                fastbootContext = screens.context
            )
        }
    }.setTitle()
    private fun Component<out ViewModel>.setTitle() = apply {
        this@RootComponent.viewModel.title =title
        this@RootComponent.viewModel.subtitle =subtitle
    }
    private fun launchByChooser(context: Context,device:Array<String>) {
        when(context) {
            is Context.Boot -> context.copy(devices = device)
            is Context.Flash -> context.copy(devices = device)
            is Context.Ready -> error("launch by chooser got wrong context")
        } .let(::launch)
    }
    private fun launchByInfo(context: Context) {
        when(context) {
            is Context.Boot -> context.copy(infoChecked = true)
            is Context.Flash -> context.copy(infoChecked = true)
            is Context.Ready -> error("launch by info got wrong context")
        } .let(::launch)
    }
    private fun launchReady(partitions:Array<String>,disableAVB:Boolean,context: Context.Ready) {
        launch(context.toFlash(partitions,disableAVB))

    }
    private fun launchBoot(context: Context.Ready) {
        launch(context.toBoot())
    }
    private fun launch(context: Context) {
        if (context.devices.isEmpty()) {
            router.push(Screens.DeviceChooser(context))
            return
        }
        if (!context.infoChecked) {
            router.push(Screens.InfoCheck(context))
            return
        }
        router.push(Screens.Invoke(context))

    }
    private fun updateTitle(title:String? = null,subtitle:String? = null) {
        title?.let { viewModel.title = it }
        subtitle?.let { viewModel.subtitle = it }
    }

    interface RootVM:ViewModel {
        val title:String
        val subtitle:String
    }
}
