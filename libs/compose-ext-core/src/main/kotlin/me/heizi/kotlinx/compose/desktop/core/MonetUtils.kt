package me.heizi.kotlinx.compose.desktop.core


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.sqrt

fun main() {
    // read buffered image "C:\Users\xmzho\Desktop\BK5KO3O@)FHVPFD$6Z5]]}0.gif" and covert it as a sequence of color
    val colors = ImageIO.read(File("C:\\Users\\xmzho\\Desktop\\BK5KO3O@)FHVPFD$6Z5]]}0.gif")).let {
        sequence {
            for (x in 0 until it.width)
                for (y in 0 until it.height)
                    yield(Color(it.getRGB(x, y)))
        }
    }
    // run k-means algorithm k=5
    val kMeans = kMeans(10,colors)
    kMeans.forEach {
        println(it)
    }
    singleWindowApplication {
        LazyRow {
            items(kMeans.toList()) {
                //64x64dp box with color background
                Box(modifier = Modifier.size(64.dp).background(it).padding(4.dp))

            }
        }
    }
}

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

private fun Color.toHSB():Triple<Float,Float,Float>  {
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

