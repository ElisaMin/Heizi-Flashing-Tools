package me.heizi.flashing_tool.sideloader

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toPainter
import me.heizi.kotlinx.logger.error
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

/**
 * false if apk parse success in initializing progress
 */
var isSideload by mutableStateOf(false)


object Resources {
    operator fun get(name:String): URL? = this::class.java.classLoader.getResource(name)

    val iconASTUgly = ImageIO.read(this["ic_ast_ugly.png"]!!).toPainter()
}

/**
 * save device serial id as file name
 *
 * it will create `~/.hft/ast/` path and create a file `~/.hft/ast/.host,port` while you put
 * host:port serial using [plusAssign]
 *
 *
 * use `foreach` for scan the saved device
 */
object LocalDevice:Iterable<String> {

    val dataDir = File("${userHome?:"./tmp" }/.hft/ast").apply {
        if (!exists()) require(mkdirs()) {
            FileAlreadyExistsException(
                this, reason = "we cant create dir $absolutePath"
            )
        }
        require(isDirectory) {
            FileAlreadyExistsException(
                this, reason = "its not dir $absolutePath"
            )
        }
    }

    private fun String.path()
        = "."+this.replace(':',',')

    operator fun plusAssign(serial:String) {
        (dataDir/serial.path()).runCatching {
            if (!exists()) require(createNewFile())
        }.onFailure { this@LocalDevice.error(it) }
    }
    operator fun minusAssign(serial: String) {
        (dataDir/serial.path()).runCatching {
            if (exists()) delete()
        }.onFailure { this@LocalDevice.error(it) }
    }

    override fun iterator(): Iterator<String> =
        (dataDir.list()?: emptyArray()).map {
            it.drop(1).replace(',',':')
        }.iterator()

    private val userHome get() = System.getProperty("user.home")
            ?.takeIf { it.isNotBlank() || File(it).exists() }

}
operator fun File.div(child: String)
    = File(this,child)