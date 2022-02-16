package me.heizi.flashing_tool.vd.fb.info

import me.heizi.flashing_tool.vd.fb.FastbootDevice

sealed class PartitionType {
    object RAW : PartitionType()
    object EXT4 : PartitionType()
    object F2FS : PartitionType()
    object Typing:PartitionType(){
        override fun toString(): String
            = "所输入的分区"
    }

    override fun toString(): String = this::class.java.simpleName

//    companion object {
//        fun values(): Array<PartitionType> {
//            return arrayOf(RAW, EXT4, Typing)
//        }
//
//        fun valueOf(value: String): PartitionType {
//            return when (value) {
//                "RAW" -> RAW
//                "EXT4" -> EXT4
//                "Typing" -> Typing
//                else -> throw IllegalArgumentException("No object me.heizi.flashing_tool.vd.fb.info.PartitionType.$value")
//            }
//        }
//    }
}

data class PartitionInfo (
    val name:String,
    val type: PartitionType,
    val size:Float,
    val device: FastbootDevice,
    val isLogic:Boolean? = null
)