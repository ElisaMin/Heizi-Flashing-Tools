package me.heizi.kotlinx.compose.desktop.core

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import kotlin.reflect.KProperty

/**
 * 懒得写State.value的代理
 *
 * @param T
 * @param state
 */
fun <T> value(state: State<T>) = Value(state)

class Value<T> internal constructor(val state: State<T>) {
    operator fun getValue(any: Any?, property: KProperty<*>): T
            = state.value
    operator fun setValue(any: Any?, property: KProperty<*>, value: T) {
        require(state is MutableState) {
            "$property is Immutable !!!!!!"
        }
        state.value = value
    }
}