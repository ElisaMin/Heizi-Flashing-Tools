/*
 * Copyright 2015 The Android Open Source Project
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

import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.*


/**
 * A set of color-related utility methods, building upon those available in `Color`.
 */
object ColorUtils {
    private const val XYZ_WHITE_REFERENCE_X = 95.047
    private const val XYZ_WHITE_REFERENCE_Y = 100.0
    private const val XYZ_WHITE_REFERENCE_Z = 108.883
    private val XYZ_EPSILON = 0.008856
    private val XYZ_KAPPA = 903.3
    private val MIN_ALPHA_SEARCH_MAX_ITERATIONS = 10
    private val MIN_ALPHA_SEARCH_PRECISION = 1
    private val TEMP_ARRAY = ThreadLocal<DoubleArray>()

    /**
     * Composite two potentially translucent colors over each other and returns the result.
     */
    fun compositeColors(foreground: Int, background: Int): Int {
        val bgAlpha = Color.alpha(background)
        val fgAlpha = Color.alpha(foreground)
        val a = compositeAlpha(fgAlpha, bgAlpha)
        val r = compositeComponent(
            Color.red(foreground), fgAlpha,
            Color.red(background), bgAlpha, a
        )
        val g = compositeComponent(
            Color.green(foreground), fgAlpha,
            Color.green(background), bgAlpha, a
        )
        val b = compositeComponent(
            Color.blue(foreground), fgAlpha,
            Color.blue(background), bgAlpha, a
        )
        return Color.argb(a, r, g, b)
    }

    /**
     * Composites two translucent colors together. More specifically, adds two colors using
     * the [source over][android.graphics.PorterDuff.Mode.SRC_OVER] blending mode. The
     * colors must not be pre-multiplied and the result is a non pre-multiplied color.
     *
     *
     * If the two colors have different color spaces, the foreground color is converted to the
     * color space of the background color.
     *
     *
     * The following example creates a purple color by blending opaque blue with
     * semi-translucent red:
     *
     * <pre>`Color purple = ColorUtils.compositeColors(
     * Color.valueOf(1f, 0f, 0f, 0.5f),
     * Color.valueOf(0f, 0f, 1f));
    `</pre> *
     *
     * *Note:* This method requires API 26 or newer.
     *
     * @throws IllegalArgumentException if the
     * [models][android.graphics.Color.getModel] of the colors do not match
     */

    fun compositeColors(foreground: Color, background: Color): Color {
        return Api26Impl.compositeColors(foreground, background)
    }

    private fun compositeAlpha(foregroundAlpha: Int, backgroundAlpha: Int): Int {
        return 0xFF - (0xFF - backgroundAlpha) * (0xFF - foregroundAlpha) / 0xFF
    }

    private fun compositeComponent(fgC: Int, fgA: Int, bgC: Int, bgA: Int, a: Int): Int {
        return if (a == 0) 0 else ((0xFF * fgC * fgA) + (bgC * bgA * (0xFF - fgA))) / (a * 0xFF)
    }

    /**
     * Returns the luminance of a color as a float between `0.0` and `1.0`.
     *
     * Defined as the Y component in the XYZ representation of `color`.
     */

    fun calculateLuminance(color: Int): Double {
        val result = tempDouble3Array
        colorToXYZ(color, result)
        // Luminance is the Y component
        return result[1] / 100
    }

    /**
     * Returns the contrast ratio between `foreground` and `background`.
     * `background` must be opaque.
     *
     *
     * Formula defined
     * [here](http://www.w3.org/TR/2008/REC-WCAG20-20081211/#contrast-ratiodef).
     */
    fun calculateContrast(foreground: Int, background: Int): Double {
        var foreground = foreground
        if (Color.alpha(background) != 255) {
            throw IllegalArgumentException(
                "background can not be translucent: #"
                        + Integer.toHexString(background)
            )
        }
        if (Color.alpha(foreground) < 255) {
            // If the foreground is translucent, composite the foreground over the background
            foreground = compositeColors(foreground, background)
        }
        val luminance1 = calculateLuminance(foreground) + 0.05
        val luminance2 = calculateLuminance(background) + 0.05

        // Now return the lighter luminance divided by the darker luminance
        return Math.max(luminance1, luminance2) / Math.min(luminance1, luminance2)
    }

