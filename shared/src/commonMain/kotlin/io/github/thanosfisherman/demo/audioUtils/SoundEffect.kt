package io.github.thanosfisherman.demo.audioUtils

class SoundEffect(
    val uri: String,
    val player: Sound
) : Disposable {
    var bufferId: Int = -1
        private set

    fun init() {
        player.init()
        bufferId = player.loadSound(uri)
    }

    fun play(volume: Float = 1f, pitch: Float = 1f) {
        if (bufferId != -1) {
            player.play(bufferId, volume, pitch)
        }
    }

    override fun dispose() {
        player.dispose()
    }
}
