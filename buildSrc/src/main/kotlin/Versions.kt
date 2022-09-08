package me.heizi.gradle



object Versions {
    const val jna = "5.9.0"
    const val HFT = "0.0.8"
    const val compose = "1.2.0-alpha01-dev774"
    const val decompose = "1.0.0-alpha-04"
    const val kotlin = "1.7.10"
    const val slf4j = "2.0.0"
}
object Libs {
    const val M3 = "org.jetbrains.compose.material3:material3:${Versions.compose}"
    const val Coroutine = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1"
    const val DecomposeX = "com.arkivanov.decompose:extensions-compose-jetbrains:${Versions.decompose}"
    const val Decompose = "com.arkivanov.decompose:decompose-jvm:${Versions.decompose}"
    object SLF4J {
        const val Api = "org.slf4j:slf4j-api:${Versions.slf4j}"
        const val J12 = "org.slf4j:slf4j-log4j12:${Versions.slf4j}"
    }
    object JNA {
        const val self = "net.java.dev.jna:jna:${Versions.jna}"
        const val platform = "net.java.dev.jna:jna-platform:${Versions.jna}"
    }
}