    /**
     * Calculates the minimum alpha value which can be applied to `foreground` so that would
     * have a contrast value of at least `minContrastRatio` when compared to
     * `background`.
     *
     * @param foreground the foreground color
     * @param background the opaque background color
     * @param minContrastRatio the minimum contrast ratio
     * @return the alpha value in the range [0, 255] or -1 if no value could be calculated
     */
    fun calculateMinimumAlpha(
        foreground: Int, background: Int,
        minContrastRatio: Float
    ): Int {
        if (Color.alpha(background) != 255) {
            throw IllegalArgumentException(
                ("background can not be translucent: #"
                        + Integer.toHexString(background))
            )
        }

        // First lets check that a fully opaque foreground has sufficient contrast
        var testForeground = setAlphaComponent(foreground, 255)
        var testRatio = calculateContrast(testForeground, background)
        if (testRatio < minContrastRatio) {
            // Fully opaque foreground does not have sufficient contrast, return error
            return -1
        }

        // Binary search to find a value with the minimum value which provides sufficient contrast
        var numIterations = 0
        var minAlpha = 0
        var maxAlpha = 255
        while (numIterations <= MIN_ALPHA_SEARCH_MAX_ITERATIONS &&
            (maxAlpha - minAlpha) > MIN_ALPHA_SEARCH_PRECISION
        ) {
            val testAlpha = (minAlpha + maxAlpha) / 2
            testForeground = setAlphaComponent(foreground, testAlpha)
            testRatio = calculateContrast(testForeground, background)
            if (testRatio < minContrastRatio) {
                minAlpha = testAlpha
            } else {
                maxAlpha = testAlpha
            }
            numIterations++
        }

        // Conservatively return the max of the range of possible alphas, which is known to pass.
        return maxAlpha
    }

    /**
     * Convert RGB components to HSL (hue-saturation-lightness).
     *
     *  * outHsl[0] is Hue [0, 360)
     *  * outHsl[1] is Saturation [0, 1]
     *  * outHsl[2] is Lightness [0, 1]
     *
     *
     * @param r red component value [0, 255]
     * @param g green component value [0, 255]
     * @param b blue component value [0, 255]
     * @param outHsl 3-element array which holds the resulting HSL components
     */
    fun RGBToHSL(
        r: Int,
        g: Int,  b: Int,
        outHsl: FloatArray
    ) {
        val rf = r / 255f
        val gf = g / 255f
        val bf = b / 255f
        val max = Math.max(rf, Math.max(gf, bf))
        val min = Math.min(rf, Math.min(gf, bf))
        val deltaMaxMin = max - min
        var h: Float
        val s: Float
        val l = (max + min) / 2f
        if (max == min) {
            // Monochromatic
            s = 0f
            h = s
        } else {
            if (max == rf) {
                h = ((gf - bf) / deltaMaxMin) % 6f
            } else if (max == gf) {
                h = ((bf - rf) / deltaMaxMin) + 2f
            } else {
                h = ((rf - gf) / deltaMaxMin) + 4f
            }
            s = deltaMaxMin / (1f - Math.abs(2f * l - 1f))
        }
        h = (h * 60f) % 360f
        if (h < 0) {
            h += 360f
        }
        outHsl[0] = constrain(h, 0f, 360f)
        outHsl[1] = constrain(s, 0f, 1f)
        outHsl[2] = constrain(l, 0f, 1f)
    }

    /**
     * Convert the ARGB color to its HSL (hue-saturation-lightness) components.
     *
     *  * outHsl[0] is Hue [0, 360)
     *  * outHsl[1] is Saturation [0, 1]
     *  * outHsl[2] is Lightness [0, 1]
     *
     *
     * @param color the ARGB color to convert. The alpha component is ignored
     * @param outHsl 3-element array which holds the resulting HSL components
     */
    fun colorToHSL(color: Int, outHsl: FloatArray) {
        RGBToHSL(Color.red(color), Color.green(color), Color.blue(color), outHsl)
    }

