package me.heizi.flashing_tool.image

import java.io.File

sealed class Context(
    open val path:String ,
    open val devices: Array<String> = arrayOf()
) {
    class Ready(file: File):Context(file.absolutePath) {
        fun toBoot(devices:Array<String>) =
            Boot(path,devices)
        fun toFlash(partitions: Array<String>, devices: Array<String>) =
            Flash(partitions = partitions, devices = devices, path = path)
    }
    data class Boot(
        override val path:String = "",
        override val devices: Array<String>
    ): Context(path,devices) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Boot) return false

            if (path != other.path) return false
            if (!devices.contentEquals(other.devices)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = path.hashCode()
            result = 31 * result + devices.contentHashCode()
            return result
        }
    }

    data class Flash(
        val partitions: Array<String>,
        val disableAVB:Boolean = false,
        override val path:String = "",
        override val devices: Array<String>
    ): Context(path, devices) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Flash) return false

            if (!partitions.contentEquals(other.partitions)) return false
            if (disableAVB != other.disableAVB) return false
            if (path != other.path) return false
            if (!devices.contentEquals(other.devices)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = partitions.contentHashCode()
            result = 31 * result + disableAVB.hashCode()
            result = 31 * result + path.hashCode()
            result = 31 * result + devices.contentHashCode()
            return result
        }
    }
}