package me.heizi.kotlinx.compose.desktop.core


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.system.exitProcess

suspend fun main() {
    // read buffered image "C:\Users\xmzho\Desktop\BK5KO3O@)FHVPFD$6Z5]]}0.gif" and covert it as a sequence of color
    val colors = ImageIO.read(File("C:\\Users\\xmzho\\Desktop\\BK5KO3O@)FHVPFD$6Z5]]}0.gif")).let {
//        sequence {
//            for (x in 0 until it.width)
//                for (y in 0 until it.height)
//                    yield(Color(it.getRGB(x, y)))
//        }
        Pixels(it)
    }
    // run k-means algorithm k=5
    val kMeans = colors.kMeansLasts(5)
    kMeans.forEach {
        println(it)
    }
    singleWindowApplication {
        LazyRow {
            items(kMeans.toList().map { it.toColor() }) {
                //64x64dp box with color background
                Box(modifier = Modifier.size(64.dp).background(it).padding(4.dp))

            }
        }
    }
}

abstract class Pixels:Iterator<RGB> {
    abstract fun pixel(x:Int,y:Int):RGB
    abstract fun size():Pair<Int,Int>
    private var index = 0
    // override iterator
    override fun hasNext(): Boolean = index < size().first*size().second
    override fun next(): RGB = pixel(index/size().second,index%size().second).also { index++ }

    companion object {
//        operator fun invoke(colors:Sequence<RGB>):Pixels = object : Pixels() {
//            override fun pixel(x: Int, y: Int): RGB = colors.elementAt(x*size().second+y)
//            override fun size(): Pair<Int, Int> = Pair(colors.count()/size().second,size().second)
//        }
//        operator fun invoke(colors:List<RGB>):Pixels = object : Pixels() {
//            override fun pixel(x: Int, y: Int): RGB = colors[x*size().second+y]
//            override fun size(): Pair<Int, Int> = Pair(colors.size/size().second,size().second)
//        }
        // jetpack compose image
        operator fun invoke(image: ImageBitmap):Pixels = object : Pixels() {
            override fun pixel(x: Int, y: Int): RGB = image.toPixelMap()[x,y].toRGB()
            override fun size(): Pair<Int, Int> = Pair(image.width,image.height)
        }
        operator fun invoke(image: BufferedImage):Pixels = object : Pixels() {
            override fun pixel(x: Int, y: Int): RGB = Color(image.getRGB(x,y)).toRGB()
            override fun size(): Pair<Int, Int> = Pair(image.width,image.height)
        }
    }
}
fun distance(a: RGB, b: RGB): Float {
    val rd = a.red - b.red
    val gd = a.green - b.green
    val bd = a.blue - b.blue
    return sqrt(rd * rd + gd * gd + bd * bd)
}

fun distances(a: RGB, b: RGB): Float =
    sqrt((a.red - b.red) * (a.red - b.red) +
            (a.green - b.green) * (a.green - b.green) +
            (a.blue - b.blue) * (a.blue - b.blue))

fun Pixels.kMeansLasts(k: Int): List<RGB> {
    val pixels = this
    // Initialize centroids by randomly selecting k pixels
    var centroids = List(k) { pixel(Random.nextInt(size().first),Random.nextInt(size().second)) }.toMutableList()
    var clusters: MutableMap<RGB, MutableList<RGB>>
    var closestCentroid: RGB
    var closestDist: Float
    var dist: Float
    var moved: Boolean
    var numIterations = 0

    do {
        numIterations++
        clusters = mutableMapOf()
        moved = false
        for (p in pixels) {
            closestCentroid = centroids[0]
            closestDist = distances(p, closestCentroid)

            for (i in 1 until k) {
                dist = distance(p, centroids[i])
                if (dist < closestDist) {
                    closestCentroid = centroids[i]
                    closestDist = dist
                }
            }

            if (!clusters.containsKey(closestCentroid)) {
                clusters[closestCentroid] = mutableListOf(p)
                moved = true
            } else if (!clusters[closestCentroid]!!.contains(p)) {
                clusters[closestCentroid]!!.add(p)
                moved = true
            }
        }

        // Recompute centroids
        for (i in 0 until k) {
            if (!clusters.containsKey(centroids[i])) {
                centroids = MutableList(k) { pixel(Random.nextInt(size().first),Random.nextInt(size().second)) }
                break
            }
            var red = 0f
            var green = 0f
            var blue = 0f
            for (pixel in clusters[centroids[i]]!!) {
                red += pixel.red
                green += pixel.green
                blue += pixel.blue
            }
            centroids[i] = RGB(
                red / clusters[centroids[i]]!!.size,
                green / clusters[centroids[i]]!!.size,
                blue / clusters[centroids[i]]!!.size
            )
        }
    } while (numIterations < 100 && moved)

    return centroids.sortedWith { a, b ->
        when(val aIsBetter = a.toHSB() isBetterThen b.toHSB()){
            (aIsBetter == null) -> 0
            aIsBetter -> -1
            else -> 1
        }
    }
}

