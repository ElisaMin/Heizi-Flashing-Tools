package me.heizi.flashing_tool.fastboot.view
//
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import me.heizi.kotlinx.shell.CommandResult.Companion.waitForResult
//import me.heizi.kotlinx.shell.Shell
//import java.nio.charset.Charset
//
//
//
////object FastbootDialog:ComposeContext,LifecycleOwner {
////    @Composable override fun content() {
////        TODO("Not yet implemented")
////    }
////
////    override val scope: CoroutineScope
////        get() = TODO("Not yet implemented")
////
////    override fun onCreate() {
////        TODO("Not yet implemented")
////    }
////
////}
//
//class FastbootDialogViewModel(
//    val command: String,val serialID: String,var onDone:()->Unit = {}
//) {
//    var isRunning:Boolean? by mutableStateOf(false)
//    var log by mutableStateOf("正在执行中~!\n")
//
//    suspend operator fun invoke() {
//            isRunning = true
//
//            val command = String(command.toByteArray(Charsets.UTF_8), Charset.forName("GBK"))
//            Shell("fastboot -s $serialID $command").waitForResult(
//                onMessage = {
//                    log+="$it\n"
//                },onError = {
//                    log+="$it\n"
//                },onResult = {
//                    isRunning = null
//                    log += "\n\n"+when (it) {
//                        is me.heizi.kotlinx.shell.CommandResult.Failed -> {
//                            "指令执行似乎失败了"
//                        }
//                        is me.heizi.kotlinx.shell.CommandResult.Success ->{
//                            "指令似乎执行成功了"
//                        }
//                    }
//                    onDone()
//                }
//            )
//        }
////    }
////    companion object {
////        private val scope = CoroutineScope(EmptyCoroutineContext)
////    }
//}
