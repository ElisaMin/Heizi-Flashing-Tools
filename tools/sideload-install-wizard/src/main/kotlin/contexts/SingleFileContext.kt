package me.heizi.flashing_tool.sideloader.contexts

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import dev.kdrag0n.colorkt.conversion.ConversionGraph.convert
import dev.kdrag0n.colorkt.rgb.Srgb
import dev.kdrag0n.colorkt.ucs.lch.Oklch
import me.heizi.apk.parser.ktx.ParseVectorIconException
import me.heizi.apk.parser.ktx.color
import me.heizi.apk.parser.ktx.toImageVector
import me.heizi.flashing_tool.sideloader.isSideload
import me.heizi.kotlinx.compose.desktop.core.extractDominantColor
import me.heizi.kotlinx.compose.desktop.core.sortByHSB
import me.heizi.kotlinx.logger.println
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.IconResource
import net.dongliu.apk.parser.bean.IconTypes
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO


//region Install

class Install (
    file: File
): SingleFileContext(file) {

    private val apk = runCatching {
        ApkFile(file).also {
            it.apkMeta
            it.icons
        }
    }.onFailure {
        println("apk check","is not apk","${it::class.simpleName}: ${it.message}")
    }.getOrNull()

    private val meta by lazy {
        apk?.runCatching { apkMeta }?.getOrNull()
    }
    override val isApk = meta != null

    override val files: List<File> get() = super.files.map { file->
        object: File(file.absolutePath), Info {
            override val isReplaceExisting: Boolean = false
            override val isTestAllow: Boolean = false
            override val isDebugAllow: Boolean = false
            override val isGrantAllPms: Boolean = false
            override val isInstant: Boolean = false
            override val abi: String?=null
        }
    }

    override val name: String get() = meta?.label?: super.name
    override val packageName: String?
        get() = apk?.apkMeta?.packageName
    override val version: String?
        get() = meta?.versionName
    override val details: Map<String, Array<String>>
        get() = buildMap {
            putAll(super.details)
            val meta = meta ?: return@buildMap

            this["权限"] = meta.permissions.map { "${it.group}:${it.name}" }.toTypedArray()
            this["SDK"] = arrayOf("min:${meta.minSdkVersion}","compile:${meta.compileSdkVersion}")
            if (meta.isDebuggable)
                this["isDebuggable"] = arrayOf("是")
            meta.usesPermissions.filterNotNull().takeIf { it.isNotEmpty() }?.let {
                this["使用权限"] = it.toTypedArray()
            }
        }

    override fun toApkOrSideload(): SingleFileContext
            = Sideload(this.file)

    private val icons get() = apk?.runCatching { icons.takeIf { it.isNotEmpty() } }?.getOrNull()

    override var maintainIcon: IconResource? = null
        private set

    private inline fun IconResource.onTestVectorFailed(crossinline onParseFailed:()->Unit = {}): Boolean? =
        (this as? IconTypes.Vector)?.run {
        try {
            this.toImageVector(Density(1f,1f))
            true
        } catch (e:ParseVectorIconException) {
            iconError = "vector: ${e.message}"
            onParseFailed()
            false
        }
    }
    //        catch (e:Exception) {
    //            throw e
    //        }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun updateMaintainIcon() {
        val icons = icons?.toMutableList()
        if (icons==null) {
            iconError = "404"
            return
        }
        icons.filterIsInstance<IconTypes.Adaptive>().takeIf { it.isNotEmpty() } ?.get(0)?.let { maintainIcon ->

            maintainIcon.data.foreground.onTestVectorFailed {
                icons.remove(maintainIcon)
            } ?: run {
                this.maintainIcon = maintainIcon.data.foreground
            }
        }
        if (icons.isEmpty()&&maintainIcon==null) {
            iconError = "404"
            return
        }
        if (maintainIcon == null) {
            maintainIcon = icons.find { it.density == 0 }
            maintainIcon?.onTestVectorFailed {
                icons.remove(maintainIcon)
                maintainIcon = icons.maxBy { it.density }
                maintainIcon?.onTestVectorFailed {
                    icons.remove(maintainIcon)
                    icons.filterIsInstance<IconTypes.Raster>().takeIf { it.isNotEmpty() } ?.run {
                        maintainIcon = maxBy { it.density }
                    }
                }
            }
        }
        // preload time
        if (maintainIcon != null) {
            (
                (maintainIcon as? IconTypes.Raster) ?: this.icons
                    ?.filterIsInstance<IconTypes.Raster>()
                    ?.takeIf { it.isNotEmpty() }
                    ?.maxBy { it.density }
            )
                ?.let(::getBufferedImage)
        } else {
            iconError = "404"
            return
        }

    }

    override val color: Color? by lazy {
        validatedSeekColor()
    }
    @Suppress("NOTHING_TO_INLINE")
    private inline fun getColorIcon(): IconResource? {
        var colorIcon:IconResource? = null
        when (val icon = maintainIcon) {
            is IconTypes.Adaptive -> {
                if(icon.data.background is IconTypes.Color || icon.data.background is IconTypes.Raster ) {
                    colorIcon = icon.data.background
                }
            }
            is IconTypes.Raster, is IconTypes.Color -> {
                colorIcon = icon
            }
            else -> Unit
        }
        if (colorIcon==null) {
            colorIcon = icons?.maxBy { it.density }
            require(colorIcon is IconTypes.Raster || colorIcon is IconTypes.Color) {
                "this icon cant be used to get color"
            }
        }
        return colorIcon

    }
    @Suppress("NOTHING_TO_INLINE")
    private inline fun validatedSeekColor():Color? =
        runCatching {
            getColorIcon()?.generateSeekColor()
        }.onFailure {
            //fixme reportable exception
            name.println("icon color","error","${it.message}")
        }.getOrNull()

    var iconError:String? = null

    /**
     * load lazy data before showing ui
     */
    suspend fun onPreLoad(): Color? {
        apk
        meta
        updateMaintainIcon()
        return color

    }


    interface Info {
        @Text("替换")
        val isReplaceExisting:Boolean
        @Text("测试")
        val isTestAllow:Boolean
        @Text("Debug")
        val isDebugAllow:Boolean
        @Text("权限通行")
        val isGrantAllPms:Boolean
        @Text("临时")
        val isInstant:Boolean
        @Text("ABI")
        val abi:String?
        private annotation class Text(val text:String)
    }
    companion object {
        @JvmStatic
        private val loadedIcons = mutableMapOf<String,BufferedImage>()
        operator fun invoke(context: Sideload)
                = Install(context.file)
    }
    fun getBufferedImage(icon: IconTypes.Raster):BufferedImage? {
        val key = meta!!.packageName+icon.path
        return runCatching {
            loadedIcons.getOrPut(key) {
                ImageIO.read(icon.data.inputStream())
            }
        }.onFailure {
            //fixme reportable exception
            name.println("read image","error","${it.message}")
            throw it
        }.getOrNull()

    }
}

