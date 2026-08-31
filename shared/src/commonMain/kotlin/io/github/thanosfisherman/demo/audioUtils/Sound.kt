package io.github.thanosfisherman.demo.audioUtils

/**
 *
 *
 * A Sound is a short audio clip that can be played numerous times in parallel. It's completely loaded into memory so only load
 * small audio files. Call the [.dispose] method when you're done using the Sound.
 *
 *
 *
 *
 * Sound instances are created via a call to [Audio.newSound].
 *
 *
 *
 *
 * Calling the [.play] or [.play] method will return a long which is an id to that instance of the sound. You
 * can use this id to modify the playback of that sound instance.
 *
 *
 *
 *
 * **Note**: any values provided will not be clamped, it is the developer's responsibility to do so
 *
 *
 * @author badlogicgames@gmail.com
 */
interface Sound : Disposable {
    fun init()
    fun loadSound(path: String): Int
    fun play(bufferId: Int, volume: Float = 1f, pitch: Float = 1f)
}