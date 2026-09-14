package io.github.thanosfisherman.demo.audioUtils

import kotlin.math.pow

object MusicIntervals {

    /**
     * Calculates the frequency multiplier for a given number of semitones.
     *
     * Formula:
     *     multiplier = 2^(semitones / 12)
     *
     * Examples:
     *     frequencyMultiplier(-12) = 0.5
     *     frequencyMultiplier(0)   = 1.0
     *     frequencyMultiplier(12)  = 2.0
     */
    fun frequencyMultiplier(semitones: Int): Float =
        2.0.pow(semitones / 12.0).toFloat()


// ─────────────────────────────────────────────
// Octave -1: Two octaves below unison
// Semitones: -24 to -13
// ─────────────────────────────────────────────

    val OCTAVE_MINUS_1 = frequencyMultiplier(-24)

    val MAJOR_SEVENTH_MINUS_1 = frequencyMultiplier(-23)
    val MINOR_SEVENTH_MINUS_1 = frequencyMultiplier(-22)
    val MAJOR_SIXTH_MINUS_1 = frequencyMultiplier(-21)
    val MINOR_SIXTH_MINUS_1 = frequencyMultiplier(-20)
    val PERFECT_FIFTH_MINUS_1 = frequencyMultiplier(-19)
    val TRITONE_MINUS_1 = frequencyMultiplier(-18)
    val PERFECT_FOURTH_MINUS_1 = frequencyMultiplier(-17)
    val MAJOR_THIRD_MINUS_1 = frequencyMultiplier(-16)
    val MINOR_THIRD_MINUS_1 = frequencyMultiplier(-15)
    val MAJOR_SECOND_MINUS_1 = frequencyMultiplier(-14)
    val MINOR_SECOND_MINUS_1 = frequencyMultiplier(-13)


// ─────────────────────────────────────────────
// Octave 0: One octave below unison
// Semitones: -12 to -1
// ─────────────────────────────────────────────

    val OCTAVE_0 = frequencyMultiplier(-12)

    val MAJOR_SEVENTH_0 = frequencyMultiplier(-11)
    val MINOR_SEVENTH_0 = frequencyMultiplier(-10)
    val MAJOR_SIXTH_0 = frequencyMultiplier(-9)
    val MINOR_SIXTH_0 = frequencyMultiplier(-8)
    val PERFECT_FIFTH_0 = frequencyMultiplier(-7)
    val TRITONE_0 = frequencyMultiplier(-6)
    val PERFECT_FOURTH_0 = frequencyMultiplier(-5)
    val MAJOR_THIRD_0 = frequencyMultiplier(-4)
    val MINOR_THIRD_0 = frequencyMultiplier(-3)
    val MAJOR_SECOND_0 = frequencyMultiplier(-2)
    val MINOR_SECOND_0 = frequencyMultiplier(-1)


// ─────────────────────────────────────────────
// Unison
// ─────────────────────────────────────────────

    val UNISON = frequencyMultiplier(0)


// ─────────────────────────────────────────────
// Octave 1: One octave above unison
// Semitones: 1 to 12
// ─────────────────────────────────────────────

    val MINOR_SECOND_1 = frequencyMultiplier(1)
    val MAJOR_SECOND_1 = frequencyMultiplier(2)
    val MINOR_THIRD_1 = frequencyMultiplier(3)
    val MAJOR_THIRD_1 = frequencyMultiplier(4)
    val PERFECT_FOURTH_1 = frequencyMultiplier(5)
    val TRITONE_1 = frequencyMultiplier(6)
    val PERFECT_FIFTH_1 = frequencyMultiplier(7)
    val MINOR_SIXTH_1 = frequencyMultiplier(8)
    val MAJOR_SIXTH_1 = frequencyMultiplier(9)
    val MINOR_SEVENTH_1 = frequencyMultiplier(10)
    val MAJOR_SEVENTH_1 = frequencyMultiplier(11)

