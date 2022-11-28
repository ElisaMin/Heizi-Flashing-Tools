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

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.SparseBooleanArray
import androidx.collection.SimpleArrayMap
import androidx.compose.ui.graphics.Color
import androidx.core.util.Preconditions
import androidx.palette.graphics.Palette.Builder
import androidx.palette.graphics.Palette.PaletteAsyncListener
import java.util.Arrays
import java.util.Collections
import kotlin.math.abs

@Deprecated("not this shit", replaceWith = ReplaceWith(""))
annotation class ColorInt
@Deprecated("not this shit")
annotation class Px


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
    private val mSwatches: List<Swatch>,
    private val mTargets: List<Target>
) {
    /**
     * Listener to be used with [.generateAsync] or
     * [.generateAsync]
     */
    interface PaletteAsyncListener {
        /**
         * Called when the [Palette] has been generated. `null` will be passed when an
         * error occurred during generation.
         */
        fun onGenerated(palette: Palette?)
    }

    private val mSelectedSwatches: SimpleArrayMap<Target, Swatch?>
    private val mUsedColors: SparseBooleanArray

    /**
     * Returns the dominant swatch from the palette.
     *
     *
     * The dominant swatch is defined as the swatch with the greatest population (frequency)
     * within the palette.
     */
    val dominantSwatch: Swatch?

    /**
     * Returns all of the swatches which make up the palette.
     */
    val swatches: List<Swatch>
        get() = Collections.unmodifiableList(mSwatches)

    /**
     * Returns the targets used to generate this palette.
     */
    val targets: List<Target>
        get() = Collections.unmodifiableList(mTargets)

    /**
     * Returns the most vibrant swatch in the palette. Might be null.
     *
     * @see Target.VIBRANT
     */
    val vibrantSwatch: Swatch?
        get() = getSwatchForTarget(Target.VIBRANT)

    /**
     * Returns a light and vibrant swatch from the palette. Might be null.
     *
     * @see Target.LIGHT_VIBRANT
     */
    val lightVibrantSwatch: Swatch?
        get() = getSwatchForTarget(Target.LIGHT_VIBRANT)

    /**
     * Returns a dark and vibrant swatch from the palette. Might be null.
     *
     * @see Target.DARK_VIBRANT
     */
    val darkVibrantSwatch: Swatch?
        get() = getSwatchForTarget(Target.DARK_VIBRANT)

    /**
     * Returns a muted swatch from the palette. Might be null.
     *
     * @see Target.MUTED
     */
    val mutedSwatch: Swatch?
        get() = getSwatchForTarget(Target.MUTED)

    /**
     * Returns a muted and light swatch from the palette. Might be null.
     *
     * @see Target.LIGHT_MUTED
     */
    val lightMutedSwatch: Swatch?
        get() = getSwatchForTarget(Target.LIGHT_MUTED)

    /**
     * Returns a muted and dark swatch from the palette. Might be null.
     *
     * @see Target.DARK_MUTED
     */
    val darkMutedSwatch: Swatch?
        get() = getSwatchForTarget(Target.DARK_MUTED)

    /**
     * Returns the most vibrant color in the palette as an RGB packed int.
     *
     * @param defaultColor value to return if the swatch isn't available
     * @see .getVibrantSwatch
     */
    fun getVibrantColor(defaultColor: Int): Int {
        return getColorForTarget(Target.VIBRANT, defaultColor)
    }

    /**
     * Returns a light and vibrant color from the palette as an RGB packed int.
     *
     * @param defaultColor value to return if the swatch isn't available
     * @see .getLightVibrantSwatch
     */
    fun getLightVibrantColor(defaultColor: Int): Int {
        return getColorForTarget(Target.LIGHT_VIBRANT, defaultColor)
    }

    /**
     * Returns a dark and vibrant color from the palette as an RGB packed int.
     *
     * @param defaultColor value to return if the swatch isn't available
     * @see .getDarkVibrantSwatch
     */
    fun getDarkVibrantColor(defaultColor: Int): Int {
        return getColorForTarget(Target.DARK_VIBRANT, defaultColor)
    }

    /**
     * Returns a muted color from the palette as an RGB packed int.
     *
     * @param defaultColor value to return if the swatch isn't available
     * @see .getMutedSwatch
     */
    fun getMutedColor(defaultColor: Int): Int {
        return getColorForTarget(Target.MUTED, defaultColor)
    }

    /**
     * Returns a muted and light color from the palette as an RGB packed int.
     *
     * @param defaultColor value to return if the swatch isn't available
     * @see .getLightMutedSwatch
     */
    fun getLightMutedColor(defaultColor: Int): Int {
        return getColorForTarget(Target.LIGHT_MUTED, defaultColor)
    }

    /**
     * Returns a muted and dark color from the palette as an RGB packed int.
     *
     * @param defaultColor value to return if the swatch isn't available
     * @see .getDarkMutedSwatch
     */
    fun getDarkMutedColor(defaultColor: Int): Int {
        return getColorForTarget(Target.DARK_MUTED, defaultColor)
    }

    /**
     * Returns the selected swatch for the given target from the palette, or `null` if one
     * could not be found.
     */
    fun getSwatchForTarget(target: Target): Swatch? {
        return mSelectedSwatches[target]
    }

    /**
     * Returns the selected color for the given target from the palette as an RGB packed int.
     *
     * @param defaultColor value to return if the swatch isn't available
     */
    fun getColorForTarget(target: Target, defaultColor: Int): Int {
        val swatch = getSwatchForTarget(target)
        return swatch?.rgb ?: defaultColor
    }

    /**
     * Returns the color of the dominant swatch from the palette, as an RGB packed int.
     *
     * @param defaultColor value to return if the swatch isn't available
     * @see .getDominantSwatch
     */
    fun getDominantColor(defaultColor: Int): Int {
        return dominantSwatch?.rgb ?: defaultColor
    }

    fun  // TODO(b/141959297): Suppressed during upgrade to AGP 3.6.
            generate() {
        // We need to make sure that the scored targets are generated first. This is so that
        // inherited targets have something to inherit from
        var i = 0
        val count = mTargets!!.size
        while (i < count) {
            val target = mTargets[i]
            target.normalizeWeights()
            mSelectedSwatches.put(target, generateScoredTarget(target))
            i++
        }
        // We now clear out the used colors
        mUsedColors.clear()
    }

    private fun generateScoredTarget(target: Target): Swatch? {
        val maxScoreSwatch = getMaxScoredSwatchForTarget(target)
        if (maxScoreSwatch != null && target.isExclusive) {
            // If we have a swatch, and the target is exclusive, add the color to the used list
            mUsedColors.append(maxScoreSwatch.rgb, true)
        }
        return maxScoreSwatch
    }

    private fun getMaxScoredSwatchForTarget(target: Target): Swatch? {
        var maxScore = 0f
        var maxScoreSwatch: Swatch? = null
        var i = 0
        val count = mSwatches.size
        while (i < count) {
            val swatch = mSwatches[i]
            if (shouldBeScoredForTarget(swatch, target)) {
                val score = generateScore(swatch, target)
                if (maxScoreSwatch == null || score > maxScore) {
                    maxScoreSwatch = swatch
                    maxScore = score
                }
            }
            i++
        }
        return maxScoreSwatch
    }

    private fun shouldBeScoredForTarget(swatch: Swatch, target: Target): Boolean {
        // Check whether the HSL values are within the correct ranges, and this color hasn't
        // been used yet.
        val hsl = swatch.hsl
        return ((hsl[1] >= target.minimumSaturation && hsl[1] <= target.maximumSaturation) && hsl[2] >= target.minimumLightness) && hsl[2] <= target.maximumLightness && !mUsedColors[swatch.rgb]
    }

    private fun generateScore(swatch: Swatch, target: Target): Float {
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
        val count = mSwatches.size
        while (i < count) {
            val swatch = mSwatches[i]
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
    class Swatch(color: Int, population: Int) {
        private val mRed: Int
        private val mGreen: Int
        private val mBlue: Int

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
                ColorUtils.RGBToHSL(mRed, mGreen, mBlue, mHsl!!)
                return mHsl
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
                val lightBodyAlpha = ColorUtils.calculateMinimumAlpha(
                    Color.WHITE, rgb, MIN_CONTRAST_BODY_TEXT
                )
                val lightTitleAlpha = ColorUtils.calculateMinimumAlpha(
                    Color.WHITE, rgb, MIN_CONTRAST_TITLE_TEXT
                )
                if (lightBodyAlpha != -1 && lightTitleAlpha != -1) {
                    // If we found valid light values, use them and return
                    mBodyTextColor = ColorUtils.setAlphaComponent(Color.WHITE, lightBodyAlpha)
                    mTitleTextColor = ColorUtils.setAlphaComponent(Color.WHITE, lightTitleAlpha)
                    mGeneratedTextColors = true
                    return
                }
                val darkBodyAlpha = ColorUtils.calculateMinimumAlpha(
                    Color.BLACK, rgb, MIN_CONTRAST_BODY_TEXT
                )
                val darkTitleAlpha = ColorUtils.calculateMinimumAlpha(
                    Color.BLACK, rgb, MIN_CONTRAST_TITLE_TEXT
                )
                if (darkBodyAlpha != -1 && darkTitleAlpha != -1) {
                    // If we found valid dark values, use them and return
                    mBodyTextColor = ColorUtils.setAlphaComponent(Color.BLACK, darkBodyAlpha)
                    mTitleTextColor = ColorUtils.setAlphaComponent(Color.BLACK, darkTitleAlpha)
                    mGeneratedTextColors = true
                    return
                }
                // If we reach here then we can not find title and body values which use the same
                // lightness, we need to use mismatched values
                mBodyTextColor = if (lightBodyAlpha != -1) ColorUtils.setAlphaComponent(
                    Color.WHITE,
                    lightBodyAlpha
                ) else ColorUtils.setAlphaComponent(Color.BLACK, darkBodyAlpha)
                mTitleTextColor = if (lightTitleAlpha != -1) ColorUtils.setAlphaComponent(
                    Color.WHITE,
                    lightTitleAlpha
                ) else ColorUtils.setAlphaComponent(Color.BLACK, darkTitleAlpha)
                mGeneratedTextColors = true
            }
        }

        override fun toString(): String {
            return StringBuilder(javaClass.simpleName)
                .append(" [RGB: #").append(Integer.toHexString(rgb)).append(']')
                .append(" [HSL: ").append(Arrays.toString(hsl)).append(']')
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

    /**
     * Builder class for generating [Palette] instances.
     */
    class Builder {
        private val mSwatches: List<Swatch>?
        private val mBitmap: Bitmap?
        private val mTargets: MutableList<Target>? = ArrayList()
        private var mMaxColors = DEFAULT_CALCULATE_NUMBER_COLORS
        private var mResizeArea = DEFAULT_RESIZE_BITMAP_AREA
        private var mResizeMaxDimension = -1
        private val mFilters: MutableList<Filter> = ArrayList()
        private var mRegion: Rect? = null

        /**
         * Construct a new [Builder] using a source [Bitmap]
         */
        constructor(bitmap: Bitmap) {
            if (bitmap == null || bitmap.isRecycled) {
                throw IllegalArgumentException("Bitmap is not valid")
            }
            mFilters.add(DEFAULT_FILTER)
            mBitmap = bitmap
            mSwatches = null
            // Add the default targets
            mTargets!!.add(Target.LIGHT_VIBRANT)
            mTargets.add(Target.VIBRANT)
            mTargets.add(Target.DARK_VIBRANT)
            mTargets.add(Target.LIGHT_MUTED)
            mTargets.add(Target.MUTED)
            mTargets.add(Target.DARK_MUTED)
        }

        /**
         * Construct a new [Builder] using a list of [Swatch] instances.
         * Typically only used for testing.
         */
        constructor(swatches: List<Swatch>) {
            if (swatches.isEmpty()) {
                throw IllegalArgumentException("List of Swatches is not valid")
            }
            mFilters.add(DEFAULT_FILTER)
            mSwatches = swatches
            mBitmap = null
        }

        /**
         * Set the maximum number of colors to use in the quantization step when using a
         * [android.graphics.Bitmap] as the source.
         *
         *
         * Good values for depend on the source image type. For landscapes, good values are in
         * the range 10-16. For images which are largely made up of people's faces then this
         * value should be increased to ~24.
         */
        fun maximumColorCount(colors: Int): Builder {
            mMaxColors = colors
            return this
        }

        /**
         * Set the resize value when using a [android.graphics.Bitmap] as the source.
         * If the bitmap's largest dimension is greater than the value specified, then the bitmap
         * will be resized so that its largest dimension matches `maxDimension`. If the
         * bitmap is smaller or equal, the original is used as-is.
         *
         * @param maxDimension the number of pixels that the max dimension should be scaled down to,
         * or any value <= 0 to disable resizing.
         */
        @Deprecated("Using {@link #resizeBitmapArea(int)} is preferred since it can handle\n" + "          abnormal aspect ratios more gracefully.\n" + "         \n" + "          ")
        fun resizeBitmapSize(maxDimension: Int): Builder {
            mResizeMaxDimension = maxDimension
            mResizeArea = -1
            return this
        }

        /**
         * Set the resize value when using a [android.graphics.Bitmap] as the source.
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
        fun resizeBitmapArea(area: Int): Builder {
            mResizeArea = area
            mResizeMaxDimension = -1
            return this
        }

        /**
         * Clear all added filters. This includes any default filters added automatically by
         * [Palette].
         */
        fun clearFilters(): Builder {
            mFilters.clear()
            return this
        }

        /**
         * Add a filter to be able to have fine grained control over which colors are
         * allowed in the resulting palette.
         *
         * @param filter filter to add.
         */
        fun addFilter(filter: Filter): Builder {
            mFilters.add(filter)
            return this
        }

        /**
         * Set a region of the bitmap to be used exclusively when calculating the palette.
         *
         * This only works when the original input is a [Bitmap].
         *
         * @param left The left side of the rectangle used for the region.
         * @param top The top of the rectangle used for the region.
         * @param right The right side of the rectangle used for the region.
         * @param bottom The bottom of the rectangle used for the region.
         */
        fun setRegion(@Px left: Int, @Px top: Int, @Px right: Int, @Px bottom: Int): Builder {
            if (mBitmap != null) {
                if (mRegion == null) mRegion = Rect()
                // Set the Rect to be initially the whole Bitmap
                mRegion!![0, 0, mBitmap.width] = mBitmap.height
                // Now just get the intersection with the region
                if (!mRegion!!.intersect(left, top, right, bottom)) {
                    throw IllegalArgumentException(
                        "The given region must intersect with "
                                + "the Bitmap's dimensions."
                    )
                }
            }
            return this
        }

        /**
         * Clear any previously region set via [.setRegion].
         */
        fun clearRegion(): Builder {
            mRegion = null
            return this
        }

        /**
         * Add a target profile to be generated in the palette.
         *
         *
         * You can retrieve the result via [Palette.getSwatchForTarget].
         */
        fun addTarget(target: Target): Builder {
            if (!mTargets!!.contains(target)) {
                mTargets.add(target)
            }
            return this
        }

        /**
         * Clear all added targets. This includes any default targets added automatically by
         * [Palette].
         */
        fun clearTargets(): Builder {
            mTargets?.clear()
            return this
        }

        /**
         * Generate and return the [Palette] synchronously.
         */
        fun generate(): Palette {
            val swatches: List<Swatch>
            if (mBitmap != null) {
                // We have a Bitmap so we need to use quantization to reduce the number of colors
                // First we'll scale down the bitmap if needed
                val bitmap = scaleBitmapDown(mBitmap)
                val region = mRegion
                if (bitmap != mBitmap && region != null) {
                    // If we have a scaled bitmap and a selected region, we need to scale down the
                    // region to match the new scale
                    val scale = bitmap.width / mBitmap.width.toDouble()
                    region.left = Math.floor(region.left * scale).toInt()
                    region.top = Math.floor(region.top * scale).toInt()
                    region.right = Math.min(
                        Math.ceil(region.right * scale).toInt(),
                        bitmap.width
                    )
                    region.bottom = Math.min(
                        Math.ceil(region.bottom * scale).toInt(),
                        bitmap.height
                    )
                }
                // Now generate a quantizer from the Bitmap
                val quantizer = ColorCutQuantizer(
                    getPixelsFromBitmap(bitmap),
                    mMaxColors,
                    if (mFilters.isEmpty()) null else mFilters.toTypedArray()
                )
                // If created a new bitmap, recycle it
                if (bitmap != mBitmap) {
                    bitmap.recycle()
                }
                swatches = quantizer.getQuantizedColors()
            } else if (mSwatches != null) {
                // Else we're using the provided swatches
                swatches = mSwatches
            } else {
                // The constructors enforce either a bitmap or swatches are present.
                throw AssertionError()
            }
            // Now create a Palette instance
            val p = mTargets?.let { Palette(swatches, it) }!!
            // And make it generate itself
            p.generate()
            return p
        }
        /**
         * Generate the [Palette] asynchronously. The provided listener's
         * [PaletteAsyncListener.onGenerated] method will be called with the palette when
         * generated.
         *
         */
        @Deprecated(
            "Use the standard <code>java.util.concurrent</code> or\n" + "          <a href=" https ://developer.android.com/topic/libraries/architecture/coroutines">\n" + "          Kotlin concurrency utilities</a> to call {@link #generate()} instead.")    fun /*@@mtbbih@@*/generate(
                    listener : PaletteAsyncListener
        ) : AsyncTask<Bitmap?, Void?, Palette?>
        {
            Preconditions.checkNotNull(listener)
            return object : AsyncTask<Bitmap?, Void?, Palette?>() {
                protected override fun doInBackground(vararg params: Bitmap): Palette? {
                    try {
                        return generate()
                    } catch (e: Exception) {
                        Log.e(LOG_TAG, "Exception thrown during async generate", e)
                        return null
                    }
                }

                override fun onPostExecute(colorExtractor: Palette?) {
                    listener.onGenerated(colorExtractor)
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mBitmap)
        }

        private fun getPixelsFromBitmap(bitmap: Bitmap): IntArray {
            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height
            val pixels = IntArray(bitmapWidth * bitmapHeight)
            bitmap.getPixels(pixels, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight)
            if (mRegion == null) {
                // If we don't have a region, return all of the pixels
                return pixels
            } else {
                // If we do have a region, lets create a subset array containing only the region's
                // pixels
                val regionWidth = mRegion!!.width()
                val regionHeight = mRegion!!.height()
                // pixels contains all of the pixels, so we need to iterate through each row and
                // copy the regions pixels into a new smaller array
                val subsetPixels = IntArray(regionWidth * regionHeight)
                for (row in 0 until regionHeight) {
                    System.arraycopy(
                        pixels, ((row + mRegion!!.top) * bitmapWidth) + mRegion!!.left,
                        subsetPixels, row * regionWidth, regionWidth
                    )
                }
                return subsetPixels
            }
        }

        /**
         * Scale the bitmap down as needed.
         */
        private fun scaleBitmapDown(bitmap: Bitmap): Bitmap {
            var scaleRatio = -1.0
            if (mResizeArea > 0) {
                val bitmapArea = bitmap.width * bitmap.height
                if (bitmapArea > mResizeArea) {
                    scaleRatio = Math.sqrt(mResizeArea / bitmapArea.toDouble())
                }
            } else if (mResizeMaxDimension > 0) {
                val maxDimension = Math.max(bitmap.width, bitmap.height)
                if (maxDimension > mResizeMaxDimension) {
                    scaleRatio = mResizeMaxDimension / maxDimension.toDouble()
                }
            }
            return if (scaleRatio <= 0) {
                // Scaling has been disabled or not needed so just return the Bitmap
                bitmap
            } else Bitmap.createScaledBitmap(
                bitmap,
                Math.ceil(bitmap.width * scaleRatio).toInt(),
                Math.ceil(bitmap.height * scaleRatio).toInt(),
                false
            )
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
         * @see Builder.addFilter
         */
        fun isAllowed(rgb: Int, hsl: FloatArray): Boolean
    }

    init {
        mUsedColors = SparseBooleanArray()
        mSelectedSwatches = SimpleArrayMap()
        dominantSwatch = findDominantSwatch()
    }

    companion object {
        const val DEFAULT_RESIZE_BITMAP_AREA = 112 * 112
        const val DEFAULT_CALCULATE_NUMBER_COLORS = 16
        const val MIN_CONTRAST_TITLE_TEXT = 3.0f
        const val MIN_CONTRAST_BODY_TEXT = 4.5f
        const val LOG_TAG = "Palette"
        const val LOG_TIMINGS = false

        /**
         * Start generating a [Palette] with the returned [Builder] instance.
         */
        fun from(bitmap: Bitmap): Builder {
            return Builder(bitmap)
        }

        /**
         * Generate a [Palette] from the pre-generated list of [Palette.Swatch] swatches.
         * This is useful for testing, or if you want to resurrect a [Palette] instance from a
         * list of swatches. Will return null if the `swatches` is null.
         */
        fun from(swatches: List<Swatch>): Palette {
            return Builder(swatches).generate()
        }

        @Deprecated("Use {@link Builder} to generate the Palette.")
        fun generate(bitmap: Bitmap): Palette {
            return from(bitmap).generate()
        }

        @Deprecated("Use {@link Builder} to generate the Palette.")
        fun generate(bitmap: Bitmap, numColors: Int): Palette {
            return from(bitmap).maximumColorCount(numColors).generate()
        }

        @Deprecated("Use {@link Builder} to generate the Palette.")
        fun generateAsync(
            bitmap: Bitmap, listener: PaletteAsyncListener
        ): AsyncTask<Bitmap, Void, Palette> {
            return from(bitmap).generate(listener)
        }

        @Deprecated("Use {@link Builder} to generate the Palette.")
        fun generateAsync(
            bitmap: Bitmap, numColors: Int, listener: PaletteAsyncListener
        ): AsyncTask<Bitmap, Void, Palette> {
            return from(bitmap).maximumColorCount(numColors).generate(listener)
        }

        /**
         * The default filter.
         */
        val DEFAULT_FILTER: Filter = object : Filter {
            private static
            val BLACK_MAX_LIGHTNESS = 0.05f
            private static
            val WHITE_MIN_LIGHTNESS = 0.95f
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