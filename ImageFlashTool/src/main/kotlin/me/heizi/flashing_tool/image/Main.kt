@file:JvmName("Main")
package me.heizi.flashing_tool.image

import me.heizi.flashing_tool.image.fragment.Fragment.Companion.start
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
    start(file)
}