    /**
     * Convert HSL (hue-saturation-lightness) components to a RGB color.
     *
     *  * hsl[0] is Hue [0, 360)
     *  * hsl[1] is Saturation [0, 1]
     *  * hsl[2] is Lightness [0, 1]
     *
     * If hsv values are out of range, they are pinned.
     *
     * @param hsl 3-element array which holds the input HSL components
     * @return the resulting RGB color
     */

    fun HSLToColor(hsl: FloatArray): Int {
        val h = hsl[0]
        val s = hsl[1]
        val l = hsl[2]
        val c = (1f - Math.abs(2 * l - 1f)) * s
        val m = l - 0.5f * c
        val x = c * (1f - Math.abs((h / 60f % 2f) - 1f))
        val hueSegment = h.toInt() / 60
        var r = 0
        var g = 0
        var b = 0
        when (hueSegment) {
            0 -> {
                r = Math.round(255 * (c + m))
                g = Math.round(255 * (x + m))
                b = Math.round(255 * m)
            }

            1 -> {
                r = Math.round(255 * (x + m))
                g = Math.round(255 * (c + m))
                b = Math.round(255 * m)
            }

            2 -> {
                r = Math.round(255 * m)
                g = Math.round(255 * (c + m))
                b = Math.round(255 * (x + m))
            }

            3 -> {
                r = Math.round(255 * m)
                g = Math.round(255 * (x + m))
                b = Math.round(255 * (c + m))
            }

            4 -> {
                r = Math.round(255 * (x + m))
                g = Math.round(255 * m)
                b = Math.round(255 * (c + m))
            }

            5, 6 -> {
                r = Math.round(255 * (c + m))
                g = Math.round(255 * m)
                b = Math.round(255 * (x + m))
            }
        }
        r = constrain(r, 0, 255)
        g = constrain(g, 0, 255)
        b = constrain(b, 0, 255)
        return Color.rgb(r, g, b)
    }

    /**
     * Set the alpha component of `color` to be `alpha`.
     */

    fun setAlphaComponent(
        color: Int,
        alpha: Int
    ): Int {
        if (alpha < 0 || alpha > 255) {
            throw IllegalArgumentException("alpha must be between 0 and 255.")
        }
        return (color and 0x00ffffff) or (alpha shl 24)
    }

    /**
     * Convert the ARGB color to its CIE Lab representative components.
     *
     * @param color the ARGB color to convert. The alpha component is ignored
     * @param outLab 3-element array which holds the resulting LAB components
     */
    fun colorToLAB(color: Int, outLab: DoubleArray) {
        RGBToLAB(Color.red(color), Color.green(color), Color.blue(color), outLab)
    }

    /**
     * Convert RGB components to its CIE Lab representative components.
     *
     *
     *  * outLab[0] is L [0, 100]
     *  * outLab[1] is a [-128, 127)
     *  * outLab[2] is b [-128, 127)
     *
     *
     * @param r red component value [0, 255]
     * @param g green component value [0, 255]
     * @param b blue component value [0, 255]
     * @param outLab 3-element array which holds the resulting LAB components
     */
    fun RGBToLAB(
        r: Int,
        g: Int,  b: Int,
        outLab: DoubleArray
    ) {
        // First we convert RGB to XYZ
        RGBToXYZ(r, g, b, outLab)
        // outLab now contains XYZ
        XYZToLAB(outLab[0], outLab[1], outLab[2], outLab)
        // outLab now contains LAB representation
    }

    /**
     * Convert the ARGB color to its CIE XYZ representative components.
     *
     *
     * The resulting XYZ representation will use the D65 illuminant and the CIE
     * 2° Standard Observer (1931).
     *
     *
     *  * outXyz[0] is X [0, 95.047)
     *  * outXyz[1] is Y [0, 100)
     *  * outXyz[2] is Z [0, 108.883)
     *
     *
     * @param color the ARGB color to convert. The alpha component is ignored
     * @param outXyz 3-element array which holds the resulting LAB components
     */
    fun colorToXYZ(color: Int, outXyz: DoubleArray) {
        RGBToXYZ(Color.red(color), Color.green(color), Color.blue(color), outXyz)
    }

