package me.heizi.flashing_tool.image

import me.heizi.flashing_tool.image.fragment.Fragment.Companion.start
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        error("请输入文件地址")
        exitProcess(-1)
    }
    val file = args[0]
    if (!file.matches(
       ".+\\.(bin|img)".toRegex()
    )) {
        println("非正常镜像")
    }
    start(File(file))
}