package io.github.thanosfisherman.demo

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import io.github.thanosfisherman.demo.audioUtils.Music

class MusicPlayer(private val context: Context) : Music {

    private var mediaPlayer: MediaPlayer? = null

    override fun init() {
        // MediaPlayer instances are created fresh per load() — nothing to set up here.
    }

    /**
     * @param path a Compose Multiplatform resource URI from Res.getUri(...)
     */
    override fun load(path: String) {
        dispose()

        val afd = context.assets.openFd(toAssetPath(path))
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            prepare()
        }
        afd.close()
    }

    override fun play(loop: Boolean, volume: Float) {
        val mp = mediaPlayer ?: error("No track loaded — call load() first")
        mp.isLooping = loop
        mp.setVolume(volume.coerceAtLeast(0f), volume.coerceAtLeast(0f))
        mp.start()
    }

    override fun update() {
        // MediaPlayer manages its own buffering internally — nothing to pump per-frame.
    }

    override fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume.coerceAtLeast(0f), volume.coerceAtLeast(0f))
    }

    override fun pause() {
        mediaPlayer?.takeIf { it.isPlaying }?.pause()
    }

    override fun resume() {
        mediaPlayer?.start()
    }

    override fun stop() {
        mediaPlayer?.apply {
            if (isPlaying) pause()
            seekTo(0)
        }
    }

    override fun dispose() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }
}