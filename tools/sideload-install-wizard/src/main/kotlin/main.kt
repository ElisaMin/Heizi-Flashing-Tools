@file:JvmName("Main")
package me.heizi.flashing_tool.sideloader

import androidx.compose.animation.core.updateTransition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.takeWhile
import me.heizi.compose.ext.monet.common.*
import me.heizi.flashing_tool.adb.ADB
import me.heizi.flashing_tool.adb.ADBDevice
import me.heizi.flashing_tool.sideloader.screens.invoke
import me.heizi.kotlinx.logger.debug
import me.heizi.kotlinx.logger.println
import java.io.File
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.system.exitProcess


val context: MutableStateFlow<Context> =
    MutableStateFlow(Context.Ready)

val colors:ColorScheme
    @Composable
    get() {
    return MaterialTheme.colorScheme
}
val ColorScheme.current get() = this

var contextReady by mutableStateOf(false)

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
    runBlocking {
        init_state.emit("初始化Compose")
    }
    application {
        init_state.value = "初始化完成"
        LaunchedEffect("init the icon") {

            init_state.value = "初始化图标"
            init_state.value = "初始化图标"
            Resources.iconASTUgly
        }

        if (!contextReady) StanWindow {
            Context.Ready()
            kotlin.io.println("官网: dl.lge.fun 或 tools.lge.fun\nQQ群: 549674080")
            requireAndStuck(args.size==1) {
                "请输入正确的文件地址"
            }
            LaunchedEffect("init the icon") {
                init_state.value = "初始化图标"
                Resources.iconASTUgly
            }
            LaunchedEffect(frame) {
                init(args,frame)
                MonetWindow.isDark
                MonetWindow.systemColor
            }
            DisposableEffect(frame) {
                onDispose {
                    frame.isVisible = false
                    init_state.value = null
                    frame.dispose()
                }
            }
        } else StanWindow(true) {
            val currentContext by context.collectAsState()
            "Context".println("current",currentContext::class.simpleName)
            Monet {
                val seek = remember(currentContext) {
                    (currentContext as? SingleFileContext)?.color?.let { c->
                        "TESTING".println("color",c)
                        this.color = c
                        "TESTING".println("color","window",color)
                    }
                }
                LaunchedEffect(seek) {
                    updateColorBySystemAccent()
                }
                app(currentContext)
            }
        }
    }
}

@Composable
inline fun ApplicationScope.StanWindow(
    icon:Boolean = false,
    crossinline content: @Composable FrameWindowScope.() -> Unit
){
    Window(
        onCloseRequest = this::exitApplication,
        title = "",
        icon = if (icon) Resources.iconASTUgly else null,
        state = WindowState(position = WindowPosition(Alignment.Center), size = DpSize(width = 420.dp, height = 580.dp)),
        content = { content() }
    )
}
@Composable
fun FrameWindowScope.app(currentContext:Context) {
    val mode = if (isSideload) "线刷模式" else "安装模式"
    when(val current = currentContext) {
        is SingleFileContext -> {

//                    var theVector by remember {
//                        current.takeIf { it.isApk }?.icon?.takeIf {
//                            (it is ApkIcon.Adaptive && (it.background is ApkIcon.Vector || it.foreground is ApkIcon.Vector)) || it is ApkIcon.Vector
//                        }?.let {
//                            when(it) {
//                                is ApkIcon.Vector -> it
//                                is ApkIcon.Adaptive -> (it.background.takeIf { it is ApkIcon.Vector } ?: it.foreground) as ApkIcon.Vector
//                                else -> null
//                            }
//                        }.let(::mutableStateOf)
//                    }
//
//                    theVector?.run {
//                        data.toByteArray().inputStream().use {
//                            loadXmlImageVector(InputSource(it), LocalDensity.current)
//                        }.run {
//                            val painter = rememberVectorPainter(this)
//                            val imageBitmap = with(LocalDensity.current) { ImageBitmap(defaultHeight.roundToPx(), defaultWidth.roundToPx()) }
//                            CanvasDrawScope().apply {
//
//                                with(painter) {
//                                    draw(size)
//                                }
//
//                            }
//                            Box(Modifier.size(this.defaultWidth,this.defaultHeight)) {
//                                Canvas(Modifier.fillMaxSize().align(Alignment.Center,)) {
//
//                                    with(painter) {
//                                        draw(size)
//                                    }
//                                    drawImage(imageBitmap)
//
//                                    imageBitmap.run {
//                                        IntArray(width*height).apply {
//                                            readPixels(this,0,0,0,0,width,height)
//                                        }
//                                    }.asSequence().forEach { kotlin.io.println(it) }
//                                }
//                            }
//                            singleWindowApplication {
////                                val painter = reme
//                                Image(imageBitmap,"what")
//                            }

//                            Canvas()
//                            with(LocalDensity.current) {
//                                ImageBitmap(defaultHeight.roundToPx(),defaultWidth.roundToPx()).apply {
//                                    Canvas(this).drawImage(this, Offset.Zero,painter).run {
//                                        extractDominantColor().run {
//                                            color = Kdrag0nMonetThemeColorScheme.Dynamic[convert<Oklch>(convert<Srgb>(this))]
//                                                .run {
//                                                    if (isSystemDarkTheme) darkM3Scheme() else lightM3Scheme()
//                                                }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }

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
        else -> {
            init_state.value = "初始化中"
            Context.Ready()
        }
    }

}
val init_state: MutableStateFlow<String?> = MutableStateFlow("正在初始化中")
suspend fun init(args: Array<String>,frame: JFrame) = Context.scope.launch {
    // init adb
    val adbJob =
    launch(Default) {
        init_state.emit("正在初始化ADB")
        ADB.println("launching adb")
        ADB.devices.collect {
            "ADB".debug("devices",it)
            init_state.emit("获取到设备：${it.serial}")
        }
    }
    val colorJob =
//     init color
    launch(Default) {
        init_state.emit("正在初始化颜色")
        MonetWindow.systemColor.let {
            init_state.emit("获取到颜色：${it}")
        }
    }
    // init files
    launch(IO) {
        init_state.emit("正在初始化文件")
        files.value = listOf(File(args[0]))
        init_state.emit("获取到文件：${files.value.size}个")
        files.value.filter { it.exists() && it.isFile }.getOrNull(0)?.let {
            Context(it)
        }?.also { ctx ->
            contextReady = true
            if ( ctx.isApk ) {
                isSideload = false
                init_state.emit("是APK")
                ctx.color?.let {
                    colorJob.cancel()
                    colorJob.join()
                    init_state.emit("获取到主题颜色：${it.toArgb()}")
                }
            } else {
                isSideload = true
                init_state.emit("是Sideload")
            }
//            isColorLoaded = true
        }?.let { context.emit(it) } ?: run {
//            colorJob.cancel()
            adbJob.cancel()
            init_state.emit("文件解析失败")
            init_state.emit("文件解析失败")
            init_state.emit("文件解析失败")
            init_state.emit(null)
            delay(1000)
            frame.isVisible = false
            requireAndStuck(false) {
                "文件解析失败"
            }
        }
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

