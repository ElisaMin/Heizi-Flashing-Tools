package lib
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import me.heizi.kotlinx.shell.CommandResult
import me.heizi.kotlinx.shell.CommandResult.Companion.waitForResult
import me.heizi.kotlinx.shell.shell
import me.heizi.kotlinx.logger.*

object PlatformTools{

    internal val platformTool = PlatformTools
    val fastboot = Fastboot
    var  adbSource  = ".\\lib\\adb.exe"
    var fastbootSource  = ".\\lib\\fastboot.exe"
    private fun getPrefix(isAdb: Boolean) = if(isAdb) adbSource else fastbootSource

    private suspend fun doCommand(isADB: Boolean, commands: Array<String>)
        = coroutineScope { shell(*arrayOf(getPrefix(isADB),*commands)).waitForResult() }


    //        CommandExecutor.run(lists, false, getPrefix(isADB))
    private suspend fun doCommand(isADB: Boolean,string: String) =
        doCommand(isADB, arrayOf(string))

//        CommandExecutor.run(string, getPrefix(isADB), isGBK = false)


//    infix fun adb(lists: Array<ArrayList<String>>) =      doCommand(true,lists)
//    infix fun adb(string: String) =                       doCommand(true,string)

    suspend infix fun fastboot(commands: Array<String>) = doCommand(false,commands)
    suspend infix fun fastboot(command: String): CommandResult =         doCommand(false,command)

    object Fastboot{

//        val isBootloaderUnlocked:Boolean? get() {
//            val r = fastboot getvar "unlocked"
//            println(r.message!!)
//            return when{
//                r.isSuccess("yes") -> true
//                r.isSuccess("no") -> false
//                else -> null
//            }
//        }



        //flash
        private fun getArrayListForFlash(pair: Pair<String, String>, isA: Boolean?=null, isAVBRemove: Boolean = false):String = buildString {
            fun add(string: String) = append(" $string")
            if (isAVBRemove) {
                add("--disable-verity")
                add("--disable-verification")
            }
            add("flash")
            val (ptt,file) = pair
            if (isA!=null) {
                append(" ")
                append(ptt)
                append("_")
                append(if (isA) "a" else "b")
            }
            else add(ptt)
            add(file)
            println(toString())
        }

        suspend infix fun flash (pair: Pair<String,String>) = platformTool fastboot getArrayListForFlash(pair)
        suspend infix fun flash_ab(pair: Pair<String, String>) = platformTool fastboot arrayOf(
            getArrayListForFlash(pair,true),
            getArrayListForFlash(pair,false)
        )
        //flash without avb
        suspend infix fun removeAVB (pair: Pair<String,String>) = platformTool fastboot getArrayListForFlash(pair,isAVBRemove = true)
        suspend infix fun removeAVB_ab(pair: Pair<String, String>) = platformTool fastboot arrayOf(
            getArrayListForFlash(pair,true,true),
            getArrayListForFlash(pair,false,true)
        )

        //erase
        suspend infix fun erase (partition:String) = platformTool fastboot "erase $partition"

        //slot
        suspend infix fun switchSlotAB(isSlotA: Boolean) = platformTool fastboot "--set-active=${if (isSlotA) "a" else "b"}"
        suspend infix fun setSlot(slot: String) = platformTool fastboot "--set-active=$slot"

        //boot
        suspend infix fun boot(path: String) = platformTool fastboot "boot $path"


        suspend infix fun getvar (name:String) = platformTool fastboot "getvar $name"
        suspend infix fun reboot (isBootloader:Boolean) = fastboot(if (isBootloader)"reboot" else "reboot bootloader")
        suspend fun startListenDevices()  = flow<Iterable<String>> {
            do {
                var hasNext = true
                val r = platformTool fastboot "devices"
                require(r is CommandResult.Success) {
                    hasNext = false
                    this@Fastboot.error("未知错误",r)
                    IllegalStateException("未知错误")
                }
                val lines = r.message.lines()
                require(lines.isNotEmpty()) {
                    hasNext = false
                    this@Fastboot.error("未知错误",r)
                    IllegalStateException("未知错误")
                }
                emit(lines.drop(1))
                delay(1000)
            } while (hasNext)
        }
    }

//    object ADB{
//        //adb get-things
//        val state:String get() {val (b,s) = adb("get-state");return if (b) s!! else "null"}
//        val serialno:String get() {val (b,s) = adb("get-serialno");return if (b) s!! else "null"}
//
//
//        // adb root/unroot
//        infix fun root(isEnable: Boolean):CommandResult = adb(arrayListOf(if (isEnable)"root" else "unroot"))
//        //adb disable/enable verity
//        infix fun setVerity(isEnable:Boolean):CommandResult = platformTool adb if (isEnable) "enable-verity" else "disable-verity"
//        //remount
//        fun remount() : CommandResult = adb(arrayListOf("remount"))
//
//        // adb shell
//        fun shell (list: ArrayList<String>, isRoot:Boolean):CommandResult = adb (if (isRoot) arrayListOf("su","-c").apply { addAll(list) } else list)
//        infix fun shell (shellCommand:()->String):CommandResult{ return platformTool adb arrayListOf("shell",shellCommand()) }
//        // adb sideload
//        infix fun sideload(path:String):CommandResult = adb(arrayListOf("wait-for-sideload",path))
//
//        // fileManaging
//        infix fun pull (dirs: Pair<String,String?>):CommandResult = adb(arrayListOf("pull","${dirs.first}","${dirs.second}"))
//        infix fun pull (dir: String):CommandResult = adb(arrayListOf("pull","${dir}"))
//        infix fun push (dirs: Pair<String,String> ):CommandResult = adb(arrayListOf("push","${dirs.first}","${dirs.second}"))
//
//        infix fun server (status :Boolean):CommandResult = platformTool adb if (status)"start-server" else "kill-server"
//        infix fun reconnect (status:Boolean?):CommandResult= platformTool adb arrayListOf("reconnect", when (status) { null -> "";true ->"device"; false -> "offline" } )
//
//        enum class BootableMode(index:Int) {
//            Android(0),
//            Recovery(1),
//            Sideload(2),
//            Bootloader(3),
//            SideloadAutoReboot(4);
////
////            fun getString(bootable: BootableMode): String
////                = when (bootable){
////
////            }
//        }
//
//        infix fun reboot(mode: BootableMode):CommandResult =
//            if (mode == BootableMode.Android){
//                adb("reboot")
//            }
//            else {
//                adb(arrayListOf("reboot", when (mode) {
//                    BootableMode.Recovery -> "recovery"
//                    BootableMode.Sideload -> "sideload"
//                    BootableMode.Bootloader -> "bootloader"
//                    BootableMode.SideloadAutoReboot -> "sideload-auto-reboot"
//                    else -> ""
//                }))
//            }
//
//    }
}
