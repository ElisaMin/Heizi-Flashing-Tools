package me.heizi.flashing_tool.sideloader



sealed interface Context {
    val filePath:String

    abstract class Sideload private constructor():Context {

    }
    abstract class Install private constructor():Context {

    }
    companion object {
        operator fun invoke(filePath:String) {

        }
    }
}