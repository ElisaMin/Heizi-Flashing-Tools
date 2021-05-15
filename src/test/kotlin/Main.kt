import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import kotlinx.coroutines.runBlocking
import me.heizi.kotlinx.shell.shell
import kotlin.test.Test


class Main {
    @Test
    fun mainTest() = Window {


        MaterialTheme {
            Column {

                Button(onClick = {
                    runBlocking {
                        shell(*arrayOf("echo heizi"))
                    }
                }) {
                    Text("EchoHeizi")
                }
            }
//        start()
//        val devices = mutableStateListOf<String>()
//        GlobalScope.launch {
//            delay(2000)
//            devices.add("LMG710ULMahshadf56wdascode")
//            delay(1000)
//            devices.add("LMG710ULMajhshadf56wdascode")
//            delay(1000)
//            devices.add("LMG710ULMajhaaashadf56wdascode")
//        }
//        devices(devices)
//        check()
//        flashing()
        }
    }
}