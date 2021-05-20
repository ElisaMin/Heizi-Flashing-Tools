package me.heizi.kotlinx.compose.desktop.core.fragment


import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

/**
 * Create a fragment
 */
fun Fragment(vararg args: Pair<String, Any>,content:@Composable ()->Unit,):FragmentINTF
    = object : AbstractFragment(*args) {
        override val content: @Composable () -> Unit = content
    }

/**
 * Create a container
 */
@Composable
fun FragmentContainer(handler: FragmentHandler) {
    (handler as FragmentManager).setContent()
}

/**
 * Create a fragment handler
 *
 * @param fragments key to getFragment
 */
//TODO FIXME: 2021/5/20 HashMap has sort
fun handlerOf(
    vararg fragments: Pair<String,@Composable ()->FragmentINTF>,
):FragmentHandler = FragmentOwner(fragments)

/**
 * Event
 *
 * Fragment的启动和摧毁事件
 */
enum class Event {
    Create,
    Destroy,
}

/**
 * DONT USE IT
 *
 * @see me.heizi.kotlinx.compose.desktop.core.fragment.AbstractFragment
 */
interface FragmentINTF {
    val args:Map<String,Any>
    val content:@Composable ()->Unit
    fun on(event: Event,block:()->Unit)
}

/**
 * Fragment handler 掌管着
 *
 * 实例创建：
 * @see handlerOf
 */
interface FragmentHandler {
    /**
     * Current
     */
    val current:FragmentINTF

    /**
     * Current key
     */
    val currentKey:String

    /**
     * TODO 懒得写注释（
     *
     * @param key
     * @param args
     */
    fun go(key:String, vararg args:Pair<String,Any>)
}


internal interface FragmentManager {
    val fragments:List<()->FragmentINTF>
    val currentIndex: State<Int>
    var current:FragmentINTF
    @Composable
    fun setContent() {
        current = (fragments[currentIndex.value] as (@Composable  ()->FragmentINTF ))()
        current.content()
    }
}


