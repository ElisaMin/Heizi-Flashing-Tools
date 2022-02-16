package me.heizi.gradle

object Versions {
    const val compose = "1.1.0-alpha04"
    const val kotlin = "1.6.10"
}
object Libs {
    val M3 = "org.jetbrains.compose.material3:material3:${Versions.compose}"
    const val Coroutine = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0"
    val Decompose = "com.arkivanov.decompose:extensions-compose-jetbrains:0.5.0"
}