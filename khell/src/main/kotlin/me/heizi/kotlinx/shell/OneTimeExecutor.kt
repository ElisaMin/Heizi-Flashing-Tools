package me.heizi.kotlinx.shell

/**
 * 单次执行 执行完毕后立即作废
 */

import me.heizi.kotlinx.logger.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import me.heizi.kotlinx.shell.CommandResult.Companion.waitForResult
import java.io.IOException

interface RunScope {
    fun run(command:String)
    fun write(string: String)
}

/**
 * 用于匹配错误的Regex
 */
private val exceptionRegex by lazy {
    "Cannot run program \".+\": error=(\\d+), (.+)"
    .toRegex()
}

fun main() = runBlocking {
    println("called")

    println(shell(*arrayOf("echo heizi")).waitForResult())
    println("callledddd")
    Unit
}

fun printlns(vararg msg:Any?) = "shell".println(*msg)

/**
 *
 *
 * @param commandLines 所要丢给shell的指令
 * @param prefix 决定了以哪种形式打开这个解释器
 * @return
 */
@Suppress( "BlockingMethodInNonBlockingContext")
suspend fun CoroutineScope.shell(
    vararg commandLines:String,
    prefix:Array<String> = arrayOf("cmd","/k", "@echo off", ),
    isMixingMessage: Boolean = false,
    isWindows_keep: Boolean = true,
    dispatcher: CoroutineDispatcher = Default
): Flow<ProcessingResults> {
    val onCreateCommand = arrayOf(prefix.joinToString(" "),*commandLines)
    printlns("new command",onCreateCommand.joinToString(" && "))
    commandLines.forEach {
        printlns("commands",it)
    }
    return shell(prefix=prefix,isMixingMessage=isMixingMessage,isEcho = false,dispatcher=dispatcher) {
        if (isWindows_keep) {
            commandLines.forEach(this::run)
            runBlocking {
                delay(300)
            }
                run("@exit")
        }
    }
}
///**
// *
// *
// * @param commandLines 所要丢给shell的指令
// * @param prefix 决定了以哪种形式打开这个解释器
// * @return
// */
//@Suppress( "BlockingMethodInNonBlockingContext")
//suspend fun shell(
//    commandLines:Flow<String>,
//    prefix:Array<String> = arrayOf("cmd","/c") ,
//    isMixingMessage: Boolean = false,
//    isEcho: Boolean = false,
//    dispatcher: CoroutineDispatcher = Default
//): Flow<ProcessingResults> = shell(prefix = prefix,isMixingMessage,isEcho  = isEcho,dispatcher = dispatcher) {
//
//    commandLines.collect(::run)
//}



/**
 *
 *
 * @param commandLines 所要丢给shell的指令
 * @param prefix 决定了以哪种形式打开这个解释器
 * @return
 */
@Suppress( "BlockingMethodInNonBlockingContext")
private suspend fun CoroutineScope.shell(
    prefix:Array<String> = arrayOf("cmd","/k"),
    isMixingMessage: Boolean = false,
    isEcho: Boolean = false,
    dispatcher: CoroutineDispatcher = Default,
    block:suspend RunScope.()->Unit,
): Flow<ProcessingResults> {
    fun println(any: Any?) = printlns("running",any.toString())
    fun debug(any: Any?) = "shell".debug("running",any.toString())

    debug("building runner")

    val flow = MutableSharedFlow<ProcessingResults>()

    val process = try {
        ProcessBuilder(*prefix).run {
            if (isMixingMessage) this.redirectErrorStream(true)
            start()
        }
    } catch (e:IOException) {
        println("catch IO exception \n $e",)
        e.message?.let { msg -> when {
            msg.matches(exceptionRegex) -> { runBlocking {
                exceptionRegex.find(msg)!!.groupValues.let {
                    flow.emit(ProcessingResults.Error(it[2]))
                    flow.emit(ProcessingResults.CODE(it[1].toInt()))
                    flow.emit(ProcessingResults.Closed)
                } }
                return flow
            }
            "error=" in msg -> {
                //["cannot run xxxx","114514,message"]
                msg.split("error=")[1].split(",").let { runBlocking {
                    //["114514","message"]
                    flow.emit(ProcessingResults.Error(it[1]))
                    flow.emit(ProcessingResults.CODE(it[0].toInt()))
                    flow.emit(ProcessingResults.Closed)
                } }
                return flow
            }
            else -> Unit
        } }
        throw IOException("未知错误",e)
    }
    //runner构建完成
    debug("runner bullied")
    launch(dispatcher) {

        launch(dispatcher) {
            val writer = process.outputStream.writer()
            debug("writing")
            block( object : RunScope {
                override fun run(command: String) {
                    if (isEcho) {
                        writer.write("echo \"$command\" \n")
                        writer.flush()
                    }
                    writer.write(command)
                    debug("command", command)
                    writer.write("\n")
                    writer.flush()
                }
                override fun write(string: String) { TODO("懒得写") }
            })
        }.invokeOnCompletion {
            process.outputStream.runCatching { close() }
        }

        launch(dispatcher) {
            process.inputStream.bufferedReader().lineSequence().forEach {
                flow.emit(ProcessingResults.Message(it))
                printlns("message", it)
            }
        }
        //如果混合消息则直接跳过这次的collect
        if (!isMixingMessage) launch(dispatcher) {
            process.errorStream.bufferedReader(charset("GBK")).lineSequence().forEach {
                flow.emit(ProcessingResults.Error(it))
                "shell".error("failed",it)
            }
        }

    } .invokeOnCompletion {
        GlobalScope.launch {
            flow.emit(ProcessingResults.CODE(process.waitFor()))
            println("exiting")
            process.runCatching {
                inputStream.close()
                errorStream.close()
                destroy()
            }.onFailure {
                debug(it)
            }
            debug("all closed")
            flow.emit(ProcessingResults.Closed)
            debug("emit closed")
        }
    }
    return flow
}
