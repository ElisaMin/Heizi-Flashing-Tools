package me.heizi.flashing_tool.image

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.push
import com.arkivanov.decompose.router.router
import me.heizi.flashing_tool.image.screens.LauncherComponent
import me.heizi.flashing_tool.image.screens.Screens
import me.heizi.flashing_tool.image.screens.SelectorComponent
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

    private val router = router<Screens, Component<out ViewModel>>(
        initialConfiguration = Screens.Launcher(file),
        childFactory = ::createChild
    )

    private fun createChild(screens: Screens, context: ComponentContext): Component<out ViewModel> = when (screens) {
        is Screens.Launcher -> {
            LauncherComponent(
                context,
                screens.file.name,
                { partitions, disableAVB -> launchReady(partitions, disableAVB, screens.context) },
                { launchBoot(screens.context) }
            )
        }
        is Screens.DeviceChooser -> {
            SelectorComponent(
                context
            ) { launch(screens.context) }
        }

//        else -> TODO()
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
        val next = when(context) {
            is Context.Flash -> {
//                if (!context.infoChecked)
//                    context.copy(infoChecked = true)
//                else
            }
            is Context.Boot -> {

            }
            else -> error("")
        }

    }

}