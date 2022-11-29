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
import kotlin.math.max
import kotlin.math.min


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
    require(foreground.model == background.model) {
        "Color models must match (${foreground.model} vs. ${background.model})"
    }
    var dst = background
    var res = if (background.colorSpace == foreground.colorSpace)
        foreground else foreground.convert(background.colorSpace)

    dst = dst.copy(
        // Final alpha: src_alpha + dst_alpha * (1 - src_alpha)
        alpha = res.alpha+(
           // Destination alpha pre-composited
            background.alpha * (1.0f - res.alpha)
        )
    )
    // Divide by final alpha to return non pre-multiplied color
    if (dst.alpha>0) {
        res = res.copy(alpha = res.alpha.div(dst.alpha))
        dst = dst.copy(alpha = dst.alpha.div(dst.alpha))
    }

    // Composite non-alpha components
    dst = dst.copy(
        alpha = res.alpha *res.alpha+dst.alpha*dst.alpha,
        red   = res.red   *res.alpha+dst.red  *dst.alpha,
        green = res.green *res.alpha+dst.green*dst.alpha,
        blue  = res.blue  *res.alpha+dst.blue *dst.alpha,
    )

    return dst.convert(background.colorSpace)
}

private val Color.components
    get() = arrayOf(component1(),component2(),component3(),component4())

private val Color.model
    get() = colorSpace.model

/**
 * A set of color-related utility methods, building upon those available in `Color`.
 */

private val TEMP_ARRAY = ThreadLocal<DoubleArray>()


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
 * Returns the contrast ratio between `foreground` and `background`.
 * `background` must be opaque.
 *
 *
 * Formula defined
 * [here](http://www.w3.org/TR/2008/REC-WCAG20-20081211/#contrast-ratiodef).
 */
private fun calculateContrast(foreground: Int, background: Int): Double {
    var foreground = foreground
    if (Color.alpha(background) != 255) {
        throw IllegalArgumentException(
            "background can not be translucent: #"
                    + Integer.toHexString(background)
        )
    }
    if (Color.alpha(foreground) < 255) {
        // If the foreground is translucent, composite the foreground over the background
        foreground = compositeColors(Color(foreground), Color(background)).value.toInt()
    }
    val luminance1 = calculateLuminance(foreground) + 0.05
    val luminance2 = calculateLuminance(background) + 0.05

    // Now return the lighter luminance divided by the darker luminance
    return max(luminance1, luminance2) / min(luminance1, luminance2)
}
private val tempDouble3Array: DoubleArray
    get() {
        var result = TEMP_ARRAY.get()
        if (result == null) {
            result = DoubleArray(3)
            TEMP_ARRAY.set(result)
        }
        return result
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
private fun compositeAlpha(foregroundAlpha: Int, backgroundAlpha: Int): Int {
    return 0xFF - (0xFF - backgroundAlpha) * (0xFF - foregroundAlpha) / 0xFF
}

private fun compositeComponent(fgC: Int, fgA: Int, bgC: Int, bgA: Int, a: Int): Int {
    return if (a == 0) 0 else ((0xFF * fgC * fgA) + (bgC * bgA * (0xFF - fgA))) / (a * 0xFF)
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
private fun RGBToXYZ(
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
private const val XYZ_WHITE_REFERENCE_X = 95.047
private const val XYZ_WHITE_REFERENCE_Y = 100.0
private const val XYZ_WHITE_REFERENCE_Z = 108.883
private const val XYZ_EPSILON = 0.008856
private const val XYZ_KAPPA = 903.3

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
private fun LABToXYZ(
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

private fun XYZToColor(
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

private fun constrain(amount: Float, low: Float, high: Float): Float {
    return if (amount < low) low else Math.min(amount, high)
}

private fun circularInterpolate(a: Float, b: Float, f: Float): Float {
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

private fun constrain(amount: Int, low: Int, high: Int): Int {
    return if (amount < low) low else Math.min(amount, high)
}

private const val MIN_ALPHA_SEARCH_MAX_ITERATIONS = 10
private const val MIN_ALPHA_SEARCH_PRECISION = 1