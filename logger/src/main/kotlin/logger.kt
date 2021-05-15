package me.heizi.kotlinx.logger


import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

fun main() {
    LoggerFactory.getLogger("logger").println("fuck")
}


fun Any?.toString():String = when(this) {
    null -> "Nothings"
    is Iterable<*> -> this.joinToString(",")
    else -> toString()
}

fun getLogger(name: String?) =
    LoggerFactory.getLogger(name?:"unknown")?:Unknown.logger
fun getLogger(klz: KClass<out Any>) =
    LoggerFactory.getLogger(klz.java)?:Unknown.logger
object Unknown {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)
}
private val Any?.logger get() = kotlin.runCatching {
    getLogger((this?:Unknown)::class)
}.getOrDefault(Unknown.logger)


fun String.println(vararg any: Any?) {
    getLogger(this).info(any.joinToString(": "))
}
fun String.debug(vararg any: Any?) {
    getLogger(this).debug(any.joinToString(": "))
}

fun Any?.println(any: Any?) {
    logger.info(any.toString())
}
fun Any?.error(any: Any?) {
    logger.error(any.toString())
}
fun Any?.debug(any: Any?) {
    logger.debug(any.toString())
}


fun Any?.println(vararg any: Any?) = println(any.joinToString(": "))
fun Any?.error(vararg any: Any?) = error(any.joinToString(": "))
fun Any?.debug(vararg any: Any?) = debug(any.joinToString(": "))