/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.palette.graphics

import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette.Swatch
import colorToHSL
import java.awt.Color.*
import java.util.*
import kotlin.math.roundToInt


/**
 * An color quantizer based on the Median-cut algorithm, but optimized for picking out distinct
 * colors rather than representation colors.
 *
 * The color space is represented as a 3-dimensional cube with each dimension being an RGB
 * component. The cube is then repeatedly divided until we have reduced the color space to the
 * requested number of colors. An average color is then generated from each cube.
 *
 * What makes this different to median-cut is that median-cut divided cubes so that all of the cubes
 * have roughly the same population, where this quantizer divides boxes based on their color volume.
 * This means that the color space is divided into distinct colors, rather than representative
 * colors.
 */
internal class ColorCutQuantizer(
    pixels: Array<Int>,
    maxColors: Int,
    val mFilters: Array<Palette.Filter>?
) {
    val colors: IntArray
    val histogram: IntArray = IntArray(1 shl (QUANTIZE_WORD_WIDTH * 3))
    var mQuantizedColors: MutableList<Swatch>? = null
    private val mTempHsl: FloatArray = FloatArray(3)

    /**
     * @return the list of quantized colors
     */
    val quantizedColors: List<Swatch>?
        get() = mQuantizedColors

    private fun quantizePixels(maxColors: Int): MutableList<Swatch> {
        // Create the priority queue which is sorted by volume descending. This means we always
        // split the largest box in the queue
        val pq: PriorityQueue<Vbox> = PriorityQueue(maxColors, VBOX_COMPARATOR_VOLUME)
        // To start, offer a box which contains all of the colors
        pq.offer(Vbox(0, colors.size - 1))
        // Now go through the boxes, splitting them until we have reached maxColors or there are no
        // more boxes to split
        splitBoxes(pq, maxColors)
        // Finally, return the average colors of the color boxes
        return generateAverageColors(pq)
    }

    /**
     * Iterate through the [java.util.Queue], popping
     * [ColorCutQuantizer.Vbox] objects from the queue
     * and splitting them. Once split, the new box and the remaining box are offered back to the
     * queue.
     *
     * @param queue [java.util.PriorityQueue] to poll for boxes
     * @param maxSize Maximum amount of boxes to split
     */
    private fun splitBoxes(queue: PriorityQueue<Vbox>, maxSize: Int) {
        while (queue.size < maxSize) {
            val vbox: Vbox? = queue.poll()
            if (vbox != null && vbox.canSplit()) {
                // First split the box, and offer the result
                queue.offer(vbox.splitBox())
                // Then offer the box back
                queue.offer(vbox)
            } else {
                // If we get here then there are no more boxes to split, so return
                return
            }
        }
    }

    private fun generateAverageColors(vboxes: Collection<Vbox>): MutableList<Swatch> {
        val colors: ArrayList<Swatch> = ArrayList(vboxes.size)
        for (vbox: Vbox in vboxes) {
            val swatch: Swatch = vbox.averageColor
            if (!shouldIgnoreColor(swatch)) {
                // As we're averaging a color box, we can still get colors which we do not want, so
                // we check again here
                colors.add(swatch)
            }
        }
        return colors
    }

    /**
     * Represents a tightly fitting box around a color space.
     */
    private inner class Vbox internal constructor(// lower and upper index are inclusive
        private val mLowerIndex: Int, private var mUpperIndex: Int
    ) {
        // Population of colors within this box
        private var mPopulation: Int = 0
        private var mMinRed: Int = 0
        private var mMaxRed: Int = 0
        private var mMinGreen: Int = 0
        private var mMaxGreen: Int = 0
        private var mMinBlue: Int = 0
        private var mMaxBlue: Int = 0

        init {
            fitBox()
        }

        val volume: Int
            get() = ((mMaxRed - mMinRed + 1) * (mMaxGreen - mMinGreen + 1) *
                    (mMaxBlue - mMinBlue + 1))

        fun canSplit(): Boolean {
            return colorCount > 1
        }

        val colorCount: Int
            get() {
                return 1 + mUpperIndex - mLowerIndex
            }

        /**
         * Recomputes the boundaries of this box to tightly fit the colors within the box.
         */
        fun fitBox() {
            val colors: IntArray = colors
            val hist: IntArray = histogram
            // Reset the min and max to opposite values
            var minRed: Int
            var minGreen: Int
            var minBlue: Int
            minBlue = Int.MAX_VALUE
            minGreen = minBlue
            minRed = minGreen
            var maxRed: Int
            var maxGreen: Int
            var maxBlue: Int
            maxBlue = Int.MIN_VALUE
            maxGreen = maxBlue
            maxRed = maxGreen
            var count: Int = 0
            for (i in mLowerIndex..mUpperIndex) {
                val color: Int = colors.get(i)
                count += hist.get(color)
                val r: Int = quantizedRed(color)
                val g: Int = quantizedGreen(color)
                val b: Int = quantizedBlue(color)
                if (r > maxRed) {
                    maxRed = r
                }
                if (r < minRed) {
                    minRed = r
                }
                if (g > maxGreen) {
                    maxGreen = g
                }
                if (g < minGreen) {
                    minGreen = g
                }
                if (b > maxBlue) {
                    maxBlue = b
                }
                if (b < minBlue) {
                    minBlue = b
                }
            }
            mMinRed = minRed
            mMaxRed = maxRed
            mMinGreen = minGreen
            mMaxGreen = maxGreen
            mMinBlue = minBlue
            mMaxBlue = maxBlue
            mPopulation = count
        }

        /**
         * Split this color box at the mid-point along its longest dimension
         *
         * @return the new ColorBox
         */
        fun splitBox(): Vbox {
            if (!canSplit()) {
                throw IllegalStateException("Can not split a box with only 1 color")
            }
            // find median along the longest dimension
            val splitPoint: Int = findSplitPoint()
            val newBox: Vbox = Vbox(splitPoint + 1, mUpperIndex)
            // Now change this box's upperIndex and recompute the color boundaries
            mUpperIndex = splitPoint
            fitBox()
            return newBox
        }

        /**
         * @return the dimension which this box is largest in
         */
        val longestColorDimension: Int
            get() {
                val redLength: Int = mMaxRed - mMinRed
                val greenLength: Int = mMaxGreen - mMinGreen
                val blueLength: Int = mMaxBlue - mMinBlue
                if (redLength >= greenLength && redLength >= blueLength) {
                    return COMPONENT_RED
                } else if (greenLength >= redLength && greenLength >= blueLength) {
                    return COMPONENT_GREEN
                } else {
                    return COMPONENT_BLUE
                }
            }

        /**
         * Finds the point within this box's lowerIndex and upperIndex index of where to split.
         *
         * This is calculated by finding the longest color dimension, and then sorting the
         * sub-array based on that dimension value in each color. The colors are then iterated over
         * until a color is found with at least the midpoint of the whole box's dimension midpoint.
         *
         * @return the index of the colors array to split from
         */
        fun findSplitPoint(): Int {
            val longestDimension: Int = longestColorDimension
            val colors: IntArray = colors
            val hist: IntArray = histogram
            // We need to sort the colors in this box based on the longest color dimension.
            // As we can't use a Comparator to define the sort logic, we modify each color so that
            // its most significant is the desired dimension
            modifySignificantOctet(colors, longestDimension, mLowerIndex, mUpperIndex)
            // Now sort... Arrays.sort uses a exclusive toIndex so we need to add 1
            Arrays.sort(colors, mLowerIndex, mUpperIndex + 1)
            // Now revert all of the colors so that they are packed as RGB again
            modifySignificantOctet(colors, longestDimension, mLowerIndex, mUpperIndex)
            val midPoint: Int = mPopulation / 2
            var i: Int = mLowerIndex
            var count: Int = 0
            while (i <= mUpperIndex) {
                count += hist.get(colors.get(i))
                if (count >= midPoint) {
                    // we never want to split on the upperIndex, as this will result in the same
                    // box
                    return Math.min(mUpperIndex - 1, i)
                }
                i++
            }
            return mLowerIndex
        }

        /**
         * @return the average color of this box.
         */
        val averageColor: Swatch
            get() {
                val colors: IntArray = colors
                val hist: IntArray = histogram
                var redSum: Int = 0
                var greenSum: Int = 0
                var blueSum: Int = 0
                var totalPopulation: Int = 0
                for (i in mLowerIndex..mUpperIndex) {
                    val color: Int = colors.get(i)
                    val colorPopulation: Int = hist.get(color)
                    totalPopulation += colorPopulation
                    redSum += colorPopulation * quantizedRed(color)
                    greenSum += colorPopulation * quantizedGreen(color)
                    blueSum += colorPopulation * quantizedBlue(color)
                }
                val redMean: Int = (redSum / totalPopulation.toFloat()).roundToInt()
                val greenMean: Int = (greenSum / totalPopulation.toFloat()).roundToInt()
                val blueMean: Int = (blueSum / totalPopulation.toFloat()).roundToInt()
                return Swatch(approximateToRgb888(redMean, greenMean, blueMean), totalPopulation)
            }
    }

    private fun shouldIgnoreColor(color565: Int): Boolean {
        val rgb: Int = approximateToRgb888(color565)
        colorToHSL(rgb, mTempHsl)
        return shouldIgnoreColor(rgb, mTempHsl)
    }

    private fun shouldIgnoreColor(color: Swatch): Boolean {
        return shouldIgnoreColor(color.rgb, color.hsl)
    }

    private fun shouldIgnoreColor(rgb: Int, hsl: FloatArray): Boolean {
        if (mFilters != null && mFilters.size > 0) {
            var i: Int = 0
            val count: Int = mFilters.size
            while (i < count) {
                if (!mFilters.get(i).isAllowed(rgb, hsl)) {
                    return true
                }
                i++
            }
        }
        return false
    }

    /**
     * Constructor.
     *
     * @param pixels histogram representing an image's pixel data
     * @param maxColors The maximum number of colors that should be in the result palette.
     * @param filters Set of filters to use in the quantization stage
     */
    init {
        val hist: IntArray = histogram
        for (i in pixels.indices) {
            val quantizedColor: Int = quantizeFromRgb888(pixels.get(i))
            // Now update the pixel value to the quantized value
            pixels[i] = quantizedColor
            // And update the histogram
            hist[quantizedColor]++
        }
        // Now let's count the number of distinct colors
        var distinctColorCount: Int = 0
        for (color in hist.indices) {
            if (hist[color] > 0 && shouldIgnoreColor(color)) {
                // If we should ignore the color, set the population to 0
                hist[color] = 0
            }
            if (hist[color] > 0) {
                // If the color has population, increase the distinct color count
                distinctColorCount++
            }
        }
        // Now lets go through create an array consisting of only distinct colors
        colors = IntArray(distinctColorCount)
        val colors: IntArray = colors
        var distinctColorIndex: Int = 0
        for (color in hist.indices) {
            if (hist.get(color) > 0) {
                colors[distinctColorIndex++] = color
            }
        }
        if (distinctColorCount <= maxColors) {
            // The image has fewer colors than the maximum requested, so just return the colors
            mQuantizedColors = ArrayList()
            for (color: Int in colors) {
                (mQuantizedColors as ArrayList<Swatch>).add(Swatch(approximateToRgb888(color), hist.get(color)))
            }
        } else {
            // We need use quantization to reduce the number of colors
            mQuantizedColors = quantizePixels(maxColors)
        }
    }

    companion object {
        const val COMPONENT_RED: Int = -3
        const val COMPONENT_GREEN: Int = -2
        const val COMPONENT_BLUE: Int = -1
        private const val QUANTIZE_WORD_WIDTH: Int = 5
        private const val QUANTIZE_WORD_MASK: Int = (1 shl QUANTIZE_WORD_WIDTH) - 1

        /**
         * Modify the significant octet in a packed color int. Allows sorting based on the value of a
         * single color component. This relies on all components being the same word size.
         *
         * @see Vbox.findSplitPoint
         */
        fun modifySignificantOctet(
            a: IntArray, dimension: Int,
            lower: Int, upper: Int
        ) {
            when (dimension) {
                COMPONENT_RED -> {}
                COMPONENT_GREEN ->                 // We need to do a RGB to GRB swap, or vice-versa
                {
                    var i: Int = lower
                    while (i <= upper) {
                        val color: Int = a.get(i)
                        a[i] =
                            (quantizedGreen(color) shl (QUANTIZE_WORD_WIDTH + QUANTIZE_WORD_WIDTH)
                                    ) or (quantizedRed(color) shl QUANTIZE_WORD_WIDTH
                                    ) or quantizedBlue(color)
                        i++
                    }
                }

                COMPONENT_BLUE ->                 // We need to do a RGB to BGR swap, or vice-versa
                {
                    var i: Int = lower
                    while (i <= upper) {
                        val color: Int = a.get(i)
                        a[i] =
                            (quantizedBlue(color) shl (QUANTIZE_WORD_WIDTH + QUANTIZE_WORD_WIDTH)
                                    ) or (quantizedGreen(color) shl QUANTIZE_WORD_WIDTH
                                    ) or quantizedRed(color)
                        i++
                    }
                }
            }
        }

        /**
         * Comparator which sorts [Vbox] instances based on their volume, in descending order
         */
        private val VBOX_COMPARATOR_VOLUME: Comparator<Vbox> = object : Comparator<Vbox> {
            override fun compare(lhs: Vbox, rhs: Vbox): Int {
                return rhs.volume - lhs.volume
            }
        }

        /**
         * Quantized a RGB888 value to have a word width of {@value #QUANTIZE_WORD_WIDTH}.
         */
        private fun quantizeFromRgb888(color: Int): Int {
            val r: Int = modifyWordWidth(Color.red(color), 8, QUANTIZE_WORD_WIDTH)
            val g: Int = modifyWordWidth(Color.green(color), 8, QUANTIZE_WORD_WIDTH)
            val b: Int = modifyWordWidth(Color.blue(color), 8, QUANTIZE_WORD_WIDTH)
            return (r shl (QUANTIZE_WORD_WIDTH + QUANTIZE_WORD_WIDTH)) or (g shl QUANTIZE_WORD_WIDTH) or b
        }

        /**
         * Quantized RGB888 values to have a word width of {@value #QUANTIZE_WORD_WIDTH}.
         * TODO
         */
        fun approximateToRgb888(r: Int, g: Int, b: Int): Int {
            return Color.rgb(
                modifyWordWidth(r, QUANTIZE_WORD_WIDTH, 8),
                modifyWordWidth(g, QUANTIZE_WORD_WIDTH, 8),
                modifyWordWidth(b, QUANTIZE_WORD_WIDTH, 8)
            )
        }

        private fun approximateToRgb888(color: Int): Int {
            return approximateToRgb888(
                quantizedRed(color),
                quantizedGreen(color),
                quantizedBlue(color)
            )
        }

        /**
         * @return red component of the quantized color
         */
        fun quantizedRed(color: Int): Int {
            return (color shr (QUANTIZE_WORD_WIDTH + QUANTIZE_WORD_WIDTH)) and QUANTIZE_WORD_MASK
        }

        /**
         * @return green component of a quantized color
         */
        fun quantizedGreen(color: Int): Int {
            return (color shr QUANTIZE_WORD_WIDTH) and QUANTIZE_WORD_MASK
        }

        /**
         * @return blue component of a quantized color
         */
        fun quantizedBlue(color: Int): Int {
            return color and QUANTIZE_WORD_MASK
        }

        private fun modifyWordWidth(value: Int, currentWidth: Int, targetWidth: Int): Int {
            val newValue: Int = if (targetWidth > currentWidth) {
                // If we're approximating up in word width, we'll shift up
                value shl (targetWidth - currentWidth)
            } else {
                // Else, we will just shift and keep the MSB
                value shr (currentWidth - targetWidth)
            }
            return newValue and ((1 shl targetWidth) - 1)
        }
    }
}

internal fun Color.Companion.red(color: Int): Int {
    return color shr 16 and 0xFF
}
internal fun Color.Companion.green(color: Int): Int {
    return color shr 8 and 0xFF
}
internal fun Color.Companion.blue(color: Int): Int {
    return color and 0xFF
}
internal fun Color.Companion.alpha(color: Int): Int {
    return color ushr 24
}
internal fun Color.Companion.rgb(red:Int,green:Int,blue:Int): Int {
    return -0x1000000 or (red shl 16) or (green shl 8) or blue
}
internal fun Color.Companion.argb(alpha:Int,red:Int,green:Int,blue:Int): Int {
    return alpha shl 24 or (red shl 16) or (green shl 8) or blue
}
