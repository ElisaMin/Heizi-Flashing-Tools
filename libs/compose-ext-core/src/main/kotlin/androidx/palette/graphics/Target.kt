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

import androidx.annotation.FloatRange
import androidx.palette.graphics.Target.Builder

/**
 * A class which allows custom selection of colors in a [Palette]'s generation. Instances
 * can be created via the [Builder] class.
 *
 *
 * To use the target, use the [Palette.Builder.addTarget] API when building a
 * Palette.
 */
class Target {
    val mSaturationTargets = FloatArray(3)
    val mLightnessTargets = FloatArray(3)
    val mWeights = FloatArray(3)

    /**
     * Returns whether any color selected for this target is exclusive for this target only.
     *
     *
     * If false, then the color can be selected for other targets.
     */
    var isExclusive = true // default to true

    internal constructor() {
        setTargetDefaultValues(mSaturationTargets)
        setTargetDefaultValues(mLightnessTargets)
        setDefaultWeights()
    }

    internal constructor(from: Target) {
        System.arraycopy(
            from.mSaturationTargets, 0, mSaturationTargets, 0,
            mSaturationTargets.size
        )
        System.arraycopy(
            from.mLightnessTargets, 0, mLightnessTargets, 0,
            mLightnessTargets.size
        )
        System.arraycopy(from.mWeights, 0, mWeights, 0, mWeights.size)
    }

    /**
     * The minimum saturation value for this target.
     */
    @get:FloatRange(from = 0, to = 1)
    val minimumSaturation: Float
        get() = mSaturationTargets[INDEX_MIN]

    /**
     * The target saturation value for this target.
     */
    @get:FloatRange(from = 0, to = 1)
    val targetSaturation: Float
        get() = mSaturationTargets[INDEX_TARGET]

    /**
     * The maximum saturation value for this target.
     */
    @get:FloatRange(from = 0, to = 1)
    val maximumSaturation: Float
        get() = mSaturationTargets[INDEX_MAX]

    /**
     * The minimum lightness value for this target.
     */
    @get:FloatRange(from = 0, to = 1)
    val minimumLightness: Float
        get() = mLightnessTargets[INDEX_MIN]

    /**
     * The target lightness value for this target.
     */
    @get:FloatRange(from = 0, to = 1)
    val targetLightness: Float
        get() = mLightnessTargets[INDEX_TARGET]

    /**
     * The maximum lightness value for this target.
     */
    @get:FloatRange(from = 0, to = 1)
    val maximumLightness: Float
        get() = mLightnessTargets[INDEX_MAX]

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
    val saturationWeight: Float
        get() = mWeights[INDEX_WEIGHT_SAT]

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
    val lightnessWeight: Float
        get() = mWeights[INDEX_WEIGHT_LUMA]

    /**
     * Returns the weight of importance that this target places on a color's population within
     * the image.
     *
     *
     * The larger the weight, relative to the other weights, the more important that a
     * color's population being close to the most populous has on selection.
     */
    val populationWeight: Float
        get() = mWeights[INDEX_WEIGHT_POP]

    private fun setDefaultWeights() {
        mWeights[INDEX_WEIGHT_SAT] = WEIGHT_SATURATION
        mWeights[INDEX_WEIGHT_LUMA] = WEIGHT_LUMA
        mWeights[INDEX_WEIGHT_POP] = WEIGHT_POPULATION
    }

    fun normalizeWeights() {
        var sum = 0f
        var i = 0
        val z = mWeights.size
        while (i < z) {
            val weight = mWeights[i]
            if (weight > 0) {
                sum += weight
            }
            i++
        }
        if (sum != 0f) {
            var i = 0
            val z = mWeights.size
            while (i < z) {
                if (mWeights[i] > 0) {
                    mWeights[i] /= sum
                }
                i++
            }
        }
    }

    /**
     * Builder class for generating custom [Target] instances.
     */
    class Builder {
        private val mTarget: Target

        /**
         * Create a new [Target] builder from scratch.
         */
        constructor() {
            mTarget = Target()
        }

        /**
         * Create a new builder based on an existing [Target].
         */
        constructor(target: Target) {
            mTarget = Target(target)
        }

        /**
         * Set the minimum saturation value for this target.
         */
        fun setMinimumSaturation(@FloatRange(from = 0, to = 1) value: Float): Builder {
            mTarget.mSaturationTargets[INDEX_MIN] = value
            return this
        }

        /**
         * Set the target/ideal saturation value for this target.
         */
        fun setTargetSaturation(@FloatRange(from = 0, to = 1) value: Float): Builder {
            mTarget.mSaturationTargets[INDEX_TARGET] = value
            return this
        }

        /**
         * Set the maximum saturation value for this target.
         */
        fun setMaximumSaturation(@FloatRange(from = 0, to = 1) value: Float): Builder {
            mTarget.mSaturationTargets[INDEX_MAX] = value
            return this
        }

        /**
         * Set the minimum lightness value for this target.
         */
        fun setMinimumLightness(@FloatRange(from = 0, to = 1) value: Float): Builder {
            mTarget.mLightnessTargets[INDEX_MIN] = value
            return this
        }

        /**
         * Set the target/ideal lightness value for this target.
         */
        fun setTargetLightness(@FloatRange(from = 0, to = 1) value: Float): Builder {
            mTarget.mLightnessTargets[INDEX_TARGET] = value
            return this
        }

