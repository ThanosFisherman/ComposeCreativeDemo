package io.github.thanosfisherman.demo.audioUtils

import composecreativedemo.shared.generated.resources.Res

enum class SoundTrack(val path: String) {
    BouncingBalls("files/sounds/vib1.wav"),
    Pendulums("files/sounds/vib2.wav"),
    Bass("files/sounds/bass.wav"),
    Swarm("files/sounds/bells.wav"),
    Beep("files/sounds/beep.wav");

    val uri: String
        get() = try {
            Res.getUri(path)
        } catch (_: Throwable) {
            path
        }
}