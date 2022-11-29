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
@file:Suppress("DataClassPrivateConstructor")

package androidx.palette.graphics

import RGBToHSL
import androidx.collection.SparseArrayCompat
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette.Builder
import calculateMinimumAlpha
import org.jetbrains.skia.*
import setAlphaComponent
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil

private val Color.Companion.WHITE: Int
    get() = White.value.toInt()
private val Color.Companion.BLACK: Int
    get() = Black.value.toInt()

@Deprecated("not this shit", replaceWith = ReplaceWith(""))
annotation class ColorInt
@Deprecated("not this shit")
annotation class Px

typealias SparseBooleanArrayList
    = SparseArrayCompat<Boolean>

/**
 * A helper class to extract prominent colors from an image.
 *
 *
 * A number of colors with different profiles are extracted from the image:
 *
 *  * Vibrant
 *  * Vibrant Dark
 *  * Vibrant Light
 *  * Muted
 *  * Muted Dark
 *  * Muted Light
 *
 * These can be retrieved from the appropriate getter method.
 *
 *
 *
 * Instances are created with a [Builder] which supports several options to tweak the
 * generated Palette. See that class' documentation for more information.
 *
 *
 * Generation should always be completed on a background thread, ideally the one in
 * which you load your image on. [Builder] supports both synchronous and asynchronous
 * generation:
 *
 * <pre>
 * // Synchronous
 * Palette p = Palette.from(bitmap).generate();
 *
 * // Asynchronous
 * Palette.from(bitmap).generate(new PaletteAsyncListener() {
 * public void onGenerated(Palette p) {
 * // Use generated instance
 * }
 * });
</pre> *
 */
