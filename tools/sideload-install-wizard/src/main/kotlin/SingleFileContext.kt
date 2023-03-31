package me.heizi.flashing_tool.sideloader

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dev.kdrag0n.colorkt.conversion.ConversionGraph.convert
import dev.kdrag0n.colorkt.rgb.Srgb
import dev.kdrag0n.colorkt.ucs.lch.Oklch
import me.heizi.apk.parser.ktx.color
import me.heizi.kotlinx.compose.desktop.core.extractDominantColor
import me.heizi.kotlinx.compose.desktop.core.sortByHSB
import me.heizi.kotlinx.logger.println
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkIcon
import java.io.File
import javax.imageio.ImageIO


class Install constructor(
    file: File
): SingleFileContext(file) {
    override val files: List<File> get() = super.files.map {file->
        object: File(file.absolutePath), Info {
            override val isReplaceExisting: Boolean = false
            override val isTestAllow: Boolean = false
            override val isDebugAllow: Boolean = false
            override val isGrantAllPms: Boolean = false
            override val isInstant: Boolean = false
            override val abi: String?=null
        }
    }
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
    override val name: String
        get() = meta?.label?: super.name
    override val icon: ApkIcon<*>? by lazy {
        apk?.runCatching {
            icons.takeIf { it.isNotEmpty() }?.let { icons ->
                icons.find { it is ApkIcon.Adaptive }
                    ?:icons.find{ it.density == 0 }
                    ?:icons.maxBy { it.density }
            } }?.getOrNull()
    }
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
        operator fun invoke(context: Sideload)
                = Install(context.file)
    }
}

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
    open val icon: ApkIcon<*>?=null
    open val color by lazy {
        icon?.takeIf {when(it) {
        is ApkIcon.Adaptive -> it.background !is ApkIcon.Vector
        is ApkIcon.Vector -> false
        else -> true
    } }?.valid.also {
        name.println("icon color","$it")
    } }

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
}

private val Color.valid:Boolean
    get() = Srgb(toArgb()).convert<Oklch>().run {
        chroma in 0.05f..1f && lightness in 0.1f..0.9f
    }
private inline val <T:Any> ApkIcon<T>.valid: Color?
    get() =  seekColor?.takeIf { it.valid }

val <T:Any> ApkIcon<T>.seekColor: Color? get() {
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
