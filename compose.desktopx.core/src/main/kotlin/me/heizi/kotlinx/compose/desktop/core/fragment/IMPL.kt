package me.heizi.kotlinx.compose.desktop.core.fragment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf


/**
 * abstract fragment 抽象Fragment
 *
 * 默认的Fragment Impl
 * @param args for default args
 */
abstract class AbstractFragment(
    vararg args: Pair<String, Any>
):FragmentINTF {
    /**
     * mutable argument and it just a map
     */
    internal val _args = hashMapOf(*args)

    /**
     * public argument getter
     */
    final override val args get() = _args.toMap()

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

/**
 * FragmentOwner
 *
 * implement of [FragmentManager] & [FragmentHandler]
 * @param fragments key and getFragment
 */
internal class FragmentOwner(
    fragments: Array<out Pair<String, @Composable () -> FragmentINTF>>,
):FragmentManager,FragmentHandler {

    /**
     * Fragment any key
     */
    private val fragmentAnyKey: HashMap<String,()-> FragmentINTF> = hashMapOf(*fragments)

    /**
     * nullable _current
     */
    private var _current: FragmentINTF? = null

    /**
     * 启动Fragment by key
     *
     * @param key
     * @param args
     * @see FragmentHandler.go
     */
    override fun go(key: String, vararg args: Pair<String, Any>) {
        if (_current != null) {
            require(_current is AbstractFragment) {
                "${_current?.javaClass} is not Abstract Fragment"
            }
            (_current as AbstractFragment).events[Event.Destroy]?.invoke()
        }
        fragmentAnyKey.keys.indexOf(key).let {
            currentIndex.value = it
            (_current as AbstractFragment)._args.putAll(args)
            (_current as AbstractFragment).events[Event.Create]?.invoke()
        }
    }


    /**
     * Current index 当前页面相对于[fragmentAnyKey] or [fragments] 的索引
     *
     * @see FragmentManager.currentIndex
     */
    override val currentIndex: MutableState<Int>  = mutableStateOf(0)

    /**
     * Fragments 获取所有getFragment block
     *
     * @see FragmentManager.fragments
     */
    override val fragments: List<() -> FragmentINTF> get() = fragmentAnyKey.values.toList()

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
    override val currentKey: String get() = fragmentAnyKey.keys.toList()[currentIndex.value]
}
