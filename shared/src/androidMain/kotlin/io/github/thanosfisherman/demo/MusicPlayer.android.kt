package io.github.thanosfisherman.demo

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.HandlerThread
import io.github.thanosfisherman.demo.audioUtils.Music
import java.util.concurrent.atomic.AtomicBoolean

class MusicPlayer(private val context: Context) : Music {

    private val thread = HandlerThread("MusicPlayer-audio").apply { start() }
    private val handler = Handler(thread.looper)
    private var mediaPlayer: MediaPlayer? = null
    private val disposed = AtomicBoolean(false)

    override fun init() {
        // Background looper thread is started upon construction.
    }

    /**
     * @param path a Compose Multiplatform resource URI from Res.getUri(...)
     */
    override fun load(path: String) {
        if (disposed.get()) return
        handler.post {
            if (disposed.get()) return@post
            releasePlayer()

            try {
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun play(loop: Boolean, volume: Float) {
        if (disposed.get()) return
        handler.post {
            if (disposed.get()) return@post
            mediaPlayer?.apply {
                isLooping = loop
                val vol = volume.coerceAtLeast(0f)
                setVolume(vol, vol)
                start()
            }
        }
    }

    override fun update() {
        // MediaPlayer manages its own buffering internally — nothing to pump per-frame.
    }

    override fun setVolume(volume: Float) {
        if (disposed.get()) return
        handler.post {
            if (disposed.get()) return@post
            val vol = volume.coerceAtLeast(0f)
            mediaPlayer?.setVolume(vol, vol)
        }
    }

    override fun pause() {
        if (disposed.get()) return
        handler.post {
            if (disposed.get()) return@post
            mediaPlayer?.takeIf { it.isPlaying }?.pause()
        }
    }

    override fun resume() {
        if (disposed.get()) return
        handler.post {
            if (disposed.get()) return@post
            mediaPlayer?.start()
        }
    }

    override fun stop() {
        if (disposed.get()) return
        handler.post {
            if (disposed.get()) return@post
            mediaPlayer?.apply {
                if (isPlaying) pause()
                seekTo(0)
            }
        }
    }

    private fun releasePlayer() {
        mediaPlayer?.apply {
            try {
                if (isPlaying) stop()
                release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaPlayer = null
    }

    override fun dispose() {
        if (!disposed.compareAndSet(false, true)) return
        handler.post {
            releasePlayer()
            thread.quitSafely()
        }
    }
}