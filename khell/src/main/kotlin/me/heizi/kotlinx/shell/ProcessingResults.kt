package me.heizi.kotlinx.shell


/**
 * 处理中会弹出的数据
 */
sealed class ProcessingResults {



    /**
     * [Process.exitValue] 退出返回值
     */
    class CODE internal constructor(val code:Int):ProcessingResults() {

        companion object {
            const val SUCCESS = 0
        }

        override fun toString(): String {
            return "CODE(code=$code)"
        }
    }

    /**
     * [Process.getErrorStream] 错误数据
     *
     * @param message  错误数据的本身
     */
    class Error internal constructor(val message:String):ProcessingResults() {
        override fun toString(): String {
            return "Error(message='$message')"
        }
    }

    /**
     * [Process.getInputStream] 弹出的数据
     *
     * @param message
     */
    class Message internal constructor(val message: String):ProcessingResults() {
        override fun toString(): String {
            return "Message(message='$message')"
        }
    }

    /**
     * Closed process执行完毕
     */
    object Closed : ProcessingResults()

    /**
     * Starting 正在启动
     *
     */
//    object Starting : ProcessingResults()
}