    val OCTAVE_1 = frequencyMultiplier(12)


// ─────────────────────────────────────────────
// Octave 2: Two octaves above unison
// Semitones: 13 to 24
// ─────────────────────────────────────────────

    val MINOR_SECOND_2 = frequencyMultiplier(13)
    val MAJOR_SECOND_2 = frequencyMultiplier(14)
    val MINOR_THIRD_2 = frequencyMultiplier(15)
    val MAJOR_THIRD_2 = frequencyMultiplier(16)
    val PERFECT_FOURTH_2 = frequencyMultiplier(17)
    val TRITONE_2 = frequencyMultiplier(18)
    val PERFECT_FIFTH_2 = frequencyMultiplier(19)
    val MINOR_SIXTH_2 = frequencyMultiplier(20)
    val MAJOR_SIXTH_2 = frequencyMultiplier(21)
    val MINOR_SEVENTH_2 = frequencyMultiplier(22)
    val MAJOR_SEVENTH_2 = frequencyMultiplier(23)

    val OCTAVE_2 = frequencyMultiplier(24)


// ─────────────────────────────────────────────
// Scales
// ─────────────────────────────────────────────

    val CHROMATIC_SCALE: FloatArray = floatArrayOf(
        OCTAVE_0,
        MAJOR_SEVENTH_0,
        MINOR_SEVENTH_0,
        MAJOR_SIXTH_0,
        MINOR_SIXTH_0,
        PERFECT_FIFTH_0,
        TRITONE_0,
        PERFECT_FOURTH_0,
        MAJOR_THIRD_0,
        MINOR_THIRD_0,
        MAJOR_SECOND_0,
        MINOR_SECOND_0,
        UNISON,
        MINOR_SECOND_1,
        MAJOR_SECOND_1,
        MINOR_THIRD_1,
        MAJOR_THIRD_1,
        PERFECT_FOURTH_1,
        TRITONE_1,
        PERFECT_FIFTH_1,
        MINOR_SIXTH_1,
        MAJOR_SIXTH_1,
        MINOR_SEVENTH_1,
        MAJOR_SEVENTH_1,
        OCTAVE_1
    )

    val CHROMATIC_SCALE_MINUS_1: FloatArray = floatArrayOf(
        OCTAVE_MINUS_1,
        MAJOR_SEVENTH_MINUS_1,
        MINOR_SEVENTH_MINUS_1,
        MAJOR_SIXTH_MINUS_1,
        MINOR_SIXTH_MINUS_1,
        PERFECT_FIFTH_MINUS_1,
        TRITONE_MINUS_1,
        PERFECT_FOURTH_MINUS_1,
        MAJOR_THIRD_MINUS_1,
        MINOR_THIRD_MINUS_1,
        MAJOR_SECOND_MINUS_1,
        MINOR_SECOND_MINUS_1,
        OCTAVE_0
    )

    val CHROMATIC_SCALE_2: FloatArray = floatArrayOf(
        OCTAVE_1,
        MINOR_SECOND_2,
        MAJOR_SECOND_2,
        MINOR_THIRD_2,
        MAJOR_THIRD_2,
        PERFECT_FOURTH_2,
        TRITONE_2,
        PERFECT_FIFTH_2,
        MINOR_SIXTH_2,
        MAJOR_SIXTH_2,
        MINOR_SEVENTH_2,
        MAJOR_SEVENTH_2,
        OCTAVE_2
    )


    val MAJOR_SCALE: FloatArray = floatArrayOf(
        OCTAVE_0,
        MINOR_SEVENTH_0,
        MINOR_SIXTH_0,
        PERFECT_FIFTH_0,
        PERFECT_FOURTH_0,
        MINOR_THIRD_0,
        MINOR_SECOND_0,
        UNISON,
        MAJOR_SECOND_1,
        MAJOR_THIRD_1,
        PERFECT_FOURTH_1,
        PERFECT_FIFTH_1,
        MAJOR_SIXTH_1,
        MAJOR_SEVENTH_1,
        OCTAVE_1
    )

