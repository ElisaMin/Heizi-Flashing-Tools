package me.heizi.flashing_tool.image

import java.io.File

val File.imageInfos get() = buildMap {
    guess {imgInfo,(size,sizeType) ->
        this["size"] = "" + size + sizeType
        this["guess"] = Images.group(imgInfo).joinToString()
    }
    this["fPath"] = absolutePath
}

private fun File.guess(block:(
    guessResult:Images,
    sizeInfo:Pair<Double,String>
)->Unit) {

    var result = guessImage(readHead(this))
    val sizeInfo = getType(this.length().toDouble())
    if (result == Images.Failed || sizeInfo.second == "GB" || sizeInfo.first >63)
        result = guessSize(sizeInfo.second,sizeInfo.first)
    block(result,sizeInfo)
}

private val emptyChar = Char(0)
private val spacial = Char(65533)

private fun readHead(file: File) = buildString {
    file.reader().use {
        var lines = 0
        do {
            val c = Char(it.read().takeIf { it!=-1}?:break)
            when (c) {
                emptyChar,spacial -> continue
                '\n' -> {
                    lines++
                }
            }
            append(c)
        } while (lines<32)
    }
}
enum class Images {
    Boot,System,Vendor,Vbmeta,Failed,Super,Recovery,Laf,Dtbo;

    override fun toString(): String = super.toString().lowercase()
    companion object {
        fun group(image: Images) = arrayOf(
            arrayOf(Boot,Recovery,Laf,Dtbo),
            arrayOf(Super,System,Vendor),
            arrayOf(Vbmeta)
        ).find { image in it } ?: arrayOf(Failed)

    }
}

private fun guessImage(head:String):Images {
    if("androidboot" in head && head.startsWith("ANDROID!")) return Images.Boot
    if (head.startsWith("AVB")) return Images.Vbmeta
    return Images.Failed
}

///**
// * 返回ImageType和Image大小Info
// *
// * @return
// */
//private fun File.guessSize():Pair<Images,Pair<Double,String>>  {
//    val t = getType(this.length().toDouble())
//    val (size,type) = t
//    return guessSize(type, size) to t
//}
private fun guessSize(type:String,size:Double):Images {
    return when (type) {
        "KB" -> Images.Vbmeta.takeIf { size in 32.0..65.0 }
        "MB" -> when (size) {
            in 24.0..64.0 ->  Images.Boot
            in 64.0..1024.0 ->  Images.Vendor
            else -> error("out of size")
        }
        "GB" -> when (size) {
            in 0.0..3.0 -> Images.Vendor
            in 3.0..5.0 -> Images.System
            else  -> Images.Super
        }
        else -> null
    }?: Images.Failed
}
private fun getType(double: Double,times: Int = 0 ):Pair<Double,String> {
    return if (double>1024) getType(double/1024,times+1)
    else double to  when (times) {
        0 -> "B "
        1 -> "KB"
        2 -> "MB"
        else -> "GB"
    }
}