class Palette internal constructor(
    private val swatches: List<Swatch>,
    private val targets: Set<Targets>
) {

    private val selectedSwatches: MutableMap<Targets, Swatch> = hashMapOf()
    private val usedColors: SparseBooleanArrayList = SparseBooleanArrayList()

    /**
     * Returns the dominant swatch from the palette.
     *
     *
     * The dominant swatch is defined as the swatch with the greatest population (frequency)
     * within the palette.
     */
    val dominantSwatch: Swatch? by lazy {
        findDominantSwatch()
    }

    /**
     * Returns the selected swatch for the given target from the palette, or `null` if one
     * could not be found.
     */
    operator fun get(target: Targets) = selectedSwatches[target]


    fun generate() {
        // TODO didnt working
        // We need to make sure that the scored targets are generated first. This is so that
        // inherited targets have something to inherit from
        for (target in targets) {
            target.normalizeWeights()
            generateScoredTarget(target)?.let { selectedSwatches.put(target, it) }
        }
        println(selectedSwatches.size)
        // We now clear out the used colors
        usedColors.clear()
    }


    private fun generateScoredTarget(target: Targets) =
        getMaxScoredSwatchForTarget(target)?.also {maxScoreSwatch ->
            // If we have a swatch, and the target is exclusive, add the color to the used list
            if (target.isExclusive) usedColors.append(maxScoreSwatch.rgb, true)
        }


    private fun getMaxScoredSwatchForTarget(target: Targets): Swatch? {
        var maxScore = 0f
        var maxScoreSwatch: Swatch? = null
        swatches.asSequence().filter {
            shouldBeScoredForTarget(it, target)
        }.forEach { swatch ->
            if (maxScoreSwatch==null) maxScoreSwatch= swatch
            generateScore(swatch,target).takeIf { it>maxScore }?.let {
                maxScore = it
                maxScoreSwatch = swatch
            }
            println("$target max $maxScoreSwatch")
        }
        return maxScoreSwatch
    }

    private fun shouldBeScoredForTarget(swatch: Swatch, target: Targets): Boolean =
        // Check whether the HSL values are within the correct ranges, and this color hasn't
        // been used yet.
        target.run {
            val hsl = swatch.hsl
            hsl[1] in minimumSaturation..maximumSaturation && hsl[2] in minimumLightness..maximumLightness
        } && (!usedColors.get(swatch.rgb,false))

    private fun generateScore(swatch: Swatch, target: Targets): Float {
        val hsl = swatch.hsl
        var saturationScore = 0f
        var luminanceScore = 0f
        var populationScore = 0f
        val maxPopulation = dominantSwatch?.population ?: 1
        if (target.saturationWeight > 0) {
            saturationScore = (target.saturationWeight
                    * (1f - abs(hsl[1] - target.targetSaturation)))
        }
        if (target.lightnessWeight > 0) {
            luminanceScore = (target.lightnessWeight
                    * (1f - abs(hsl[2] - target.targetLightness)))
        }
        if (target.populationWeight > 0) {
            populationScore = (target.populationWeight
                    * (swatch.population / maxPopulation.toFloat()))
        }
        return saturationScore + luminanceScore + populationScore
    }

    private fun findDominantSwatch(): Swatch? {
        var maxPop = Int.MIN_VALUE
        var maxSwatch: Swatch? = null
        var i = 0
        val count = swatches.size
        while (i < count) {
            val swatch = swatches[i]
            if (swatch.population > maxPop) {
                maxSwatch = swatch
                maxPop = swatch.population
            }
            i++
        }
        return maxSwatch
    }

    /**
     * Represents a color swatch generated from an image's palette. The RGB color can be retrieved
     * by calling [.getRgb].
     */
    class Swatch(color: Int, population: Int){
        private val mRed: Int
        private val mGreen: Int
        private val mBlue: Int

        val color = Color(color)
        /**
         * @return this swatch's RGB color value
         */
        val rgb: Int

        /**
         * @return the number of pixels represented by this swatch
         */
        val population: Int
        private var mGeneratedTextColors = false
        private var mTitleTextColor = 0
        private var mBodyTextColor = 0
        private var mHsl: FloatArray? = null

        init {
            mRed = Color.red(color)
            mGreen = Color.green(color)
            mBlue = Color.blue(color)
            rgb = color
            this.population = population
        }

        /**
         * Return this swatch's HSL values.
         * hsv[0] is Hue [0 .. 360)
         * hsv[1] is Saturation [0...1]
         * hsv[2] is Lightness [0...1]
         */
        val hsl: FloatArray
            get() {
                if (mHsl == null) {
                    mHsl = FloatArray(3)
                }
                RGBToHSL(mRed, mGreen, mBlue, mHsl!!)
                return mHsl as FloatArray
            }

        /**
         * Returns an appropriate color to use for any 'title' text which is displayed over this
         * [Swatch]'s color. This color is guaranteed to have sufficient contrast.
         */
        val titleTextColor: Int
            get() {
                ensureTextColorsGenerated()
                return mTitleTextColor
            }

        /**
         * Returns an appropriate color to use for any 'body' text which is displayed over this
         * [Swatch]'s color. This color is guaranteed to have sufficient contrast.
         */
        val bodyTextColor: Int
            get() {
                ensureTextColorsGenerated()
                return mBodyTextColor
            }

        private fun ensureTextColorsGenerated() {
            if (!mGeneratedTextColors) {
                // First check white, as most colors will be dark
                val lightBodyAlpha = calculateMinimumAlpha(
                    Color.WHITE, rgb, MIN_CONTRAST_BODY_TEXT
                )
                val lightTitleAlpha = calculateMinimumAlpha(
                    Color.WHITE, rgb, MIN_CONTRAST_TITLE_TEXT
                )
                if (lightBodyAlpha != -1 && lightTitleAlpha != -1) {
                    // If we found valid light values, use them and return
                    mBodyTextColor = setAlphaComponent(Color.WHITE, lightBodyAlpha)
                    mTitleTextColor = setAlphaComponent(Color.WHITE, lightTitleAlpha)
                    mGeneratedTextColors = true
                    return
                }
                val darkBodyAlpha = calculateMinimumAlpha(
                    Color.BLACK, rgb, MIN_CONTRAST_BODY_TEXT
                )
                val darkTitleAlpha = calculateMinimumAlpha(
                    Color.BLACK, rgb, MIN_CONTRAST_TITLE_TEXT
                )
                if (darkBodyAlpha != -1 && darkTitleAlpha != -1) {
                    // If we found valid dark values, use them and return
                    mBodyTextColor = setAlphaComponent(Color.BLACK, darkBodyAlpha)
                    mTitleTextColor = setAlphaComponent(Color.BLACK, darkTitleAlpha)
                    mGeneratedTextColors = true
                    return
                }
                // FIXME:
                if (darkBodyAlpha== -1) {
                    mBodyTextColor = Color.WHITE
                    return
                }
                // If we reach here then we can not find title and body values which use the same
                // lightness, we need to use mismatched values
                mBodyTextColor = if (lightBodyAlpha != -1) setAlphaComponent(
                    Color.WHITE,
                    lightBodyAlpha
                ) else setAlphaComponent(Color.BLACK, darkBodyAlpha)
                mTitleTextColor = if (lightTitleAlpha != -1) setAlphaComponent(
                    Color.WHITE,
                    lightTitleAlpha
                ) else setAlphaComponent(Color.BLACK, darkTitleAlpha)
                mGeneratedTextColors = true
            }
        }

        override fun toString(): String {
            return StringBuilder(javaClass.simpleName)
                .append(" [RGB: #").append(Integer.toHexString(rgb)).append(']')
                .append(" [HSL: ").append(hsl.contentToString()).append(']')
                .append(" [Population: ").append(population).append(']')
                .append(" [Title Text: #").append(Integer.toHexString(titleTextColor))
                .append(']')
                .append(" [Body Text: #").append(Integer.toHexString(bodyTextColor))
                .append(']').toString()
        }

        // TODO Remove @Nullable once AGP 3.3. Fixed by I32b659c4e842ba5ac3d45b2d75b080b810fe1fe8.
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other == null || javaClass != other.javaClass) {
                return false
            }
            val swatch = other as Swatch
            return population == swatch.population && rgb == swatch.rgb
        }

        override fun hashCode(): Int {
            return 31 * rgb + population
        }
    }

    data class Builder private constructor(
        var swatches: List<Swatch> = emptyList(),
        var bitmap: Bitmap? = null,
        val targets: MutableSet<Targets> = mutableSetOf(),
        var maxColors: Int = DEFAULT_CALCULATE_NUMBER_COLORS,
        val filters: MutableList<Filter> = arrayListOf(DEFAULT_FILTER),
//        var region: Rect? = null,
    ) {
        companion object {
            operator fun invoke(bitmap: Bitmap)
                = Builder(bitmap = bitmap)
            operator fun invoke(swatches: List<Swatch>)
                = Builder(swatches)
        }
        init {
            when {
                bitmap!=null-> {
                    if (bitmap!!.isNull) {
                        throw IllegalArgumentException("Bitmap is not valid")
                    }

                    swatches = emptyList()
                    // Add the default targets
                    targets.add(Targets.VibrantLight)
                    targets.add(Targets.Vibrant)
                    targets.add(Targets.VibrantDark)
                    targets.add(Targets.MutedLight)
                    targets.add(Targets.Muted)
                    targets.add(Targets.MutedDark)
                }
                swatches.isNotEmpty() -> {
                    bitmap = null
                }
            }
            if (bitmap==null && swatches.isEmpty()) {
                throw NotImplementedError()
            }
        }


        var resizeArea: Int = DEFAULT_RESIZE_BITMAP_AREA
            private set
        var resizeMaxDimension: Int = -1
            private set

        /**
         * Set the resize value when using a [org.jetbrains.skia.Bitmap] as the source.
         * If the bitmap's largest dimension is greater than the value specified, then the bitmap
         * will be resized so that its largest dimension matches `maxDimension`. If the
         * bitmap is smaller or equal, the original is used as-is.
         *
         * @param maxDimension the number of pixels that the max dimension should be scaled down to,
         * or any value <= 0 to disable resizing.
         */
        @Deprecated("Using {@link #resizeBitmapArea(int)} is preferred since it can handle\n" + "          abnormal aspect ratios more gracefully.\n" + "         \n" + "          ")
        fun resizeBitmapSize(maxDimension: Int) {
            resizeMaxDimension = maxDimension
            resizeArea = -1
        }

        /**
         * Set the resize value when using a [Bitmap] as the source.
         * If the bitmap's area is greater than the value specified, then the bitmap
         * will be resized so that its area matches `area`. If the
         * bitmap is smaller or equal, the original is used as-is.
         *
         *
         * This value has a large effect on the processing time. The larger the resized image is,
         * the greater time it will take to generate the palette. The smaller the image is, the
         * more detail is lost in the resulting image and thus less precision for color selection.
         *
         * @param area the number of pixels that the intermediary scaled down Bitmap should cover,
         * or any value <= 0 to disable resizing.
         */
        fun resizeBitmapArea(area: Int) {
            resizeArea = area
            resizeMaxDimension = -1
        }


//        /**
//         * Set a region of the bitmap to be used exclusively when calculating the palette.
//         *
//         * This only works when the original input is a [Bitmap].
//         *
//         * @param left The left side of the rectangle used for the region.
//         * @param top The top of the rectangle used for the region.
//         * @param right The right side of the rectangle used for the region.
//         * @param bottom The bottom of the rectangle used for the region.
//         */
//        fun setRegion(left: Float, top: Float,right: Float,bottom: Float) {
//            if (bitmap != null) {
//                if (region == null) region = Rect()
//                // Set the Rect to be initially the whole Bitmap
//                region!![0, 0, bitmap.width] = bitmap.height
//                // Now just get the intersection with the region
//                if (!region!!.intersect(left, top, right, bottom)) {
//                    throw IllegalArgumentException(
//                        "The given region must intersect with "
//                                + "the Bitmap's dimensions."
//                    )
//                }
//            }
//        }
        /**
         * Generate and return the [Palette] synchronously.
         */
        suspend fun generate(): Palette {
            val swatches: List<Swatch> = when {
                bitmap != null -> {
                    // We have a Bitmap so we need to use quantization to reduce the number of colors
                    // First we'll scale down the bitmap if needed
                    val bitmap = scaleBitmapDown(bitmap!!)
                    // Now generate a quantizer from the Bitmap
                    val quantizer = ColorCutQuantizer(
                        bitmap.readPixels()!!.map { it.toInt() }.toTypedArray(),
                        maxColors,
                        if (filters.isEmpty()) null else filters.toTypedArray()
                    )
                    // If created a new bitmap, recycle it
                    if (bitmap != this.bitmap) {
                        bitmap.reset()
                    }
                    quantizer.quantizedColors!!
                }
                swatches.isNotEmpty() -> {
                    // Else we're using the provided swatches
                    swatches
                }
                else -> {
                    // The constructors enforce either a bitmap or swatches are present.
                    throw AssertionError()
                }
            }
            // Now create a Palette instance
            val p = Palette(swatches, targets.toSet())
            // And make it generate itself
            p.generate()
            return p
        }
        /**
         * Scale the bitmap down as needed.
         */
        private fun scaleBitmapDown(bitmap: Bitmap): Bitmap {
            var scaleRatio = -1.0
            if (resizeArea > 0) {
                val bitmapArea = bitmap.width * bitmap.height
                if (bitmapArea > resizeArea) {
                    scaleRatio = Math.sqrt(resizeArea / bitmapArea.toDouble())
                }
            } else if (resizeMaxDimension > 0) {
                val maxDimension = Math.max(bitmap.width, bitmap.height)
                if (maxDimension > resizeMaxDimension) {
                    scaleRatio = resizeMaxDimension / maxDimension.toDouble()
                }
            }
            return if (scaleRatio <= 0) {
                // Scaling has been disabled or not needed so just return the Bitmap
                bitmap
            } else {
                val width = ceil(bitmap.width * scaleRatio).toInt()
                val height = ceil(bitmap.height * scaleRatio).toInt()
                Bitmap().apply {
                    setImageInfo(ImageInfo(width, height, bitmap.colorType, bitmap.alphaType))
                    allocPixels()

                    require(Image.makeFromBitmap(bitmap).scalePixels(peekPixels()!!, SamplingMode.DEFAULT,true)) {
                        "fail to create bitmap"
                    }
                }
            }
        }

    }

    /**
     * A Filter provides a mechanism for exercising fine-grained control over which colors
     * are valid within a resulting [Palette].
     */
    interface Filter {
        /**
         * Hook to allow clients to be able filter colors from resulting palette.
         *
         * @param rgb the color in RGB888.
         * @param hsl HSL representation of the color.
         *
         * @return true if the color is allowed, false if not.
         *
         * @see Builder.filters
         */
        fun isAllowed(rgb: Int, hsl: FloatArray): Boolean
    }

    companion object {
        const val DEFAULT_RESIZE_BITMAP_AREA = 112 * 112
        const val DEFAULT_CALCULATE_NUMBER_COLORS = 16
        const val MIN_CONTRAST_TITLE_TEXT = 3.0f
        const val MIN_CONTRAST_BODY_TEXT = 4.5f

        private const val BLACK_MAX_LIGHTNESS = 0.05f
        private const val WHITE_MIN_LIGHTNESS = 0.95f
        /**
         * The default filter.
         */
        val DEFAULT_FILTER: Filter = object : Filter {

            override fun isAllowed(rgb: Int, hsl: FloatArray): Boolean {
                return !isWhite(hsl) && !isBlack(hsl) && !isNearRedILine(hsl)
            }

            /**
             * @return true if the color represents a color which is close to black.
             */
            private fun isBlack(hslColor: FloatArray): Boolean {
                return hslColor[2] <= BLACK_MAX_LIGHTNESS
            }

            /**
             * @return true if the color represents a color which is close to white.
             */
            private fun isWhite(hslColor: FloatArray): Boolean {
                return hslColor[2] >= WHITE_MIN_LIGHTNESS
            }

            /**
             * @return true if the color lies close to the red side of the I line.
             */
            private fun isNearRedILine(hslColor: FloatArray): Boolean {
                return (hslColor[0] >= 10f) && (hslColor.get(
                    0
                ) <= 37f) && (hslColor[1] <= 0.82f)
            }
        }
    }
}