package me.heizi.flashing_tool.sideloader

import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkIcon
import java.io.File


class Install constructor(
    file: File
): SingleFileContext(file) {
    override val file: File = object: File(file.toURI()), Info {
        override val isReplaceExisting: Boolean = false
        override val isTestAllow: Boolean = false
        override val isDebugAllow: Boolean = false
        override val isGrantAllPms: Boolean = false
        override val isInstant: Boolean = false
        override val abi: String?=null
    }

    val apk = runCatching {
        ApkFile(file)
    }.getOrNull()
    val meta get() = apk?.apkMeta
    override val name: String
        get() = meta?.label?: super.name
    override val icon: ApkIcon<*>?
        get() = apk?.icons?.takeIf { it.isNotEmpty() }?.let {
            it.find { it is ApkIcon.Adaptive }
                ?:it.first()
        }
    override val packageName: String?
        get() = apk?.apkMeta?.packageName
    override val version: String?
        get() = meta?.versionName
    override val details: Map<String, Array<String>>
        get() = buildMap {
            putAll(super.details)
            val meta = meta ?: return@buildMap

            meta.usesPermissions.filterNotNull().takeIf { it.isNotEmpty() }?.let {
                this["使用权限"] = it.toTypedArray()
            }
            this["权限"] = meta.permissions.map { "${it.group}:${it.name}" }.toTypedArray()
            this["SDK"] = arrayOf("min:${meta.minSdkVersion}","compile:${meta.compileSdkVersion}")
            if (meta.isDebuggable)
                this["isDebuggable"] = arrayOf("是")
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
    override val files: List<File> = listOf(file)
    open val name get() =  file.fileName
    open val packageName:String?=null
    open val version:String?=null
    open val icon: ApkIcon<*>?=null
    open val details = mapOf(
        "路径" to arrayOf(file.absolutePath),
        "大小" to arrayOf(file.size),
    )
    abstract fun toApkOrSideload(): SingleFileContext

}
class Sideload constructor(file: File): SingleFileContext(file) {

    override fun toApkOrSideload(): SingleFileContext
            = Install(this.file)

}