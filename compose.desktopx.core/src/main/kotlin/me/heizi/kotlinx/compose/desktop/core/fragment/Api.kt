package me.heizi.kotlinx.compose.desktop.core.fragment


import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import me.heizi.kotlinx.logger.debug
import kotlin.reflect.KClass

/**
 * Get Fragment function
 */
typealias GetFragment = @Composable () -> FragmentINTF

internal operator fun String.invoke(vararg string: String) {
    "Fragment".debug(this,*string)
}



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
    "Container func called"()
    (handler as FragmentManager).setContent()
}

/**
 * Create a fragment handler
 *
 * @param fragments key to getFragment
 */
@Composable
fun handlerOf(
    vararg fragments: Pair<String,@Composable ()->FragmentINTF>,
):FragmentHandler = FragmentOwner(fragments)

@Composable
fun getFragment(t:KClass<out FragmentINTF>):FragmentINTF =
    (t.constructors.find { it is Function0<*> } as Function0<FragmentINTF>?)?.invoke()
        ?: throw IllegalStateException("FragmentCreator need zero args constructor !!!!")

@Composable
fun handlerOf(
    vararg fragments: KClass<out FragmentINTF>,
):FragmentHandler {



    return fragments.map<KClass<out FragmentINTF>,Pair<String,GetFragment>> {
        it.simpleName!! to {
            getFragment(it)
        }
    }.toTypedArray().let(::FragmentOwner)
}

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
    val handler:FragmentHandler
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
    fun go(clz:KClass<out FragmentINTF>, vararg args:Pair<String,Any>) {
        go(clz.simpleName!!,*args)
    }
}


internal interface FragmentManager:FragmentHandler {
    val fragments:List<Pair<String,GetFragment>>
    val currentIndex: State<String>
    override var current:FragmentINTF

    @Composable
    fun setContent() {
        val r = fragments.find{it.first == currentIndex.value}
        "$r is ready to In"()
        if (r!=null) {
            "$r is not null"()
            current = (r.second as GetFragment) ()
            (current as AbstractFragment).run {
                _handler = this@FragmentManager

            }
            "current is ${current.javaClass}"()
            current.content()
            "content seted"()
        }
    }
}


