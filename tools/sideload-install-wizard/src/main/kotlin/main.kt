@file:JvmName("Main")
package me.heizi.flashing_tool.sideloader

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.mayakapps.compose.windowstyler.WindowBackdrop
import com.mayakapps.compose.windowstyler.WindowFrameStyle
import com.mayakapps.compose.windowstyler.WindowStyle
import dev.kdrag0n.colorkt.conversion.ConversionGraph.convert
import dev.kdrag0n.colorkt.rgb.Srgb
import dev.kdrag0n.colorkt.ucs.lch.Oklch
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.takeWhile
import me.heizi.apk.parser.ktx.color
import me.heizi.compose.ext.monet.common.*
import me.heizi.flashing_tool.adb.ADBDevice
import me.heizi.flashing_tool.sideloader.screens.invoke
import me.heizi.kotlinx.compose.desktop.core.extractDominantColor
import me.heizi.kotlinx.compose.desktop.core.sortByHSB
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

var time = System.currentTimeMillis()
fun debugTiming(vararg any: Any){
    println("${
        System.currentTimeMillis().let {
            val t = it - time
            time = it
            t
        }   
    }ms ${any.joinToString(" ") { it.toString() }}")
}

object New {
    @OptIn(DelicateCoroutinesApi::class)
    @JvmStatic
    fun main(args: Array<String>):Unit = runBlocking {
        newSingleThreadContext("resources").run {
            CoroutineScope(this).launch {
                resources()
            }
        }
        debugTiming("init")
        debugTiming("init")
        debugTiming("inita","aaaa")
        debugTiming("init","bbbb")
        debugTiming("init let the light in","i love you love you love you love yo")
        debugTiming("init i need you need you need you need you","make yo back door yedl cuz i wanna commming","ohhhhh")
        app(args)
    }
}

suspend fun resources() = coroutineScope {
    debugTiming("init resources")
    Resources
    debugTiming("init resources go on")
    Resources.iconASTUglyImage
    debugTiming("read out")
    Resources.iconASTUgly
    debugTiming("read out another")

}
suspend fun app(args: Array<String>) =  coroutineScope {
    debugTiming("init app")
    launchApplication {
        debugTiming("init app launchApplication")
        var errorState by remember { mutableStateOf<Throwable?>(null) }
        if (errorState!=null) {
            debugTiming("init app launchApplication errorState")
            errorState!!.printStackTrace()
            failed(errorState!!)
        } else runCatching {
            debugTiming("init app launchApplication runCatching")
//            launch(IO) {
//                debugTiming("IO coroutine")
//                resources()
//            }
            debugTiming("readey to compose")
            reacting(args)
        }.onFailure { errorState = it }
    }
}

@Composable
fun reacting(args: Array<String>) {

    debugTiming("reacting")
    val context by context.collectAsState()
    debugTiming("context go on")
    if (true) {
        debugTiming("go on $context")
        Dialog(
            onCloseRequest = { exitProcess(0) },
            title = "正在加载中",
            icon = Resources.also {
                debugTiming("icon go on")
            }.iconASTUglys,

            ) {}
    }
}
@Composable
fun preLoad() {

}
@Composable
fun failed(cuz:Throwable) {
    // 加载失败窗口
}
//@Composable
//suspend fun

val context: MutableStateFlow<Context> =
    MutableStateFlow(Context.Ready)
