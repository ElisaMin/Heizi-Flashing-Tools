package me.heizi.kotlinx.filedialog

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import com.sun.jna.WString
import java.io.File
import java.nio.charset.Charset
import java.util.*


fun main() {

    FileDialogs.choose()
//    debug(FileDialogs.save())
//    debug(FileDialogs.choose(multipleSelection = true))
//
//    debug(System.getProperty("os.name")
////        .startsWith("Windows")
//    )
//    debug(System.getProperty("os.arch"))

}

@Suppress("NAME_SHADOWING")
object FileDialogs {
    operator fun get(name:String):String? = System.getProperty(name)
    private val nfd:NFD by lazy {
        val libraryName= "NativeFileDialog"+ this["os.name"]!!.lowercase(Locale.getDefault()).let {
            when {
                it.startsWith("windows") -> {
                    val arch = this["os.arch"]!!
                    when {
                        arch.contains("64") -> "_x64.dll"
                        arch.contains("86") -> "_x86.dll"
                        else -> throw IllegalStateException("非原生Dialog支持系统")
                    }
                }
                else -> throw IllegalCallerException("非原生Dialog支持系统")
            }
        }

        val tempDir = File((this["java.io.tmpdir"] ?: throw IllegalArgumentException("BAD TEMP DIR !!!"))+"/nativeFileDialog/").let {
            if (!it.exists()) require(it.mkdir()) {
                    FileAlreadyExistsException(it)
            }
            val lib = File(it.canonicalPath+"\\"+libraryName)
            if (!lib.exists())
            Thread.currentThread().contextClassLoader.getResourceAsStream(libraryName)!!.use { stream ->
                lib.run {
                    deleteOnExit()
                    createNewFile()
                    setExecutable(true)
                    setWritable(true)
                    require(lib.exists()) {
                        NoSuchFileException(lib)
                    }
                }
                stream.copyTo(lib.outputStream())
            }
//            debug("file exist:${lib.exists()}")
            it.canonicalPath
        }
//        debug(tempDir)
        NativeLibrary.addSearchPath(libraryName,tempDir)

//        debug("native:added search")
//        Native.load(libraryName)
//        debug("native:registered")
        Native.load(libraryName,NFD::class.java)
    }


    /**
     * Save - Open a dialog for save file
     *
     * @param title
     * @param filter filter name to filter
     * @param initial dir to filename
     */
    fun save(
        title: String = "保存文件",
        filter: Pair<String,List<String>> = "所有文件" to listOf("*") ,
        initial: Pair<String?,String?>?=null ,
    ) = teardownFilter(filter).let { (filterName,filter) ->
        nfd.showSaveDialog(
            title = title.toUTF16(),
            filterName = filterName,
            filter = filter,
            initialDir = initial?.first?.toUTF16(),
            initialName = initial?.second?.toUTF16()
        )
    }

    /**
     * Choose - Open a dialog to choose file or files
     *
     * @param title
     * @param multipleSelection
     * @param initialDir
     * @param accept acceptFiles to acceptFolders
     * @param filter name to filters e.g. jpg,png
     * @return
     */
    fun choose (
        title: String = "选择文件",
        multipleSelection: Boolean = false,
        initialDir: String? = null,
        accept:Pair<Boolean,Boolean> = true to false,
        filter: Pair<String,List<String>>?=null ,
    ):Result<*> {
        val (filterName,filter) = teardownFilter(filter)
        val (acceptFiles,acceptFolders) = accept
        return nfd.showOpenDialog(
            title = title.toUTF16(),
            acceptFiles = acceptFiles,
            acceptFolders = acceptFolders,
            multipleSelection = multipleSelection,
            filter = filter,
            filterName = filterName,
            initialDir = initialDir?.toUTF16(),
        ).let { Result(it,multipleSelection) }
    }
    // filter name to filter
    private fun teardownFilter(filter: Pair<String, List<String>>?):Pair<ByteArray?,ByteArray?> {
        require(filter?.second?.isNotEmpty()?:true) {
            "list cant be empty when filter is selected"
        }
        return filter?.first?.toUTF16() to filter?.second?.joinToString(";")?.toUTF16()
    }

    /**
     * 密封Dialog结果
     */
    sealed class Result<T:Any>(open val result:T) {
        /**
         * 用户取消或关闭窗口时
         */
        object Cancel : Result<Any>(Any()) { override fun toString(): String = this::class.simpleName!! }

        /**
         * 返回单个文件
         */
        data class Single(override val result:File) : Result<File>(result)

        /**
         * 返回多个文件
         */
        data class Multiple(override val result:List<File>) : Result<List<File>>(result)

        companion object {
            /**
             * WString to Result
             */
            internal operator fun invoke(result:WString?,multiple:Boolean=false): Result<*> = when(val r = result?.toString()) {
                "[null]",null,"null" -> Cancel
                else -> {
                    if (multiple) r.substring(0, r.length ).split(", ",";").let {
                        if (it.size<=1)
                            Single(File(r))
                        else Multiple(it.drop(1).map { s -> File(it[0]+s) })
                    } else Single(File(r))
                }
            }
        }
    }


    private fun String.toUTF16():ByteArray = toByteArray(Charset.forName("UTF-16LE"))
        .let {
            it.copyOf(it.size + 2)
        }.also { bytes->
            bytes[bytes.size - 2] = '\u0000'.code.toByte()
            bytes[bytes.size - 1] = '\u0000'.code.toByte()
        }



    private interface NFD:Library {

        fun showOpenDialog(
            title: ByteArray?, acceptFiles: Boolean, acceptFolders: Boolean, multipleSelection: Boolean,
            filterName: ByteArray?, filter: ByteArray?, initialDir: ByteArray?
        ): WString?

        fun showSaveDialog(
            title: ByteArray?,
            filterName: ByteArray?,
            filter: ByteArray?,
            initialDir: ByteArray?,
            initialName: ByteArray?
        ): WString?
    }

}

