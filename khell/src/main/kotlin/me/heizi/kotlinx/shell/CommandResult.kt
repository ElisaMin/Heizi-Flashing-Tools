package me.heizi.kotlinx.shell


import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import me.heizi.kotlinx.logger.debug
import me.heizi.kotlinx.logger.error
import me.heizi.kotlinx.logger.println
import me.heizi.kotlinx.shell.ProcessingResults.CODE.Companion.SUCCESS

/**
 * 程序结束时会拿到的结果
 */
sealed class CommandResult {
    companion object {
        /**
         * 等待正在执行的程序退出并返回结果
         * @return 完整的程序执行结果
         */
        suspend fun Flow<ProcessingResults>.waitForResult(
            onMessage:(String)->Unit = {},
            onError:(String)->Unit={},
            onResult:(CommandResult)->Unit = {},
        ): CommandResult {
            "result".debug("waiting")
            val message:StringBuilder = StringBuilder()
            var error:StringBuilder? = null
            var result: CommandResult? = null
            this.takeWhile {
                it !is ProcessingResults.Closed
            }.collect {
                when(it) {
                    is ProcessingResults.Message -> {
                        if (message.isNotEmpty()) message.append("\n")
                        message.append(it.message)
                        onMessage(it.message)
                    }
                    is ProcessingResults.Error -> {
                        error?.let { e ->
                            "result".debug("error",it.message)
                            e.append(it.message)
                        } ?: run {
                            error = StringBuilder(it.message)
                        }
                        onError(it.message)
                    }
                    is ProcessingResults.CODE -> {
                        result = if (it.code == SUCCESS) {
                            Success(message.toString())
                        } else {
                            Failed(message.toString(), error?.toString(), it.code)
                        }
                        GlobalScope.launch {
                            onResult(result!!)
                        }
                        "result".debug("code",it.code)
                        "result".debug(result!!::class.simpleName,result)
                        currentCoroutineContext().cancel()
                    }
                    is ProcessingResults.Closed -> {
                        "result".debug(result!!::class.simpleName,result)
                        currentCoroutineContext().cancel()
                    }
                }
            }
            return result!!
        }
        private var errorTimes = -1
        fun Exception.toResult():Failed {
            return Failed(stackTrace.joinToString(":"),message, errorTimes--)
        }
    }
    data class Success internal constructor(val message: String): CommandResult()
    data class Failed internal constructor(val processingMessage: String,val errorMessage:String?,val code: Int): CommandResult()
}