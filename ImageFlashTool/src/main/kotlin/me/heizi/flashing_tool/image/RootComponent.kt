package me.heizi.flashing_tool.image

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.router
import me.heizi.flashing_tool.image.screens.LauncherComponent
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

    private val router = router<Screens, Component>(
        initialConfiguration = Screens.Launcher(file),
        childFactory = ::createChild
    )

    private fun createChild(screens: Screens, context: ComponentContext): Component = when (screens) {
        is Screens.Launcher -> {
            LauncherComponent(
                context,
                screens.file.name,
                { partitions, disableAVB -> launchReady(partitions, disableAVB, screens.context) },
                { launchBoot(screens.context) }
            )
        }
        else -> TODO()
    }
    private fun launchReady(partitions:Array<String>,disableAVB:Boolean,context: Context.Ready) {
        TODO()
    }
    private fun launchBoot(context: Context.Ready) {
        TODO()
    }
    private fun launch(context: Context) {
        if (context.devices.isEmpty()) {
            TODO("device check")
            return
        }
        when(context) {
            is Context.Flash -> TODO()
            is Context.Boot -> TODO()
            else -> error("")
        }

    }

}