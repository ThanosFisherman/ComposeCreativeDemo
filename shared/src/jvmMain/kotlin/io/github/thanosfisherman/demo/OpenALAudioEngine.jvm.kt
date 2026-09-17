package io.github.thanosfisherman.demo

import org.lwjgl.openal.AL
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALC10

internal object OpenALAudioEngine {
    private var device: Long = 0
    private var context: Long = 0
    private var refCount = 0

    @Synchronized
    fun retain() {
        if (refCount == 0) {
            val defaultDeviceName = ALC10.alcOpenDevice(null as CharSequence?)
            device = defaultDeviceName
            require(device != 0L) {
                "Failed to open default OpenAL device"
            }

            val attribs = intArrayOf(0)
            context = ALC10.alcCreateContext(device, attribs)
            require(context != 0L) {
                "Failed to create OpenAL context"
            }

            ALC10.alcMakeContextCurrent(context)

            val alcCapabilities = ALC.createCapabilities(device)
            AL.createCapabilities(alcCapabilities)
        } else {
            ALC10.alcMakeContextCurrent(context)
        }
        refCount++
    }

    @Synchronized
    fun makeContextCurrent() {
        if (context != 0L) {
            ALC10.alcMakeContextCurrent(context)
        }
    }

    @Synchronized
    fun release() {
        if (refCount > 0) {
            refCount--
            if (refCount == 0 && context != 0L) {
                ALC10.alcMakeContextCurrent(0)
                ALC10.alcDestroyContext(context)
                ALC10.alcCloseDevice(device)
                context = 0
                device = 0
            }
        }
    }
}
