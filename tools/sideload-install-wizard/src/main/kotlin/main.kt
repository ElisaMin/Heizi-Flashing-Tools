@file:JvmName("Main")
package me.heizi.flashing_tool.sideloader

import androidx.compose.material.Colors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import dev.kdrag0n.colorkt.conversion.ConversionGraph.convert
import dev.kdrag0n.colorkt.rgb.Srgb
import dev.kdrag0n.colorkt.ucs.lch.Oklch
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.takeWhile
import me.heizi.apk.parser.ktx.color
import me.heizi.compose.ext.monet.common.*
import me.heizi.flashing_tool.adb.ADB
import me.heizi.flashing_tool.adb.ADBDevice
import me.heizi.flashing_tool.sideloader.screens.invoke
import me.heizi.kotlinx.compose.desktop.core.extractDominantColor
import me.heizi.kotlinx.compose.desktop.core.sortByHSB
import me.heizi.kotlinx.logger.debug
import me.heizi.kotlinx.logger.println
import net.dongliu.apk.parser.bean.ApkIcon
import java.io.File
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.system.exitProcess
import dev.kdrag0n.monet.theme.ColorScheme as Kdrag0nMonetThemeColorScheme


val context: MutableStateFlow<Context> =
    MutableStateFlow(Context.Ready)
val isSystemDarkTheme by lazy {
    runCatching {
        systemIsDarkTheme()
    }.getOrDefault(false)
}

val initSeek: ColorScheme? by lazy {
    systemSeekColor()?.let { Kdrag0nMonetThemeColorScheme.Dynamic[it] }?.run {
        if (isSystemDarkTheme) darkM3Scheme() else lightM3Scheme()
    }
}
private var color: ColorScheme? by mutableStateOf(null)

val colors = compositionLocalOf {
    lightColorScheme()
}


fun main(args: Array<String>) {
    val frame = JFrame("正在加载中").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        val label = JLabel("正在加载中")
        add(JPanel().apply {
            add(label)
        })
        this.minimumSize = java.awt.Dimension(300, 100)
        setLocationRelativeTo(null)
        pack()
        Context.scope.launch {
            init_state
                .takeWhile { it != null }
                .collect { label.text = it }
            cancel()
        }
    }
    frame.isVisible = true

    Context.scope.launch {
        init(args)
    }
    println("官网: dl.lge.fun 或 tools.lge.fun\nQQ群: 549674080")
    requireAndStuck(args.size==1) {
        "请输入正确的文件地址"
    }
    runBlocking {
        init_state.emit("初始化Compose")
    }
    app(frame)
}