fun Pixels.kMeansLast(k:Int):List<RGB> {
    val (width,height) = size()
    // init centroids mutable list as random pixels
    val centroids = (0 until k).map { pixel(Random.nextInt(width-1),Random.nextInt(height-1)) }.toMutableList()
    // init clusters mutable list as empty list
    val clusters = mutableMapOf<RGB,MutableList<RGB>>()
    while (true) {
        // clear clusters
        clusters.clear()
        // init clusters
        centroids.forEach { clusters[it] = mutableListOf() }
        // for each pixel
        forEach { pixel ->
            // find the nearest centroid
            val nearest = centroids.minByOrNull { distance(it,pixel) }!!
            // add the pixel to the cluster
            clusters[nearest]!!.add(pixel)
        }
        // init new centroids
        val newCentroids = mutableListOf<RGB>()
        // rgb as double

        // for each cluster
        clusters.forEach { (centroid, cluster) ->
            println(centroid)
            // init new centroid
            var red = 0.0
            var green = 0.0
            var blue = 0.0
            var size = 0
            // for each pixel in the cluster
            cluster.forEach { pixel ->
                // add the pixel to the new centroid
                red += pixel.red
                green += pixel.green
                blue += pixel.blue
                size++
            }
            // all not zero
            if (0.0 !in listOf(red,green,blue)) {
                red /= size
                green /= size
                blue /= size
            }


            if (red.isNaN() || green.isNaN() || blue.isNaN()) {
                println("NaN")
                // output rgb
                println(red)
                println(green)
                println(blue)
                exitProcess(-1)
            }

            println(red)
            // divide the new centroid by the size of the cluster as float
            val newCentroid = RGB(red.toFloat(),green.toFloat(),blue.toFloat())
            // add the new centroid to the new centroids
            newCentroids.add(newCentroid)
            println(newCentroid)
        }
        // if the new centroids are the same as the old centroids
        if (centroids == newCentroids) {
            // return the new centroids
            return newCentroids
        }
        // set the centroids to the new centroids
        centroids.clear()
        centroids.addAll(newCentroids)
    }

}

@Suppress("NOTHING_TO_INLINE")
private inline operator fun RGB.plus(pixel: RGB): RGB {
    return Triple(first + pixel.first,second + pixel.second,third + pixel.third)
}
// div by float
@Suppress("NOTHING_TO_INLINE")
private inline operator fun RGB.div(div: Int): RGB {
    return Triple(first / div,second / div,third / div)
}


//suspend fun Flow<RGB>.kMeans(k:Int,size: Int = -1): Flow<RGB> = coroutineScope {
//    fun distance(a: RGB, b: RGB): Float {
//        val rd = a.red - b.red
//        val gd = a.green - b.green
//        val bd = a.blue - b.blue
//        return sqrt(rd * rd + gd * gd + bd * bd)
//    }
//    var size = size
//    if (size < k)  size = count()
//    require(size > k)
//    withContext(Main) {
//        val group = size/k
//        var currentGroup = 0
//        flow {
//            var items = arrayListOf<RGB>()
//            collectIndexed { index, color ->
//                items.add(color)
//                val current =  index / group
//                if (currentGroup != current) {
//                    currentGroup = current
//                    emit(items.asFlow())
//                    items = arrayListOf()
//                }
//            }
//        }.map{
//            async {
//                it.map {
//
//                }
//            }
//        }
//    }
//
//
//    TODO()
//}

private typealias RGB = Triple<Float,Float,Float>
private inline fun Color.toRGB() = RGB(red,green,blue)
private inline fun RGB.toColor() = Color(red,green,blue)
private inline val RGB.red get() = first
private inline val RGB.green get() = second
private inline val RGB.blue get() = third
@Suppress("NAME_SHADOWING")
suspend fun kMeansNew(k:Int, colors:Sequence<RGB>): Sequence<RGB> = coroutineScope {
    fun distance(a: RGB, b: RGB): Float {
        val rd = a.red - b.red
        val gd = a.green - b.green
        val bd = a.blue - b.blue
        return sqrt(rd * rd + gd * gd + bd * bd)
    }
    var colors = colors.shuffled()

    var centroids = colors.take(k)
    var times = 0
    while (true) {
        println(times++)

        val clusters = colors.groupBy { color ->
            centroids.minByOrNull { item -> distance(item,color) }!!
        }

        var changed = false
        println(centroids.count())
        centroids = centroids.map {
            return@map clusters[it]?.to(it)
        }.filterNotNull().filter {(it,_)->
            it.isNotEmpty()
        }.map {(it,color) ->
            it.asSequence() to color
        }.map {(it,color) ->
            RGB(
                it.map { it.red }.average().toFloat(),
                it.map { it.green }.average().toFloat(),
                it.map { it.blue }.average().toFloat(),
            ) to color

        }.map {(new,old) ->
//            println(new.value to old.value)
            changed = changed || new != old
            if (changed) return@map new
            null
        }.apply { if (null in this) return@coroutineScope centroids.sortedWith { a, b ->
            when(val aIsBetter = a.toHSB() isBetterThen b.toHSB()) {
                (aIsBetter == null) -> 0
                aIsBetter -> -1
                else -> 1
            } }
        }.filterNotNull()

    }
    throw IllegalStateException("Should not reach here")
}

