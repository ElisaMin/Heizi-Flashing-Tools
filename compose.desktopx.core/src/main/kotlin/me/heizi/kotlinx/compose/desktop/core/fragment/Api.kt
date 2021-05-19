package me.heizi.kotlinx.compose.desktop.core.fragment


import androidx.compose.runtime.Composable
import androidx.compose.runtime.State


fun Fragment(content:@Composable ()->Unit):Fragment
    = object : AbstractFragment() {
        override val content: @Composable () -> Unit = content
    }
@Composable
fun FragmentContainer(handler: FragmentHandler) {
    (handler as FragmentManager).setContent()
}


fun handlerOf(
    vararg fragments: Pair<String,@Composable ()->Fragment>,
):FragmentHandler = FragmentOwner(fragments)


interface Fragment {
    val args:Collection<Any>
    val content:@Composable ()->Unit
}

interface FragmentHandler {
    val current:Fragment
    val currentKey:String
    fun go(key:String, vararg args:Pair<String,Any>)
}


internal interface FragmentManager {
    val fragments:List<()->Fragment>
    val currentIndex: State<Int>
    var current:Fragment

    @Composable
    fun setContent() {
        current = (fragments[currentIndex.value] as (@Composable  ()->Fragment ))()
        current.content()
    }
}


