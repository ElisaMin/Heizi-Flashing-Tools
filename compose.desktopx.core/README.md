# Compose Desktop extension
# Fragment
## How to use it?
```kotlin
import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.heizi.kotlinx.compose.desktop.core.fragment.*


val page = @Composable {it:String->
    Column(Modifier.padding(32.dp)) {
        "$it Fragment"()
        Text(it)
        Box(Modifier.padding(16.dp))
        Button(onClick = {toggle()}) { Text("Toggle")}
    }
}

val screens = handlerOf(
    "page1" to {
         Fragment {
             "Page1Fragment"()
             page.invoke("its page1")
         }
    },
    "page2" to {
         Fragment {
             "Page2Fragment"()
             page.invoke("its page2")
         }
    },
)
fun toggle() {
    "toggle called"()
    val id = "page"+when(val page = screens.currentKey.last()){
        '1' -> '2'
        '2' -> '1'
        else -> throw IllegalStateException("$page")
    }
    screens.go(id)
}

fun main() = Window {
    FragmentContainer(screens)
}

operator fun String.invoke() = println(this)
```