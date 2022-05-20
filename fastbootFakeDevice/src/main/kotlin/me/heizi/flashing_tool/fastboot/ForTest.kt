package me.heizi.flashing_tool.fastboot

import me.heizi.flashing_tool.fastboot.repositories.Fastboot



fun main() {
    Fastboot.isTesting = true
    run()
}