fun app(frame:JFrame) = singleWindowApplication(
    title = "",
    icon = Resources.iconASTUgly,
    exitProcessOnExit = true,
    state = WindowState(position = WindowPosition(Alignment.Center), size = DpSize(width = 420.dp, height = 580.dp))
){
    LaunchedEffect(frame) {
        frame.isVisible = false
        init_state.value = null
        frame.dispose()
    }
    val currentContext by context.collectAsState()
//        val primaryColor = MaterialTheme.colorScheme.primary

    var color = remember {
        color
    }

    "Context".println("current",currentContext::class.simpleName)
    val mode = if (isSideload) "线刷模式" else "安装模式"

    CompositionLocalProvider(colors provides (color?: lightColorScheme())) {
        MaterialTheme(
            colorScheme = colors.current,
        ) {
            when(val current = currentContext) {
                is Context.Ready -> {
                    current()
                }
                is SingleFileContext -> {
                    window.title = "AST $mode ${current.name} 选择设备"
                    current()
                }
                is Context.Done, ->  {
                    window.requestFocus()
                    window.title = "AST 执行结果: ${current.smallTitle}"
                    current()
                }
                is Context.Invoke -> {
                    window.title = "AST 执行中 $mode"
                    current()
                }
                else -> Unit
            }

        }
    }
}
val init_state: MutableStateFlow<String?> = MutableStateFlow("正在初始化中")
suspend fun init(args: Array<String>) = Context.scope.launch {
    // init adb
    launch(IO) {
        init_state.emit("正在初始化ADB")
        ADB.println("launching adb")
        ADB.devices.collect {
            "ADB".debug("devices",it)
            init_state.emit("获取到设备：${it.serial}")
        }
    }
    val colorJob =
    // init color
    launch(Default) {
        init_state.emit("正在初始化颜色")
        systemSeekColor().let {
            init_state.emit("获取到颜色：${it?.toHex()}")
        }
        color = initSeek
    }
    // init files
    launch(IO) {
        init_state.emit("正在初始化文件")
        files.value = listOf(File(args[0]))
        init_state.emit("获取到文件：${files.value.size}个")
        files.value.getOrNull(0)?.let {
            Context(it)
        }?.also { ctx ->
            if ( ctx.isApk ) {
                isSideload = false
                init_state.emit("是APK")
                // init colorscheme
                ctx.icon?.valid?.let {
                    init_state.emit("获取到主题颜色：${it.toArgb()}")
                    colorJob.cancel()
                    color = Kdrag0nMonetThemeColorScheme.Dynamic[it.toArgb().let(::Srgb)].run {
                        if (isSystemDarkTheme) darkM3Scheme() else lightM3Scheme()
                    }
                }
            } else {
                isSideload = true
                init_state.emit("是Sideload")
            }
//            isColorLoaded = true
        }?.let { context.emit(it) } ?: run {
            init_state.emit("文件解析失败")
            delay(3000)
            exitProcess(-1)
        }
    }
}


private val Color.valid:Boolean
    get() = Srgb(toArgb()).convert<Oklch>().run {
        chroma in 0.05f..1f && lightness in 0.1f..0.9f
    }
private inline val <T:Any> ApkIcon<T>.valid:Color?
    get() =  seekColor?.takeIf { it.valid }

val <T:Any> ApkIcon<T>.seekColor:Color? get() {
    return when(this) {
        is ApkIcon.Color -> {
            Srgb(color.toArgb()).convert<Oklch>().run {
                if (chroma in 0.05f..1f && lightness in 0.1f..0.9f) {
                    return color
                }
            }
            null
        }
        is ApkIcon.Raster -> runCatching { sequence {
            val img = ImageIO.read(data.inputStream())
            repeat(img.width) { x -> repeat(img.height) { y ->
                yield(img.getRGB(x,y))
            } }
        }.map {
            Color(it)
        }.extractDominantColor().sortByHSB().first() }.getOrNull()

        is ApkIcon.Adaptive -> background.seekColor ?: foreground.seekColor

        is ApkIcon.Vector -> {
            "(\"#[a-fA-F0-9]+\")".toRegex().findAll(data).run {
                val size = count()
                // less the zero rerun null
                if (size <= 0) return null
                val colors = map { it.value }.map {
                    it.trim('"').replace("#","0x").toInt(16)
                }
                when(size) {
                    // if only one color return it
                    1 -> Color(colors.first())
                    // if in 0..10 random one
                    in 0..10 -> Color(colors.shuffled().first())
                    // if more than 10 return the most dominant color
                    else -> colors.map {
                        Color(it)
                    }.extractDominantColor().sortByHSB().first()
                }
            }
        }
        is ApkIcon.Empty -> null
    }
}

/**
 * false if apk parse success in initializing progress
 */
var isSideload by mutableStateOf(false)

val files = MutableStateFlow(listOf<File>())

object Resources {
    operator fun get(name:String): URL? = this::class.java.classLoader.getResource(name)
    val iconASTUgly = ImageIO.read(this["ic_ast_ugly.png"]!!).toPainter()
}


fun requireAndStuck(b: Boolean, function: () -> String) = runBlocking {
    if (!b) {
        println(function())
        delay(3000)
        exitProcess(-1)
    }
}

operator fun List<ADBDevice>.get(serial:String) = find { it.serial == serial }

