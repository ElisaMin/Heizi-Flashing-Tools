package me.heizi.flashing_tool.vd.fb.info

import me.heizi.flashing_tool.vd.fb.FastbootDevice

enum class PartitionType {
    RAW,
    EXT4
}

data class PartitionInfo(
    val name:String,
    val type: PartitionType,
    //M
    val size:Float,
    val device: FastbootDevice
)