package me.heizi.kotlinx.compose.desktop.core.fragment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf


internal abstract class AbstractFragment(
    vararg args: Any
):Fragment {
    override var args: MutableCollection<Any> = arrayListOf(args)
}
internal class FragmentOwner(
    fragments: Array<out Pair<String, @Composable () -> Fragment>>,
):FragmentManager,FragmentHandler {
    private val fragmentAnyKey: HashMap<String,()-> Fragment> = hashMapOf(*fragments)
    override val fragments: List<() -> Fragment> get() = fragmentAnyKey.values.toList()
    override val currentIndex: MutableState<Int>  = mutableStateOf(0)
    override lateinit var current: Fragment
    override val currentKey: String get() = fragmentAnyKey.keys.toList()[currentIndex.value]
    override fun go(key: String, vararg args: Pair<String, Any>) {
        fragmentAnyKey.keys.indexOf(key).let {
            currentIndex.value = it
        }
    }

}