private val Color.lookingGood:Boolean
    get() = Srgb(toArgb()).convert<Oklch>().run {
        chroma in 0.05f..1f && lightness in 0.1f..0.9f
    }

/**
 * return a color for generating dynamic material theme
 */
context(Install)
private fun IconResource.generateSeekColor() :Color? = when(this) {

    is IconTypes.Empty -> {
        throw IllegalArgumentException("empty icon shouldn't be max density of icons")
    }

    is IconTypes.Vector -> {
        //shouldn't support
        throw NotImplementedError("not support vector")
    }

    is IconTypes.Adaptive -> {
        data.background.generateSeekColor() ?: data.foreground.generateSeekColor()
    }

    is IconTypes.Color -> {
        color
    }

    is IconTypes.Raster -> {
        getBufferedImage(this)?.let {
            sequence {
                repeat(it.width) { x ->
                    repeat(it.height) { y ->
                        yield(it.getRGB(x, y))
                    }
                }
            }.map {
                Color(it)
            }.extractDominantColor()
                .filter { it.lookingGood }
                .sortByHSB()
                .firstOrNull()
        }
    }
}?.takeIf {it.lookingGood }

/**
 * some xml might not be valid to display on desktop
 * like attribute `android:fillType=1` is not supported
 * this function will replace it with `android:fillType="evenOdd"`
 */
//private fun IconResource.lint():IconResource  {
//    if (this is IconTypes.Adaptive) {
//        val f = this.data.foreground
//        if (f is IconTypes.Vector) {
//            f.toImageVector(Density(1f,1f))
//        }
//        return f
//    }
//    if (this is IconTypes.Vector) {
//        runCatching {
//            data.reader().use {
//                loadXmlImageVector(InputSource(it), Density(1f,1f))
//            }
//        }.onFailure {
//            if (it is ParseVectorIconException) {
//
//            } else {
//                error("icon","error","${it::class.simpleName}: ${it.message}")
//            }
//        }
//
//        data.toByteArray().inputStream().use {
//            loadXmlImageVector(InputSource(it))
//        }
//        loadXmlImageVector()
//        println("lints","icon")
//        this.copy(data = data.replace("""android:fillType="(?!=(evenOdd|nonZero))"""".toRegex(), """android:fillType="evenOdd""""))
//        //replace by regex
//        data.replace(Regex("""android:([a-zA-Z_-])="(.+)"""")) {
//            "icons".println(it.groupValues)
//            require(false)
//            throw IllegalArgumentException("not supported")
//        }

//        sequence {
//            while(true) {
//                val end = buffer.indexOf('>')
//                if (end == -1) break
//                yield(buffer.substring(0,end+1))
//                buffer = buffer.substring(end+1)
//            }
//        }.map {
//            var s = it
//            //yodo
//
//            s
//        }

//
//        return this
//    } else {
//        return this
//    }
//
//    if (this is IconTypes.Adaptive) {
//        val foreground = foreground.lint()
//
//
//        if (this.foreground is IconTypes.Vector) {
//            try {
//                this.foreground.
//                loadXmlImageVector(this.foreground.data.inputStream(),1f)
//            }
//        }
//    }
//    if (this is IconTypes.Empty) return this
//}

//endregion


/**
 * Single file context
 * @see Install
 * @see Sideload
 */
@Suppress("LeakingThis")
sealed class SingleFileContext (
    open val file: File
): Context {
    init {
        isSideload = when(this) {
            is Install -> false
            is Sideload -> true
        }
    }
    override val files: List<File> = listOf(file)
    open val name get() =  file.fileName
    open val packageName:String?=null
    open val version:String?=null
    open val maintainIcon: IconResource?=null

    abstract val color : Color?

    open val details = mapOf(
        "路径" to arrayOf(file.absolutePath),
        "大小" to arrayOf(file.size),
    )
    open val isApk = this is Install
    abstract fun toApkOrSideload(): SingleFileContext
}

class Sideload constructor(file: File): SingleFileContext(file) {
    override val isApk: Boolean get() = false
    override fun toApkOrSideload(): SingleFileContext
            = Install(this.file)
    override val color = null
}
