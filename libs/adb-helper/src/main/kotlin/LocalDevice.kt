package me.heizi.flashing_tool.adb

import me.heizi.kotlinx.logger.error
import java.io.File

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

    val dataDir = File("${userHome ?: "./tmp"}/.hft/ast").apply {
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

    operator fun File.div(child: String)
            = File(this,child)

}