    /**
     * Convert RGB components to its CIE XYZ representative components.
     *
     *
     * The resulting XYZ representation will use the D65 illuminant and the CIE
     * 2° Standard Observer (1931).
     *
     *
     *  * outXyz[0] is X [0, 95.047)
     *  * outXyz[1] is Y [0, 100)
     *  * outXyz[2] is Z [0, 108.883)
     *
     *
     * @param r red component value [0, 255]
     * @param g green component value [0, 255]
     * @param b blue component value [0, 255]
     * @param outXyz 3-element array which holds the resulting XYZ components
     */
    fun RGBToXYZ(
        r: Int,
        g: Int,  b: Int,
        outXyz: DoubleArray
    ) {
        if (outXyz.size != 3) {
            throw IllegalArgumentException("outXyz must have a length of 3.")
        }
        var sr = r / 255.0
        sr = if (sr < 0.04045) sr / 12.92 else Math.pow((sr + 0.055) / 1.055, 2.4)
        var sg = g / 255.0
        sg = if (sg < 0.04045) sg / 12.92 else Math.pow((sg + 0.055) / 1.055, 2.4)
        var sb = b / 255.0
        sb = if (sb < 0.04045) sb / 12.92 else Math.pow((sb + 0.055) / 1.055, 2.4)
        outXyz[0] = 100 * ((sr * 0.4124) + (sg * 0.3576) + (sb * 0.1805))
        outXyz[1] = 100 * ((sr * 0.2126) + (sg * 0.7152) + (sb * 0.0722))
        outXyz[2] = 100 * ((sr * 0.0193) + (sg * 0.1192) + (sb * 0.9505))
    }

    /**
     * Converts a color from CIE XYZ to CIE Lab representation.
     *
     *
     * This method expects the XYZ representation to use the D65 illuminant and the CIE
     * 2° Standard Observer (1931).
     *
     *
     *  * outLab[0] is L [0, 100]
     *  * outLab[1] is a [-128, 127)
     *  * outLab[2] is b [-128, 127)
     *
     *
     * @param x X component value [0, 95.047)
     * @param y Y component value [0, 100)
     * @param z Z component value [0, 108.883)
     * @param outLab 3-element array which holds the resulting Lab components
     */
    fun XYZToLAB(
        x: Double,
        y: Double,
        z: Double,
        outLab: DoubleArray
    ) {
        var x = x
        var y = y
        var z = z
        if (outLab.size != 3) {
            throw IllegalArgumentException("outLab must have a length of 3.")
        }
        x = pivotXyzComponent(x / XYZ_WHITE_REFERENCE_X)
        y = pivotXyzComponent(y / XYZ_WHITE_REFERENCE_Y)
        z = pivotXyzComponent(z / XYZ_WHITE_REFERENCE_Z)
        outLab[0] = Math.max(0.0, 116 * y - 16)
        outLab[1] = 500 * (x - y)
        outLab[2] = 200 * (y - z)
    }

    /**
     * Converts a color from CIE Lab to CIE XYZ representation.
     *
     *
     * The resulting XYZ representation will use the D65 illuminant and the CIE
     * 2° Standard Observer (1931).
     *
     *
     *  * outXyz[0] is X [0, 95.047)
     *  * outXyz[1] is Y [0, 100)
     *  * outXyz[2] is Z [0, 108.883)
     *
     *
     * @param l L component value [0, 100]
     * @param a A component value [-128, 127)
     * @param b B component value [-128, 127)
     * @param outXyz 3-element array which holds the resulting XYZ components
     */
    fun LABToXYZ(
        l: Double,
        a: Double,
        b: Double,
        outXyz: DoubleArray
    ) {
        val fy = (l + 16) / 116
        val fx = a / 500 + fy
        val fz = fy - b / 200
        var tmp = Math.pow(fx, 3.0)
        val xr = if (tmp > XYZ_EPSILON) tmp else (116 * fx - 16) / XYZ_KAPPA
        val yr = if (l > XYZ_KAPPA * XYZ_EPSILON) Math.pow(fy, 3.0) else l / XYZ_KAPPA
        tmp = Math.pow(fz, 3.0)
        val zr = if (tmp > XYZ_EPSILON) tmp else (116 * fz - 16) / XYZ_KAPPA
        outXyz[0] = xr * XYZ_WHITE_REFERENCE_X
        outXyz[1] = yr * XYZ_WHITE_REFERENCE_Y
        outXyz[2] = zr * XYZ_WHITE_REFERENCE_Z
    }

