package me.heizi.flashing_tool.image.screens

import com.arkivanov.essenty.parcelable.Parcelable
import me.heizi.flashing_tool.image.Context
import me.heizi.flashing_tool.image.Fastboot
import java.io.File

sealed class Screens: Parcelable {
    abstract val context: Context
    val title = ""
    val subtitle = ""
    class Launcher(val file: File) : Screens() {
        override val context: Context.Ready = Context.Ready(file)
    }
    class DeviceChooser(override val context: Context): Screens()
    class InfoCheck(override val context: Context): Screens()
    class Invoke(override val context: Context) :Screens() {
        val shell by lazy {
            Fastboot.withContext(context)
        }
    }
}