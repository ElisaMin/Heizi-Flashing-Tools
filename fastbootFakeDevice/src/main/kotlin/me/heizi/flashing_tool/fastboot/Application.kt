package me.heizi.flashing_tool.fastboot

// use decompose

//
//interface ComposeContext {
//    @Composable fun content ()
//}
//
//
//interface Runnable {
//    fun blockingRunSingleCommand(string: String): Flow<ProcessingResults>
//}
//
//enum class Events {
//    Create,Start
//}
//abstract class AbstractLifecycleOwner:LifecycleOwner {
//    val events = hashMapOf<Events,()->Unit>()
//}
//interface LifecycleOwner {
//    val scope:CoroutineScope
//    fun on(event: Events)
//}
//
//interface Application: Runnable,ComposeContext,LifecycleOwner
