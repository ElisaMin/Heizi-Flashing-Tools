package me.heizi.kotlinx.compose.desktop.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.awt.Component
import java.awt.Container
import kotlin.math.max


fun Component.setAllBackground(color: java.awt.Color) {
    background = color
    if (this is Container) {
        components.forEach { it.setAllBackground(color) }
    }
}


private typealias RGB = Triple<Int, Int, Int>

/**
 * Uses the octree algorithm to extract the dominant color from an iterable of colors.
 *
 * @param maxLevel The maximum level of the octree.
 * @return The dominant color as an RGBColor object.
 */
@JvmName("extractDominantColorComposeColorOfIterable")
@Suppress("unused")
fun Iterable<Color>.extractDominantColor(maxLevel: Int = 8): List<Color> =
    asSequence().extractDominantColor(maxLevel)

/**
 * Uses the octree algorithm to extract the dominant color from an iterable of colors.
 *
 * @param maxLevel The maximum level of the octree.
 * @return The dominant color as an RGBColor object.
 */
@JvmName("extractDominantColorComposeColorOfSequence")
fun Sequence<Color>.extractDominantColor(maxLevel: Int = 8): List<Color> =
    map { it.triple }
        .extractDominantColor(maxLevel)
        .map { it.compose }

/**
 * Sorts the colors in an iterable/sequence by hue, saturation and brightness (HSB).
 * @return The sorted list of colors.
 */
@JvmName("sortByHSBIterable")
@Suppress("unused")
fun Iterable<Color>.sortByHSB() =
    asSequence()
        .map { it.triple }
        .sortByHSB()
        .map { it.compose }

/**
 * Uses the octree algorithm to extract the dominant color from a sequence of RGB colors.
 *
 * @param maxLevel The maximum level of the octree.
 * @return The dominant color as an RGBColor object.
 */
@JvmName("extractDominantColorRGBColorOfSequence")
fun Sequence<RGB>.extractDominantColor(maxLevel: Int = 8): List<RGB> =
    Octree(maxLevel).apply { forEach(::addColor) }.reduce().getColors()

/**
 * Sorts the colors in an iterable/sequence by hue, saturation and brightness (HSB).
 * @return The sorted list of colors.
 */
@JvmName("sortByHSBSequence")
fun Sequence<RGB>.sortByHSB() = sortedWith { a, b ->
    when (a.toHSB() isBetterThen b.toHSB()) {
        null -> 0
        true -> -1
        else -> 1
    }
}



private inline val RGB.compose
    get() = java.awt.Color(red, green, blue)
        .run { Color(red, green, blue) }

private inline val Color.triple
    get() = java.awt.Color(toArgb())
        .run { Triple(red, green, blue) }


private inline val RGB.red get() = first
private inline val RGB.green get() = second
private inline val RGB.blue get() = third

// An octree data structure for storing colors
private class Octree(private val maxLevel: Int) {

    private val root = OctreeNode(0, null)
    private var leafCount = 0

    // Adds a color to the octree, creating or updating nodes as needed
    fun addColor(color: RGB) {
        var node = root
        for (level in 0 until maxLevel) {
            val index = node.getChildIndex(color)
            var child = node.children[index]
            if (child == null) {
                child = OctreeNode(level + 1, node)
                node.children[index] = child
                leafCount++
            }
            child.pixelCount++
            child.red += color.red
            child.green += color.green
            child.blue += color.blue
            node = child
        }
    }

    companion object {
        // A constant that defines the number of colors to keep in the octree after reduction (parameter)
        private const val K = 16
    }

    // Reduces the octree by merging the least populated leaf nodes with their parent
    fun reduce(): Octree {
        while (leafCount > K) {
            // Find the minimum pixel count of all leaf nodes
            var minCount = Int.MAX_VALUE
            root.children.forEach { node ->
                node?.let {
                    if (it.pixelCount in 1 until minCount) {
                        minCount = it.pixelCount
                    }
                }
            }
            // Merge all leaf nodes with the minimum pixel count with their parent
            root.children.forEach { node ->
                node?.let {
                    if (it.pixelCount == minCount) {
                        it.parent?.let { parent ->
                            parent.pixelCount += it.pixelCount
                            parent.red += it.red
                            parent.green += it.green
                            parent.blue += it.blue
                            parent.children[it.getChildIndex(it.getAverageColor())] = null
                            leafCount--
                        }
                    }
                }
            }
        }
        return this
    }

    // Returns the list of colors in the octree after reduction
    fun getColors(): List<RGB> {
        return root.children.mapNotNull { it?.getAverageColor() }
    }

}

// A node in the octree
private class OctreeNode(
    val level: Int,
    val parent: OctreeNode?,
    var pixelCount: Int = 0,
    var red: Int = 0,
    var green: Int = 0,
    var blue: Int = 0,
    val children: Array<OctreeNode?> = arrayOfNulls(8)
) {
    // Returns the index of the child node for a given color
    fun getChildIndex(color: RGB): Int {
        var index = 0
        val mask = 0x80 shr level
        if (color.red and mask != 0) index += 4
        if (color.green and mask != 0) index += 2
        if (color.blue and mask != 0) index += 1
        return index
    }

    // Returns the average color of this node
    fun getAverageColor(): RGB {
        return if (pixelCount > 0) {
            RGB(red / pixelCount, green / pixelCount, blue / pixelCount)
        } else {
            RGB(0, 0, 0)
        }
    }
}

private infix fun Triple<Float, Float, Float>.isBetterThen(other: Triple<Float, Float, Float>): Boolean? {
    val s1 = this.second
    val s2: Float = other.second
    var b1 = this.third
    var b2 = other.third
    if (b1 >= 0.5) b1 = 1f - b1
    if (b2 >= 0.5) b2 = 1f - b2
    if (max(b2, b1) < 0.3) return null
    return (2 + s1) * (b1 + 1) > (2 + s2) * (b2 + 1)
}

private fun RGB.toHSB(): Triple<Float, Float, Float> {
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