package me.heizi.gradle

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.provider.Property
import java.util.*
import kotlin.reflect.KProperty



val Project.props get() = object : Getter<Properties> {
    override fun get(key: String): Properties = Properties().apply {
        file(key+".properties").inputStream().use(::load)
    }
}
val ExtensionAware.prop: Versions get() = object : Versions {
    override val extraPropertiesExtension: ExtraPropertiesExtension
        get() = extensions.extraProperties

    override fun get(key: String): String
            = extraPropertiesExtension[key] as String
}

operator fun <T> Property<T>.setValue (thisRef:Any?, prop: KProperty<*>, value :T) {
    set(value)
}

val ExtensionAware.versions
    get() = object : Versions {
        override val extraPropertiesExtension: ExtraPropertiesExtension
            get() = extensions.extraProperties
    }
//operator fun ExtraPropertiesExtension.get(string: String):String
//    = get(string+".version") as String

interface Versions:Getter<String> {
    val extraPropertiesExtension: ExtraPropertiesExtension
    override fun get(key:String):String =
        extraPropertiesExtension[key+".version"] as String
}
interface Getter<T> {
    operator fun get(key:String):T
}