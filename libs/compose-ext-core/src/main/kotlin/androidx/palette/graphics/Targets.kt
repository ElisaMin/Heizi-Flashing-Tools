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

import kotlin.reflect.KProperty



/**
* A class which allows custom selection of colors in a [Palette]'s generation. Instances
*
*
* To use the target, use the [Palette.Builder.targets] API when building a
* Palette.
*/
sealed class Targets {
    open val saturationTargets: Array<Float> = defaultArray
    open val lightnessTargets: Array<Float> = defaultArray
    open val weights: Array<Float> = arrayOf(WEIGHT_SATURATION, WEIGHT_LUMA, WEIGHT_POPULATION)
    /**
     * A target which has the characteristics of a vibrant color which is neither light or dark.
     */
    object Vibrant:Targets() {
        init {
            defaultValue(
                light = null,
                mute = false
            )
        }
    }
    /**
     * A target which has the characteristics of a vibrant color which is light in luminance.
     */
    object VibrantLight:Targets() {
        init {
            defaultValue(
                light = true,
                mute = false
            )
        }
    }
    /**
     * A target which has the characteristics of a vibrant color which is dark in luminance.
     */
    object VibrantDark:Targets() {
        init {
            defaultValue(
                light = false,
                mute = false
            )
        }
    }
    /**
     * A target which has the characteristics of a muted color which is neither light or dark.
     */
    object Muted:Targets() {
        init {
            defaultValue(
                light = null,
                mute = true
            )
        }
    }
    /**
     * A target which has the characteristics of a muted color which is dark in luminance.
     */
    object MutedDark:Targets() {
        init {
            defaultValue(
                light = false,
                mute = true
            )
        }
    }
    /**
     * A target which has the characteristics of a muted color which is light in luminance.
     */
    object MutedLight:Targets() {
        init {
            defaultValue(
                light = true,
                mute = true
            )
        }
    }


    protected fun defaultValue(
//        normalLightness:Boolean=true,
        light:Boolean?= null,
        mute:Boolean=false
    ) {
        when(light) {
            null -> {
                minimumLightness = MIN_NORMAL_LUMA
                maximumLightness = MAX_NORMAL_LUMA
                targetLightness = TARGET_NORMAL_LUMA
            }
            true -> {
                minimumLightness = MIN_LIGHT_LUMA
                targetLightness = TARGET_LIGHT_LUMA
            }
            false->{
                targetLightness = TARGET_DARK_LUMA
                maximumLightness = MAX_DARK_LUMA
            }
        }

        if (mute) {
            minimumSaturation = MIN_VIBRANT_SATURATION
            targetSaturation = TARGET_VIBRANT_SATURATION
        }
        else {
            maximumSaturation = MAX_MUTED_SATURATION
            targetSaturation = TARGET_MUTED_SATURATION
        }

    }
    fun normalizeWeights() {
        var sum = 0f
        for (weight in weights) {
            if (weight > 0) sum += weight
        }
        if (sum!=0f) repeat(weights.size) {
            if (weights[it]>0)
                weights[it] /= sum
        }
    }

    companion object {
        val all = arrayOf(VibrantLight, Vibrant, VibrantDark, MutedLight, Muted, MutedDark)
        private val defaultArray get() = arrayOf(0f,0.5f,1f)
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
    }

    /**
     * Returns whether any color selected for this target is exclusive for this target only.
     *
     *
     * If false, then the color can be selected for other targets.
     */
    var isExclusive = true // default to true
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Targets) return false

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

    override fun toString(): String {
        return "Targets:${this::class.simpleName},s:${saturationTargets.contentToString()},l:${lightnessTargets.contentToString()};"
    }

}


/**
 * The minimum saturation value for this target.
 */
var Targets.minimumSaturation: Float by IndexOf({saturationTargets}, Targets.INDEX_MIN)


/**
 * The target saturation value for this target.
 */

var Targets.targetSaturation: Float by IndexOf({ saturationTargets }, Targets.INDEX_TARGET)

/**
 * The maximum saturation value for this target.
 */

var Targets.maximumSaturation: Float by IndexOf({ saturationTargets }, Targets.INDEX_MAX)

/**
 * The minimum lightness value for this target.
 */

var Targets.minimumLightness: Float by IndexOf({ lightnessTargets }, Targets.INDEX_MIN)

/**
 * The target lightness value for this target.
 */

var Targets.targetLightness: Float by IndexOf({ lightnessTargets }, Targets.INDEX_TARGET)

/**
 * The maximum lightness value for this target.
 */

var Targets.maximumLightness: Float by IndexOf({ lightnessTargets }, Targets.INDEX_MAX)

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
var Targets.saturationWeight: Float by IndexOf({ weights }, Targets.INDEX_WEIGHT_SAT)

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
var Targets.lightnessWeight: Float by IndexOf({ weights }, Targets.INDEX_WEIGHT_LUMA)

/**
 * Returns the weight of importance that this target places on a color's population within
 * the image.
 *
 *
 * The larger the weight, relative to the other weights, the more important that a
 * color's population being close to the most populous has on selection.
 */
var Targets.populationWeight: Float by IndexOf({ weights }, Targets.INDEX_WEIGHT_POP)

class IndexOf(
    val a: Targets.()->Array<Float>,
    val i:Int
) {
    operator fun getValue(target: Targets, property: KProperty<*>): Float {
        return a(target)[i]
    }

    operator fun setValue(target: Targets, property: KProperty<*>, fl: Float) {
        a(target)[i]=fl
    }


}