package io.github.thanosfisherman.demo.audioUtils

interface Sound : Disposable {
    fun init()
    fun loadSound(path: String): Int
    fun play(bufferId: Int, volume: Float = 1f, pitch: Float = 1f)
}