        /**
         * Set the maximum lightness value for this target.
         */
        fun setMaximumLightness(@FloatRange(from = 0, to = 1) value: Float): Builder {
            mTarget.mLightnessTargets[INDEX_MAX] = value
            return this
        }

        /**
         * Set the weight of importance that this target will place on saturation values.
         *
         *
         * The larger the weight, relative to the other weights, the more important that a color
         * being close to the target value has on selection.
         *
         *
         * A weight of 0 means that it has no weight, and thus has no
         * bearing on the selection.
         *
         * @see .setTargetSaturation
         */
        fun setSaturationWeight(@FloatRange(from = 0) weight: Float): Builder {
            mTarget.mWeights[INDEX_WEIGHT_SAT] = weight
            return this
        }

        /**
         * Set the weight of importance that this target will place on lightness values.
         *
         *
         * The larger the weight, relative to the other weights, the more important that a color
         * being close to the target value has on selection.
         *
         *
         * A weight of 0 means that it has no weight, and thus has no
         * bearing on the selection.
         *
         * @see .setTargetLightness
         */
        fun setLightnessWeight(@FloatRange(from = 0) weight: Float): Builder {
            mTarget.mWeights[INDEX_WEIGHT_LUMA] = weight
            return this
        }

        /**
         * Set the weight of importance that this target will place on a color's population within
         * the image.
         *
         *
         * The larger the weight, relative to the other weights, the more important that a
         * color's population being close to the most populous has on selection.
         *
         *
         * A weight of 0 means that it has no weight, and thus has no
         * bearing on the selection.
         */
        fun setPopulationWeight(@FloatRange(from = 0) weight: Float): Builder {
            mTarget.mWeights[INDEX_WEIGHT_POP] = weight
            return this
        }

        /**
         * Set whether any color selected for this target is exclusive to this target only.
         * Defaults to true.
         *
         * @param exclusive true if any the color is exclusive to this target, or false is the
         * color can be selected for other targets.
         */
        fun setExclusive(exclusive: Boolean): Builder {
            mTarget.isExclusive = exclusive
            return this
        }

        /**
         * Builds and returns the resulting [Target].
         */
        fun build(): Target {
            return mTarget
        }
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
        val LIGHT_VIBRANT: Target = null

        /**
         * A target which has the characteristics of a vibrant color which is neither light or dark.
         */
        val VIBRANT: Target = null

        /**
         * A target which has the characteristics of a vibrant color which is dark in luminance.
         */
        val DARK_VIBRANT: Target = null

        /**
         * A target which has the characteristics of a muted color which is light in luminance.
         */
        val LIGHT_MUTED: Target = null

        /**
         * A target which has the characteristics of a muted color which is neither light or dark.
         */
        val MUTED: Target = null

        /**
         * A target which has the characteristics of a muted color which is dark in luminance.
         */
        val DARK_MUTED: Target = null

        init {
            LIGHT_VIBRANT = Target()
            setDefaultLightLightnessValues(LIGHT_VIBRANT)
            setDefaultVibrantSaturationValues(LIGHT_VIBRANT)
            VIBRANT = Target()
            setDefaultNormalLightnessValues(VIBRANT)
            setDefaultVibrantSaturationValues(VIBRANT)
            DARK_VIBRANT = Target()
            setDefaultDarkLightnessValues(DARK_VIBRANT)
            setDefaultVibrantSaturationValues(DARK_VIBRANT)
            LIGHT_MUTED = Target()
            setDefaultLightLightnessValues(LIGHT_MUTED)
            setDefaultMutedSaturationValues(LIGHT_MUTED)
            MUTED = Target()
            setDefaultNormalLightnessValues(MUTED)
            setDefaultMutedSaturationValues(MUTED)
            DARK_MUTED = Target()
            setDefaultDarkLightnessValues(DARK_MUTED)
            setDefaultMutedSaturationValues(DARK_MUTED)
        }

        private fun setTargetDefaultValues(values: FloatArray) {
            values[INDEX_MIN] = 0f
            values[INDEX_TARGET] = 0.5f
            values[INDEX_MAX] = 1f
        }

        private fun setDefaultDarkLightnessValues(target: Target) {
            target.mLightnessTargets[INDEX_TARGET] = TARGET_DARK_LUMA
            target.mLightnessTargets[INDEX_MAX] = MAX_DARK_LUMA
        }

        private fun setDefaultNormalLightnessValues(target: Target) {
            target.mLightnessTargets[INDEX_MIN] = MIN_NORMAL_LUMA
            target.mLightnessTargets[INDEX_TARGET] = TARGET_NORMAL_LUMA
            target.mLightnessTargets[INDEX_MAX] = MAX_NORMAL_LUMA
        }

        private fun setDefaultLightLightnessValues(target: Target) {
            target.mLightnessTargets[INDEX_MIN] = MIN_LIGHT_LUMA
            target.mLightnessTargets[INDEX_TARGET] = TARGET_LIGHT_LUMA
        }

        private fun setDefaultVibrantSaturationValues(target: Target) {
            target.mSaturationTargets[INDEX_MIN] = MIN_VIBRANT_SATURATION
            target.mSaturationTargets[INDEX_TARGET] = TARGET_VIBRANT_SATURATION
        }

        private fun setDefaultMutedSaturationValues(target: Target) {
            target.mSaturationTargets[INDEX_TARGET] = TARGET_MUTED_SATURATION
            target.mSaturationTargets[INDEX_MAX] = MAX_MUTED_SATURATION
        }
    }
}