val isSystemDarkTheme by lazy {
    runCatching {
//        systemIsDarkTheme()
        false
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

    Context.scope.launch(Unconfined) {
        init(args,frame)
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
        val stylerColor = colors.current.surface
        val stylerColor1 = colors.current.onSurface
        val styler = remember(stylerColor1,stylerColor) {
            WindowFrameStyle(
                borderColor = stylerColor,
                titleBarColor = stylerColor,
                captionColor = stylerColor1,
//                cornerPreference = WindowCornerPreference.ROUNDED,
            )
        }
        WindowStyle(isSystemDarkTheme,WindowBackdrop.Default,styler)
        MaterialTheme(
            colorScheme = colors.current,
        ) {
            when(val current = currentContext) {
                is Context.Ready -> {
                    current()
                }
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
                else -> Unit
            }

        }
    }
}
val init_state: MutableStateFlow<String?> = MutableStateFlow("正在初始化中")
suspend fun init(args: Array<String>,frame: JFrame) = Context.scope.launch {
    // init adb
    val adbJob =
    launch(Default) {
        init_state.emit("正在初始化ADB")
//        ADB.println("launching adb")
//        ADB.devices.collect {
//            "ADB".debug("devices",it)
//            init_state.emit("获取到设备：${it.serial}")
//        }
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
        files.value.filter { it.exists() && it.isFile }.getOrNull(0)?.let {
            Context(it)
        }?.also { ctx ->
            if ( ctx.isApk ) {
                isSideload = false
                init_state.emit("是APK")
                // init colorscheme
                // init later while icon vector or background is vector
                ctx.icon?.takeIf {when(it) {
                    is ApkIcon.Adaptive -> it.background !is ApkIcon.Vector
                    is ApkIcon.Vector -> false
                    else -> true
                } }
                    ?.valid?.let {
                    init_state.emit("获取到主题颜色：${it.toArgb()}")
                    colorJob.cancel()
                    colorJob.join()
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
            colorJob.cancel()
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


private val Color.valid:Boolean
    get() = Srgb(toArgb()).convert<Oklch>().run {
        chroma in 0.05f..1f && lightness in 0.1f..0.9f
    }
private inline val <T:Any> ApkIcon<T>.valid:Color?
    get() =  seekColor?.takeIf { it.valid }

val <T:Any> ApkIcon<T>.seekColor:Color? get() {
    return when(this) {
        is ApkIcon.Color ->
            color.takeIf { it.valid }
        is ApkIcon.Raster -> runCatching { sequence {
            val img = ImageIO.read(data.inputStream())
            repeat(img.width) { x -> repeat(img.height) { y ->
                yield(img.getRGB(x,y))
            } }
        }.map {
            Color(it)
        }.extractDominantColor()
            .filter { it.valid }
            .sortByHSB()
//            .also {
//            singleWindowApplication {
//                //64x64 block display all it color
//                Row  {
//                    it.forEachIndexed { index, color ->
//                        Box(Modifier.size(64.dp).background(color).border(1.dp,Color.Black))
//                    }
//                }
//            }
//            "Raster".debug("seekColor",it)
//            }
            .first() }.getOrNull()

        is ApkIcon.Adaptive -> background.seekColor ?: foreground.seekColor

        // fixme render android vector to bitmap

        is ApkIcon.Vector -> runCatching {


            return null
//            data.toByteArray().inputStream().use {
//                loadXmlImageVector(InputSource(it), Density(1f,1f))
//            }



//            "(\"#[a-fA-F0-9]+\")".toRegex().findAll(data).run {
//                val size = count()
//                // less the zero rerun null
//                if (size <= 0) return null
//                val colors = map { it.value }.map {
//
//                    it.trim('"').replace("#","0x").toInt(16)
//                }
//                when(size) {
//                    // if only one color return it
//                    1 -> Color(colors.first())
//                    // if in 0..10 random one
//                    in 0..10 -> colors.map { Color(it) }.filter { it.valid }.shuffled().first()
//                    // if more than 10 return the most dominant color
//                    else -> colors.map {
//                        Color(it)
//                    }.extractDominantColor().filter { it.valid }.sortByHSB().first()
//                }
//            }
        }.getOrNull()
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
    val iconASTUglyImage = this["ic_ast_ugly.png"]?.let {
        debugTiming("load ic_ast_ugly.png")
        val img = ImageIO.read(it)
        debugTiming("readed as awt image")
        img
    }
    val iconASTUgly = ImageIO.read(this["ic_ast_ugly.png"]!!).toPainter()

    val iconASTUglys = iconASTUglyImage!!.let {
        debugTiming("load ic_ast_ugly.png")
        val painter = it.toPainter()
        debugTiming("toPainter")
        painter
    }
}


fun requireAndStuck(b: Boolean, function: () -> String) = runBlocking {
    if (!b) {
        println(function())
        delay(3000)
        exitProcess(-1)
    }
}

operator fun List<ADBDevice>.get(serial:String) = find { it.serial == serial }

