package io.github.thanosfisherman.demo.audioUtils

import composecreativedemo.shared.generated.resources.Res

enum class MusicTrack(val path: String) {
    BouncingBalls("files/music/epic_ballz_backing.ogg"),
    Pendulums("files/music/epads.ogg");

    val uri: String
        get() = try {
            Res.getUri(path)
        } catch (_: Throwable) {
            path
        }
}