    /**
     * Converts a color from CIE XYZ to its RGB representation.
     *
     *
     * This method expects the XYZ representation to use the D65 illuminant and the CIE
     * 2° Standard Observer (1931).
     *
     * @param x X component value [0, 95.047)
     * @param y Y component value [0, 100)
     * @param z Z component value [0, 108.883)
     * @return int containing the RGB representation
     */

    fun XYZToColor(
        x: Double,
        y: Double,
        z: Double
    ): Int {
        var r = ((x * 3.2406) + (y * -1.5372) + (z * -0.4986)) / 100
        var g = ((x * -0.9689) + (y * 1.8758) + (z * 0.0415)) / 100
        var b = ((x * 0.0557) + (y * -0.2040) + (z * 1.0570)) / 100
        r = if (r > 0.0031308) 1.055 * Math.pow(r, 1 / 2.4) - 0.055 else 12.92 * r
        g = if (g > 0.0031308) 1.055 * Math.pow(g, 1 / 2.4) - 0.055 else 12.92 * g
        b = if (b > 0.0031308) 1.055 * Math.pow(b, 1 / 2.4) - 0.055 else 12.92 * b
        return Color.rgb(
            constrain(Math.round(r * 255).toInt(), 0, 255),
            constrain(Math.round(g * 255).toInt(), 0, 255),
            constrain(Math.round(b * 255).toInt(), 0, 255)
        )
    }

    /**
     * Converts a color from CIE Lab to its RGB representation.
     *
     * @param l L component value [0, 100]
     * @param a A component value [-128, 127]
     * @param b B component value [-128, 127]
     * @return int containing the RGB representation
     */

    fun LABToColor(
        l: Double,
        a: Double,
        b: Double
    ): Int {
        val result = tempDouble3Array
        LABToXYZ(l, a, b, result)
        return XYZToColor(result[0], result[1], result[2])
    }

    /**
     * Returns the euclidean distance between two LAB colors.
     */
    fun distanceEuclidean(labX: DoubleArray, labY: DoubleArray): Double {
        return Math.sqrt(
            (Math.pow(labX[0] - labY[0], 2.0)
                    + Math.pow(labX[1] - labY[1], 2.0)
                    + Math.pow(labX[2] - labY[2], 2.0))
        )
    }

    private fun constrain(amount: Float, low: Float, high: Float): Float {
        return if (amount < low) low else Math.min(amount, high)
    }

    private fun constrain(amount: Int, low: Int, high: Int): Int {
        return if (amount < low) low else Math.min(amount, high)
    }

    private fun pivotXyzComponent(component: Double): Double {
        return if (component > XYZ_EPSILON) Math.pow(
            component,
            1 / 3.0
        ) else (XYZ_KAPPA * component + 16) / 116
    }

    /**
     * Blend between two ARGB colors using the given ratio.
     *
     *
     * A blend ratio of 0.0 will result in `color1`, 0.5 will give an even blend,
     * 1.0 will result in `color2`.
     *
     * @param color1 the first ARGB color
     * @param color2 the second ARGB color
     * @param ratio the blend ratio of `color1` to `color2`
     */

    fun blendARGB(
        color1: Int, color2: Int,
        ratio: Float
    ): Int {
        val inverseRatio = 1 - ratio
        val a = Color.alpha(color1) * inverseRatio + Color(color2) * ratio
        val r = Color.red(color1) * inverseRatio + Color.red(color2) * ratio
        val g = Color.green(color1) * inverseRatio + Color.green(color2) * ratio
        val b = Color.blue(color1) * inverseRatio + Color.blue(color2) * ratio
        return Color.argb(a.toInt(), r.toInt(), g.toInt(), b.toInt())
    }

