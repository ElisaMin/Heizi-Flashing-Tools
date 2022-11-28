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

import kotlin.reflect.*


/**
 * A class which allows custom selection of colors in a [Palette]'s generation. Instances
 *
 *
 * To use the target, use the [Palette.Builder.addTarget] API when building a
 * Palette.
 */
data class Target(
    val saturationTargets: FloatArray = FloatArray(3),
    val lightnessTargets: FloatArray = FloatArray(3),
    val weights: FloatArray = FloatArray(3),
) {


    class IndexOf(
        val a:FloatArray,
        val i:Int
    ) {
        operator fun getValue(target: Target, property: KProperty<*>): Float {
            return (property as KProperty1<Target,Float>).get(target)
        }

        operator fun setValue(target: Target, property: KProperty<*>, fl: Float) {
            (property as KMutableProperty1<Target,Float>).set(target,fl)
        }

    }

    /**
     * Returns whether any color selected for this target is exclusive for this target only.
     *
     *
     * If false, then the color can be selected for other targets.
     */
    var isExclusive = true // default to true

    init {
        setTargetDefaultValues(saturationTargets)
        setTargetDefaultValues(lightnessTargets)
        setDefaultWeights()
    }
    /**
     * The minimum saturation value for this target.
     */
    
    var minimumSaturation: Float by IndexOf(saturationTargets, INDEX_MIN)


    /**
     * The target saturation value for this target.
     */
    
    var targetSaturation: Float by IndexOf(saturationTargets,INDEX_TARGET)

    /**
     * The maximum saturation value for this target.
     */
    
    var maximumSaturation: Float by IndexOf(saturationTargets,INDEX_MAX)

    /**
     * The minimum lightness value for this target.
     */
    
    var minimumLightness: Float by IndexOf(lightnessTargets,INDEX_MIN)

    /**
     * The target lightness value for this target.
     */
    
    var targetLightness: Float by IndexOf(lightnessTargets,INDEX_TARGET)

    /**
     * The maximum lightness value for this target.
     */
    
    var maximumLightness: Float by IndexOf(lightnessTargets,INDEX_MAX)

    /**
     * Returns the weight of importance that this target places on a color's saturation within
     * the image.
     *
     *
     * The larger the weight, relative to the other weights, the more important that a color
     * being close to the target value has on selection.
     *
     * @see .getTargetSaturation
     */
    var saturationWeight: Float by IndexOf(weights,INDEX_WEIGHT_SAT)

    /**
     * Returns the weight of importance that this target places on a color's lightness within
     * the image.
     *
     *
     * The larger the weight, relative to the other weights, the more important that a color
     * being close to the target value has on selection.
     *
     * @see .getTargetLightness
     */
    var lightnessWeight: Float by IndexOf(weights,INDEX_WEIGHT_LUMA)

    /**
     * Returns the weight of importance that this target places on a color's population within
     * the image.
     *
     *
     * The larger the weight, relative to the other weights, the more important that a
     * color's population being close to the most populous has on selection.
     */
    var populationWeight: Float by IndexOf(weights,INDEX_WEIGHT_POP)

    private fun setDefaultWeights() {

        weights[INDEX_WEIGHT_SAT] = WEIGHT_SATURATION
        weights[INDEX_WEIGHT_LUMA] = WEIGHT_LUMA
        weights[INDEX_WEIGHT_POP] = WEIGHT_POPULATION
    }

    @Suppress("NAME_SHADOWING")
    fun normalizeWeights() {
        var sum = 0f
        var i = 0
        val z = weights.size
        while (i < z) {
            val weight = weights[i]
            if (weight > 0) {
                sum += weight
            }
            i++
        }
        if (sum != 0f) {
            var i = 0
            val z = weights.size
            while (i < z) {
                if (weights[i] > 0) {
                    weights[i] /= sum
                }
                i++
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Target) return false

        if (!saturationTargets.contentEquals(other.saturationTargets)) return false
        if (!lightnessTargets.contentEquals(other.lightnessTargets)) return false
        if (!weights.contentEquals(other.weights)) return false
        if (isExclusive != other.isExclusive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = saturationTargets.contentHashCode()
        result = 31 * result + lightnessTargets.contentHashCode()
        result = 31 * result + weights.contentHashCode()
        result = 31 * result + isExclusive.hashCode()
        return result
    }

    companion object {

        private const val TARGET_DARK_LUMA = 0.26f
        private const val MAX_DARK_LUMA = 0.45f
        private const val MIN_LIGHT_LUMA = 0.55f
        private const val TARGET_LIGHT_LUMA = 0.74f
        private const val MIN_NORMAL_LUMA = 0.3f
        private const val TARGET_NORMAL_LUMA = 0.5f
        private const val MAX_NORMAL_LUMA = 0.7f
        private const val TARGET_MUTED_SATURATION = 0.3f
        private const val MAX_MUTED_SATURATION = 0.4f
        private const val TARGET_VIBRANT_SATURATION = 1f
        private const val MIN_VIBRANT_SATURATION = 0.35f
        private const val WEIGHT_SATURATION = 0.24f
        private const val WEIGHT_LUMA = 0.52f
        private const val WEIGHT_POPULATION = 0.24f
        const val INDEX_MIN = 0
        const val INDEX_TARGET = 1
        const val INDEX_MAX = 2
        const val INDEX_WEIGHT_SAT = 0
        const val INDEX_WEIGHT_LUMA = 1
        const val INDEX_WEIGHT_POP = 2

        /**
         * A target which has the characteristics of a vibrant color which is light in luminance.
         */
        val LIGHT_VIBRANT = Target()
        init {
            setDefaultLightLightnessValues(LIGHT_VIBRANT)
            setDefaultVibrantSaturationValues(LIGHT_VIBRANT)
        }

        /**
         * A target which has the characteristics of a vibrant color which is neither light or dark.
         */
        val VIBRANT: Target = Target()
        init {
            setDefaultNormalLightnessValues(VIBRANT)
            setDefaultVibrantSaturationValues(VIBRANT)

        }

        /**
         * A target which has the characteristics of a vibrant color which is dark in luminance.
         */
        val DARK_VIBRANT: Target = Target()
        init {
            setDefaultDarkLightnessValues(DARK_VIBRANT)
            setDefaultVibrantSaturationValues(DARK_VIBRANT)
        }

        /**
         * A target which has the characteristics of a muted color which is light in luminance.
         */
        val LIGHT_MUTED: Target = Target()
        init {
            setDefaultLightLightnessValues(LIGHT_MUTED)
            setDefaultMutedSaturationValues(LIGHT_MUTED)

        }

        /**
         * A target which has the characteristics of a muted color which is neither light or dark.
         */
        val MUTED: Target = Target()
        init {
            setDefaultNormalLightnessValues(MUTED)
            setDefaultMutedSaturationValues(MUTED)
        }

        /**
         * A target which has the characteristics of a muted color which is dark in luminance.
         */
        val DARK_MUTED: Target = Target()
        init {
            setDefaultDarkLightnessValues(DARK_MUTED)
            setDefaultMutedSaturationValues(DARK_MUTED)
            setDefaultNormalLightnessValues(DARK_VIBRANT)
        }


        private fun setTargetDefaultValues(values: FloatArray) {
            values[INDEX_MIN] = 0f
            values[INDEX_TARGET] = 0.5f
            values[INDEX_MAX] = 1f
        }

        private fun setDefaultDarkLightnessValues(target: Target) {
            target.lightnessTargets[INDEX_TARGET] = TARGET_DARK_LUMA
            target.lightnessTargets[INDEX_MAX] = MAX_DARK_LUMA
        }

        private fun setDefaultNormalLightnessValues(target: Target) {
            target.lightnessTargets[INDEX_MIN] = MIN_NORMAL_LUMA
            target.lightnessTargets[INDEX_TARGET] = TARGET_NORMAL_LUMA
            target.lightnessTargets[INDEX_MAX] = MAX_NORMAL_LUMA
        }

        private fun setDefaultLightLightnessValues(target: Target) {
            target.lightnessTargets[INDEX_MIN] = MIN_LIGHT_LUMA
            target.lightnessTargets[INDEX_TARGET] = TARGET_LIGHT_LUMA
        }

        private fun setDefaultVibrantSaturationValues(target: Target) {
            target.saturationTargets[INDEX_MIN] = MIN_VIBRANT_SATURATION
            target.saturationTargets[INDEX_TARGET] = TARGET_VIBRANT_SATURATION
        }

        private fun setDefaultMutedSaturationValues(target: Target) {
            target.saturationTargets[INDEX_TARGET] = TARGET_MUTED_SATURATION
            target.saturationTargets[INDEX_MAX] = MAX_MUTED_SATURATION
        }
    }
}