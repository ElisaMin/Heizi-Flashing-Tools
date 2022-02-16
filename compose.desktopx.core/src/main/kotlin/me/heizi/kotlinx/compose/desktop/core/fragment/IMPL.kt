package me.heizi.kotlinx.compose.desktop.core.fragment

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.heizi.kotlinx.logger.debug


/**
 * abstract fragment 抽象Fragment
 *
 * 默认的Fragment Impl
 * @param args for default args
 */
@Deprecated("remove on next version", ReplaceWith("Decompose"))
abstract class AbstractFragment(
    vararg args: Pair<String, Any>
):FragmentINTF {

    internal var _handler: FragmentHandler? = null
    final override val handler: FragmentHandler
        get() = _handler ?: throw IllegalAccessException("handler not exist")

    /**
     * mutable argument and it just a map
     */
    internal val _args = hashMapOf(*args)

    /**
     * public argument getter
     */
    final override val args get() = _args

    /**
     * event and block
     */
    internal val events = hashMapOf<Event,()->Unit>()

    /**
     * on event
     *
     * @see FragmentINTF.on
     */
    final override fun on(event: Event, block: () -> Unit) {
        events[event] = block
    }
}
fun <T,R:Comparable<R>> ArrayList<Pair<R,T>>.hashUp() {
    val lastOne = hashMapOf<R,Int>()
    map { it.first }.forEachIndexed { index, s ->
        lastOne[s] = index
    }
    lastOne.toList().sortedBy {
        it.second
    }.map { this[it.second] }.let {
        this.clear()
        this.addAll(it)
    }
}
/**
 * FragmentOwner
 *
 * implement of [FragmentManager] & [FragmentHandler]
 * @param fragments key and getFragment
 */
@Deprecated("remove on next version",ReplaceWith("Decompose"))
internal class FragmentOwner(
    fragments: Array<out Pair<String, GetFragment>>,
):FragmentManager,FragmentHandler {

    /**
     * Fragments 获取所有getFragment block
     *
     * @see FragmentManager.fragments
     */
    override val fragments: ArrayList<Pair<String, GetFragment>> = arrayListOf<Pair<String,GetFragment>>().apply {
        fragments.forEach(::add)
        hashUp()
    }



    /**
     * nullable _current
     */
    private var _current: FragmentINTF? = null

    var lock = false
    /**
     * 启动Fragment by key
     *
     * @param key
     * @param args
     * @see FragmentHandler.go
     */
    override fun go(key: String, vararg args: Pair<String, Any>):Unit {
        GlobalScope.launch {
            while (lock) { delay(1)}

            lock = true
            "Fragment".debug("go","$currentKey to $key")
            //event invoke onDestroy
            if (_current != null) {
                require(_current is AbstractFragment) {
                    "${_current?.javaClass} is not Abstract Fragment"
                }
                (_current as AbstractFragment).events[Event.Destroy]?.invoke()
            }
            GlobalScope.launch {
                //event invoke onCreate
                val current = current
                launch {
                    currentIndex.value = key
                }
                while (current == _current )
                    delay(1)

                (_current as AbstractFragment).run {
                    debug("args putting  ",_args,args.map { "${it.first}:${it.second}" }.joinToString(","))
                    _args.putAll(args)
                    debug("args",this.args)
                    events[Event.Create]?.invoke()
                }
                lock = false
            }
        }
    }


    /**
     * Current index 当前页面相对于[fragmentAnyKey] or [fragments] 的索引
     *
     * @see FragmentManager.currentIndex
     */
    override val currentIndex: MutableState<String> = mutableStateOf(this.fragments[0].first)


    /**
     * Current 选中的Fragment
     *
     * shell of [_current]
     * @see FragmentHandler.current
     */
    override var current: FragmentINTF
        get() = _current!!
        set(value) {_current = value}

    /**
     * Current key 获取当前的Fragment的Key
     *
     * @see FragmentHandler.current
     */
    override val currentKey: String get() = currentIndex.value
}