    /**
     * Blend between `hsl1` and `hsl2` using the given ratio. This will interpolate
     * the hue using the shortest angle.
     *
     *
     * A blend ratio of 0.0 will result in `hsl1`, 0.5 will give an even blend,
     * 1.0 will result in `hsl2`.
     *
     * @param hsl1 3-element array which holds the first HSL color
     * @param hsl2 3-element array which holds the second HSL color
     * @param ratio the blend ratio of `hsl1` to `hsl2`
     * @param outResult 3-element array which holds the resulting HSL components
     */
    fun blendHSL(
        hsl1: FloatArray, hsl2: FloatArray,
        ratio: Float, outResult: FloatArray
    ) {
        if (outResult.size != 3) {
            throw IllegalArgumentException("result must have a length of 3.")
        }
        val inverseRatio = 1 - ratio
        // Since hue is circular we will need to interpolate carefully
        outResult[0] = circularInterpolate(hsl1[0], hsl2[0], ratio)
        outResult[1] = hsl1[1] * inverseRatio + hsl2[1] * ratio
        outResult[2] = hsl1[2] * inverseRatio + hsl2[2] * ratio
    }

    /**
     * Blend between two CIE-LAB colors using the given ratio.
     *
     *
     * A blend ratio of 0.0 will result in `lab1`, 0.5 will give an even blend,
     * 1.0 will result in `lab2`.
     *
     * @param lab1 3-element array which holds the first LAB color
     * @param lab2 3-element array which holds the second LAB color
     * @param ratio the blend ratio of `lab1` to `lab2`
     * @param outResult 3-element array which holds the resulting LAB components
     */
    fun blendLAB(
        lab1: DoubleArray, lab2: DoubleArray,
        ratio: Double, outResult: DoubleArray
    ) {
        if (outResult.size != 3) {
            throw IllegalArgumentException("outResult must have a length of 3.")
        }
        val inverseRatio = 1 - ratio
        outResult[0] = lab1[0] * inverseRatio + lab2[0] * ratio
        outResult[1] = lab1[1] * inverseRatio + lab2[1] * ratio
        outResult[2] = lab1[2] * inverseRatio + lab2[2] * ratio
    }


    fun circularInterpolate(a: Float, b: Float, f: Float): Float {
        var a = a
        var b = b
        if (Math.abs(b - a) > 180) {
            if (b > a) {
                a += 360f
            } else {
                b += 360f
            }
        }
        return (a + ((b - a) * f)) % 360
    }

    private val tempDouble3Array: DoubleArray
        private get() {
            var result = TEMP_ARRAY.get()
            if (result == null) {
                result = DoubleArray(3)
                TEMP_ARRAY.set(result)
            }
            return result
        }

    internal object Api26Impl {
        fun compositeColors(foreground: Color, background: Color): Color {
            if (!(foreground.model == background.model)) {
                throw IllegalArgumentException(
                    ("Color models must match (" + foreground.model + " vs. "
                            + background.model + ")")
                )
            }
            val s =
                if ((background.colorSpace == foreground.colorSpace)) foreground else foreground.convert(
                    background.colorSpace
                )
            val src = s.components
            val dst = background.components
            var sa = s.alpha()
            // Destination alpha pre-composited
            var da = background.alpha() * (1.0f - sa)

            // Index of the alpha component
            @SuppressLint("Range") val ai// TODO Remove after upgrading AGP to 3.1 or newer.
                    = background.componentCount - 1

            // Final alpha: src_alpha + dst_alpha * (1 - src_alpha)
            dst[ai] = sa + da

            // Divide by final alpha to return non pre-multiplied color
            if (dst[ai] > 0) {
                sa /= dst[ai]
                da /= dst[ai]
            }

            // Composite non-alpha components
            for (i in 0 until ai) {
                dst[i] = src[i] * sa + dst[i] * da
            }
            return Color.valueOf(dst, background.colorSpace)
        }
    }
}