    val DORIAN_SCALE: FloatArray = floatArrayOf(
        OCTAVE_0,
        MINOR_SEVENTH_0,
        MAJOR_SIXTH_0,
        PERFECT_FIFTH_0,
        PERFECT_FOURTH_0,
        MINOR_THIRD_0,
        MAJOR_SECOND_0,
        UNISON,
        MAJOR_SECOND_1,
        MINOR_THIRD_1,
        PERFECT_FOURTH_1,
        PERFECT_FIFTH_1,
        MAJOR_SIXTH_1,
        MINOR_SEVENTH_1,
        OCTAVE_1
    )

    val HIRAJOSHI_SCALE: FloatArray = floatArrayOf(
        OCTAVE_0,
        MINOR_SEVENTH_0,
        MAJOR_SIXTH_0,
        PERFECT_FOURTH_0,
        MAJOR_THIRD_0,
        UNISON,
        MAJOR_SECOND_1,
        MINOR_THIRD_1,
        PERFECT_FIFTH_1,
        MINOR_SIXTH_1,
        OCTAVE_1
    )

    val MINOR_PENTATONIC_SCALE: FloatArray = floatArrayOf(
        OCTAVE_0,
        MAJOR_SIXTH_0,
        PERFECT_FIFTH_0,
        PERFECT_FOURTH_0,
        MAJOR_SECOND_0,
        UNISON,
        MINOR_THIRD_1,
        PERFECT_FOURTH_1,
        PERFECT_FIFTH_1,
        MINOR_SEVENTH_1,
        OCTAVE_1
    )

    val MAJOR_PENTATONIC_SCALE: FloatArray = floatArrayOf(
        OCTAVE_0,
        MINOR_SEVENTH_0,
        MINOR_SIXTH_0,
        PERFECT_FOURTH_0,
        MINOR_THIRD_0,
        UNISON,
        MAJOR_SECOND_1,
        MAJOR_THIRD_1,
        PERFECT_FIFTH_1,
        MAJOR_SIXTH_1,
        OCTAVE_1,
        MAJOR_SECOND_2,
        MAJOR_THIRD_2,
    )

    val MAJOR_ADD_2_ARPEGGIO: FloatArray = floatArrayOf(
        OCTAVE_0,
        MINOR_SEVENTH_0,
        MINOR_SIXTH_0,
        PERFECT_FOURTH_0,
        UNISON,
        MAJOR_SECOND_1,
        MAJOR_THIRD_1,
        PERFECT_FIFTH_1,
        OCTAVE_1
    )

    val WHOLE_TONE_SCALE: FloatArray = floatArrayOf(
        OCTAVE_0,
        MINOR_SEVENTH_0,
        MINOR_SIXTH_0,
        TRITONE_0,
        MAJOR_THIRD_0,
        MAJOR_SECOND_0,
        UNISON,
        MAJOR_SECOND_1,
        MAJOR_THIRD_1,
        TRITONE_1,
        MINOR_SIXTH_1,
        MINOR_SEVENTH_1,
        OCTAVE_1
    )

    val TRITONE_SCALE: FloatArray = floatArrayOf(
        OCTAVE_0,
        MAJOR_SEVENTH_0,
        MINOR_SIXTH_0,
        TRITONE_0,
        PERFECT_FOURTH_0,
        MAJOR_SECOND_0,
        UNISON,
        MINOR_SECOND_1,
        MAJOR_THIRD_1,
        TRITONE_1,
        PERFECT_FIFTH_1,
        MINOR_SEVENTH_1,
        OCTAVE_1
    )
    val ONE_FOUR_FIVE_ONE2: FloatArray = floatArrayOf(
        UNISON,
        PERFECT_FOURTH_1,
        PERFECT_FIFTH_1,
        OCTAVE_1
    )
}
