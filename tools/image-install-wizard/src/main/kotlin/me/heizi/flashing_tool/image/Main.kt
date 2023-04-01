@file:JvmName("Main")
package me.heizi.flashing_tool.image

import androidx.compose.material.*
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.mayakapps.compose.windowstyler.WindowBackdrop
import dev.kdrag0n.colorkt.rgb.Srgb
import dev.kdrag0n.monet.theme.ColorScheme
import me.heizi.compose.ext.monet.common.Monet
import me.heizi.compose.ext.monet.common.MonetWindow
import java.io.File


fun main(args: Array<String>) {
    startApplication(checkArgsHasFile(args))
}

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

fun startApplication(file: File){
    println("官网: dl.lge.fun 或 tools.lge.fun\nQQ群: 549674080")
    singleWindowApplication(
        title = "",
        icon = style.Image.flashable.toPainter(),
        state = WindowState(
            size = DpSize(460.dp,600.dp),
            position = WindowPosition(Alignment.Center)
        )
    ) {
        val lifecycle = remember { LifecycleRegistry() }
        val context = remember { DefaultComponentContext(lifecycle)}
        val component = remember { RootComponent(file,context) }
        Monet {
            LaunchedEffect(color) {
                val styler = windowStyler
                styler.backdropType = WindowBackdrop.Tabbed
            }
            val scheme = androidx.compose.material3.MaterialTheme.colorScheme
            CompositionLocalProvider(
                LocalContentColor provides scheme.onSurface,
                androidx.compose.material3.LocalContentColor provides scheme.onSurface,
            ) {
                MaterialTheme(MaterialTheme.colors.copy(
                    isLight = !MonetWindow.isDark,
                    primary = scheme.primary,
                    primaryVariant = scheme.primaryContainer,
                    secondary = scheme.secondary,
                    secondaryVariant = scheme.secondaryContainer,
                    background = scheme.background,
                    surface = scheme.surface,
                    error = scheme.error,
                    onPrimary = scheme.onPrimary,
                    onSecondary = scheme.onSecondary,
                    onBackground = scheme.onBackground,
                    onSurface = scheme.onSurface,
                    onError = scheme.onError,
                )) {
                    component.render()
                }
            }

        }
    }
}