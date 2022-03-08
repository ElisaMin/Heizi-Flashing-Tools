package me.heizi.flashing_tool.image.screens

import com.arkivanov.essenty.parcelable.Parcelable
import me.heizi.flashing_tool.image.Context
import java.io.File

sealed class Screens: Parcelable {
    abstract val context: Context
    val title = ""
    val subtitle = ""
    class Launcher(val file: File) : Screens() {
        override val context: Context.Ready = Context.Ready(file)
    }
    class DeviceChooser(override val context: Context): Screens() {

    }
}