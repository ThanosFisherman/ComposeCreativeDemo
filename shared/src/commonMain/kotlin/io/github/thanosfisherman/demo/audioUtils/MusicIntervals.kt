package io.github.thanosfisherman.demo.audioUtils

object MusicIntervals {
    // Chromatic scale pitch multipliers (as floats for faster calculations)
    const val OCTAVE_BELOW: Float = 0.5f // n = -12 (2^(-12/12))
    const val MAJOR_SEVENTH_BELOW: Float = 0.5297316f // n = -11 (≈2^(-11/12))
    const val MINOR_SEVENTH_BELOW: Float = 0.5612310f // n = -10
    const val MAJOR_SIXTH_BELOW: Float = 0.5946036f // n = -9
    const val MINOR_SIXTH_BELOW: Float = 0.6299606f // n = -8
    const val PERFECT_FIFTH_BELOW: Float = 0.6674199f // n = -7
    const val TRITONE_BELOW: Float = 0.7071068f // n = -6 (≈2^(-6/12) = 1/√2)
    const val PERFECT_FOURTH_BELOW: Float = 0.7491536f // n = -5
    const val MAJOR_THIRD_BELOW: Float = 0.7937006f // n = -4
    const val MINOR_THIRD_BELOW: Float = 0.8408964f // n = -3
    const val MAJOR_SECOND_BELOW: Float = 0.8908987f // n = -2
    const val MINOR_SECOND_BELOW: Float = 0.9438743f // n = -1
    const val UNISON: Float = 1.0f // n = 0
    const val MINOR_SECOND: Float = 1.0594631f // n = 1 (2^(1/12))
    const val MAJOR_SECOND: Float = 1.1224620f // n = 2
    const val MINOR_THIRD: Float = 1.1892071f // n = 3
    const val MAJOR_THIRD: Float = 1.2599211f // n = 4
    const val PERFECT_FOURTH: Float = 1.3348399f // n = 5
    const val TRITONE: Float = 1.4142135f // n = 6 (√2)
    const val PERFECT_FIFTH: Float = 1.4983071f // n = 7
    const val MINOR_SIXTH: Float = 1.5874011f // n = 8
    const val MAJOR_SIXTH: Float = 1.6817929f // n = 9
    const val MINOR_SEVENTH: Float = 1.7817974f // n = 10
    const val MAJOR_SEVENTH: Float = 1.8877486f // n = 11
    const val OCTAVE_ABOVE: Float = 2.0f // n = 12

    // Array of all 12 semitones (ascending)
    val CHROMATIC_SCALE: FloatArray = floatArrayOf(
        OCTAVE_BELOW,
        MAJOR_SEVENTH_BELOW,
        MINOR_SEVENTH_BELOW,
        MAJOR_SIXTH_BELOW,
        MINOR_SIXTH_BELOW,
        PERFECT_FIFTH_BELOW,
        TRITONE_BELOW,
        PERFECT_FOURTH_BELOW,
        MAJOR_THIRD_BELOW,
        MINOR_THIRD_BELOW,
        MAJOR_SECOND_BELOW,
        MINOR_SECOND_BELOW,
        UNISON,
        MINOR_SECOND,
        MAJOR_SECOND,
        MINOR_THIRD,
        MAJOR_THIRD,
        PERFECT_FOURTH,
        TRITONE,
        PERFECT_FIFTH,
        MINOR_SIXTH,
        MAJOR_SIXTH,
        MINOR_SEVENTH,
        MAJOR_SEVENTH,
        OCTAVE_ABOVE
    )

    val MAJOR_SCALE: FloatArray = floatArrayOf(
        OCTAVE_BELOW,
        MINOR_SEVENTH_BELOW,
        MINOR_SIXTH_BELOW,
        PERFECT_FIFTH_BELOW,
        PERFECT_FOURTH_BELOW,
        MINOR_THIRD_BELOW,
        MINOR_SECOND_BELOW,
        UNISON,
        MAJOR_SECOND,
        MAJOR_THIRD,
        PERFECT_FOURTH,
        PERFECT_FIFTH,
        MAJOR_SIXTH,
        MAJOR_SEVENTH,
        OCTAVE_ABOVE
    )

    val MINOR_PENTATONIC_SCALE: FloatArray = floatArrayOf(
        OCTAVE_BELOW,
        MAJOR_SIXTH_BELOW,
        PERFECT_FIFTH_BELOW,
        PERFECT_FOURTH_BELOW,
        MAJOR_SECOND_BELOW,
        UNISON,
        MINOR_THIRD,
        PERFECT_FOURTH,
        PERFECT_FIFTH,
        MINOR_SEVENTH,
        OCTAVE_ABOVE
    )
    val MAJOR_PENTATONIC_SCALE: FloatArray = floatArrayOf(
        OCTAVE_BELOW,
        MINOR_SEVENTH_BELOW,
        MINOR_SIXTH_BELOW,
        PERFECT_FOURTH_BELOW,
        MINOR_THIRD_BELOW,
        UNISON,
        MAJOR_SECOND,
        MAJOR_THIRD,
        PERFECT_FIFTH,
        MAJOR_SIXTH,
        OCTAVE_ABOVE
    )
    val MAJOR_ADD_2_ARPEGGIO: FloatArray = floatArrayOf(
        OCTAVE_BELOW,
        MINOR_SEVENTH_BELOW,
        MINOR_SIXTH_BELOW,
        PERFECT_FOURTH_BELOW,
        UNISON,
        MAJOR_SECOND,
        MAJOR_THIRD,
        PERFECT_FIFTH,
        OCTAVE_ABOVE
    )

    val WHOLE_TONE_SCALE: FloatArray = floatArrayOf(
        OCTAVE_BELOW,
        MINOR_SEVENTH_BELOW,
        MINOR_SIXTH_BELOW,
        TRITONE_BELOW,
        MAJOR_THIRD_BELOW,
        MAJOR_SECOND_BELOW,
        UNISON,
        MAJOR_SECOND,
        MAJOR_THIRD,
        TRITONE,
        MINOR_SIXTH,
        MINOR_SEVENTH,
        OCTAVE_ABOVE
    )

    val TRITONE_SCALE: FloatArray = floatArrayOf(
        OCTAVE_BELOW,
        MAJOR_SEVENTH_BELOW,
        MINOR_SIXTH_BELOW,
        TRITONE_BELOW,
        PERFECT_FOURTH_BELOW,
        MAJOR_SECOND_BELOW,
        UNISON,
        MINOR_SECOND,
        MAJOR_THIRD,
        TRITONE,
        PERFECT_FIFTH,
        MINOR_SEVENTH,
        OCTAVE_ABOVE
    )
}
