package io.github.thanosfisherman.demo.audioUtils

interface Music : Disposable {
    fun init()
    fun load(path: String)
    fun play(loop: Boolean = true, volume: Float = 1f)
    fun setVolume(volume: Float)
    fun pause()
    fun resume()
    fun stop()
    fun update()

}