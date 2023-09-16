@file:JvmName("Main")
package me.heizi.flashing_tool.sideloader

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.mayakapps.compose.windowstyler.WindowBackdrop
import com.mayakapps.compose.windowstyler.windows.WindowsWindowStyleManager
import dev.kdrag0n.colorkt.rgb.Srgb
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import me.heizi.compose.ext.monet.common.Monet
import me.heizi.compose.ext.monet.common.MonetWindow
import me.heizi.compose.ext.monet.common.getWindowFrame
import me.heizi.flashing_tool.adb.ADB
import me.heizi.flashing_tool.sideloader.contexts.Context
import me.heizi.flashing_tool.sideloader.contexts.Install
import me.heizi.flashing_tool.sideloader.contexts.SingleFileContext
import me.heizi.flashing_tool.sideloader.screens.invoke
import me.heizi.kotlinx.compose.desktop.core.setAllBackground
import me.heizi.kotlinx.logger.debug
import me.heizi.kotlinx.logger.println
import java.awt.Font
import java.awt.Window
import java.awt.font.TextAttribute
import java.io.File
import java.io.FileNotFoundException
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel


fun main(args: Array<String>) {
    println("官网: dl.lge.fun 或 tools.lge.fun\nQQ群: 549674080")
    //slash screen
    val label = JLabel("正在加载中")
    val frame = JFrame("正在加载中").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        add(JPanel().apply { add(label) })
        this.minimumSize = java.awt.Dimension(300, 100)
        setLocationRelativeTo(null)
        pack()
    }
    Context.scope.launch {
        var last = ""
        init_state
            .takeWhile { it != null }
            .collect { s ->
                runCatching{ splashScreen }.onFailure { it.printStackTrace() }
                    .getOrNull()?.runCatching {
//                    imageURL = Resources["ic_ast_ugly.png"]
//                    update()
                    createGraphics()?.apply {
                        font = java.awt.Font(mapOf(
                            TextAttribute.FAMILY to Font.MONOSPACED,
                            TextAttribute.SIZE to 10,
//                            TextAttribute.WEIGHT to TextAttribute.WEIGHT_REGULAR,
                        ))
                        paint = java.awt.Color(0xcfcfcf)
                        this.drawString(last,60,380)
                        paint = java.awt.Color.BLACK
                        last = s!!+"..."
                        this.drawString(last,60,380)
                    }
                    update()
                }
                label.text = s
                "INIT".println("state",s)
            }
        cancel()
    }
    Context.scope.launch {
        init_state.emit("初始化Compose")
        WindowsWindowStyleManager(frame).apply {
            MonetWindow.systemColor?.let {seek ->
                dev.kdrag0n.monet.theme.ColorScheme.Dynamic[Srgb(seek)]
            }?.run {
                isDarkTheme = MonetWindow.isDark
                if (isDarkTheme) materialDark() else materialLight()
            }?.run {
                (surface as Srgb).toRgb8().let {
                    frame.setAllBackground(java.awt.Color(it))
                }
                frameStyle = getWindowFrame()
            }
        }
    }
    application {
        init_state.value = "初始化Compose完成"
        var icon: Painter? by remember {
            mutableStateOf(null)
        }
        init_state.value = "正在加载图标"
        LaunchedEffect("icon") {
            icon = Resources.iconASTUgly
            init_state.value = "图标加载完成"
        }
        init_state.value = "初始化Compose完成"
        init_state.value = "正在加载Application"
        var the_window:Window? = null
        if (the_window == null&&icon==null&&!contextReady) Window(
            onCloseRequest = this::exitApplication,
            visible = !contextReady,
            title = "",
            icon = BitmapPainter(ImageBitmap(1,1,)),
            state = WindowState(position = WindowPosition(Alignment.Center), size = DpSize(width = 420.dp, height = 580.dp)),
        ) {
            LaunchedEffect(window) {
                WindowsWindowStyleManager(window).let {
                    it.backdropType = WindowBackdrop.Tabbed
                    it.isDarkTheme = MonetWindow.isDark
                }
            }
            var text by remember { mutableStateOf("正在加载中...") }
            Context.Ready(text)
            if (icon==null) {
                LaunchedEffect(icon) {
                    init_state.value = "正在加载窗口"
                }
                LaunchedEffect(frame) {
                    Context.scope.launch {
                        init(args,frame)
                    }
                }
                DisposableEffect(frame) {
                    onDispose {
                        frame.isVisible = false
                        init_state.value = null
                        frame.dispose()
                    }
                }

            } else if (!contextReady) {
                val currentContext by context.collectAsState()
                text = "正在加载文件"
                if (currentContext == null) {
                    icon = null
                    text = "文件加载失败，\n或许下一个版本\n会加入拖拽功能。"
                } else Monet {  }
            } else Monet {
                the_window = window
            }
        }
        Window(
            visible = contextReady && icon!=null ,
            onCloseRequest = {exitApplication()},
            title = "",
            icon = icon,
            state = WindowState(position = WindowPosition(Alignment.Center), size = DpSize(width = 420.dp, height = 580.dp)),
        ) {
            LaunchedEffect(window) {
                WindowsWindowStyleManager(window)
                while (the_window == null) delay(10)
                the_window!!.isVisible = false
                the_window!!.dispose()
                the_window = null
            }
            if (icon!=null) {
                ReadyToGo()
            }
        }
    }
}
@Composable
fun FrameWindowScope.ReadyToGo() = Monet {
    val currentContext by context.collectAsState()
    "Context".println("current",currentContext?.let { it::class.simpleName })
    val seek = remember(currentContext) {
        (currentContext as? SingleFileContext)?.color?.also { c->
            "TESTING".println("color",c)
            this.color = c
            "TESTING".println("color","window",color)
        }
    }
    LaunchedEffect(seek) {
        updateColorBySystemAccent()
        windowStyler
    }
    currentContext?.let {
        app(it)
    }?: kotlin.run {
        Context.Ready("错误！")
    }
}
@Composable
fun FrameWindowScope.app(currentContext: Context) {
    val mode = if (isSideload) "线刷模式" else "安装模式"
    when(val current = currentContext) {
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
        else -> {
            init_state.value = "初始化中"
            Context.Ready("错误")
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
        files.value = args.map {
            File(it)
        }.filter {
            it.exists() && it.isFile
        }
        init_state.emit("获取到文件：${files.value.size}个")
        files.value.filter { it.exists() && it.isFile }.getOrNull(0)?.let {
            Context(it)
        }?.also { ctx ->
            contextReady = true
            if ( ctx.isApk ) {
                isSideload = false
                init_state.emit("正在解析APK")
                (ctx as? Install?)?.run {
                    onPreLoad()
                }.let {
                    colorJob.cancel()
                    colorJob.join()
                    init_state.emit("获取到主题颜色：${it?.toArgb()}")
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
            init_state.emit(null)
            context.emit(null)
            frame.isVisible = false
            delay(1000)
            throw FileNotFoundException("文件解析失败")
        }
    }
}