//fun kMeans(k: Int,colors:Flow<Color>) = flow<> {
//    fun distance(a: Color, b: Color): Double {
//        val rd = (a.red - b.red).toDouble()
//        val gd = (a.green - b.green).toDouble()
//        val bd = (a.blue - b.blue).toDouble()
//        return sqrt(rd * rd + gd * gd + bd * bd)
//    }
////    val clusters = mutableMapOf<Color,MutableList<Color>>()
//    val size = colors.count()
//    var centroids = flow {
//        repeat(k) {
//            var index = (Math.random() * size).toInt()
//            var last:Color? = null
//            colors.takeWhile { index > 0 }.collect {
//                index--
//                last = it
//            }
//            emit(last!!)
//        }
//    }
//    var times = 0
//    while (true) {
//        println(times++)
//        // Assign pixels to clusters based on closest centroid
//        colors.collect {
//            val closestCentroid = centroids.minByOrNull { item -> distance(item,it) }!!
//            emit(closestCentroid)
//        }
//
//    }
//
//
//}
/**
 * K-means algorithm
 * @param k number of clusters
 * @param colors sequence of colors
 * @return sequence of centroids
 */
fun kMeans(k:Int,colors:Sequence<Color>): Sequence<Color> {
    fun distance(a: Color, b: Color): Double {
        val rd = (a.red - b.red).toDouble()
        val gd = (a.green - b.green).toDouble()
        val bd = (a.blue - b.blue).toDouble()
        return sqrt(rd * rd + gd * gd + bd * bd)
    }
    val clusters = mutableMapOf<Color,MutableList<Color>>()

    var centroids = colors.shuffled().take(k)
    var times = 0
    while (true) {
        println(times++)
        // Assign pixels to clusters based on closest centroid
        clusters.clear()
        colors.forEach { color ->
            val closestCentroid = centroids.minByOrNull { item -> distance(item,color) }!!
            clusters.getOrPut(closestCentroid) { mutableListOf() }.add(color)
        }
        

        // Update centroids based on the mean of all pixels in the cluster
        var hasChanged = false
        centroids = centroids.map { color ->
            (clusters[color] ?: return@map color).toList().asSequence().let { cluster ->
                val size = cluster.count()
                Color(
                    red = cluster.sumOf { it.red } / size,
                    green = cluster.sumOf { it.green } / size,
                    blue = cluster.sumOf { it.blue } / size,
                )
            }.takeIf { new -> new != color }?.also { hasChanged = true }
        }.filterNotNull()

        if (!hasChanged) return centroids
//                .sortedBy { it.r+it.g+it.b }
        .sortedWith { a, b ->
            when(val aIsBetter = a.toHSB() isBetterThen b.toHSB()){
                (aIsBetter == null) -> 0
                aIsBetter -> -1
                else -> 1
            }
        }
    }
}

private inline fun <T> Sequence<T>.sumOf(crossinline function: (it:T) -> Float): Float {
    var sum = 0f
    for (element in this) {
        sum += function(element)
    }
    return sum
}

private infix fun Triple<Float,Float,Float>.isBetterThen(other: Triple<Float,Float,Float>): Boolean? {
    val s1 = this.second
    val s2: Float = other.second
    var b1 = this.third
    var b2 = other.third
    if (b1 >= 0.5) b1 = 1f - b1
    if (b2 >= 0.5) b2 = 1f - b2
    if (max(b2, b1) < 0.3) return null
    return (2 + s1) * (b1 + 1) > (2 + s2) * (b2 + 1)
}

private fun Color.toHSB():Triple<Float,Float,Float> = Triple(red / 255f,green / 255f,blue / 255f)
private fun RGB.toHSB():Triple<Float,Float,Float>  {
    val r = red / 255f
    val g = green / 255f
    val b = blue / 255f

    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    val h = when {
        delta == 0f -> 0f
        max == r -> ((g - b) / delta) % 6
        max == g -> (b - r) / delta + 2
        else -> (r - g) / delta + 4
    } * 60

    val s = if (max == 0f) 0f else delta / max

    return Triple(h, s, max)
}
private inline fun <T> Iterable<T>.sumOf(crossinline selector: (T) -> Float): Float {
    var sum = 0.toFloat()
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

