import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Bitmap
import org.jetbrains.skiko.toImage
import java.io.File
import javax.imageio.ImageIO


suspend fun main(args: Array<String>) {
    args.asSequence().map {
        println(it)
        ImageIO.read(File(it))
    }.map {
        Bitmap.Companion.makeFromImage(it.toImage())
    }.flatMap {
        runBlocking {
            Palette.Builder(it).generate().let {
                println(it.dominantSwatch)
                it.all.asSequence()
            }
        }
    }.map { it.toString() }.forEach(::println)
}