package io.github.thanosfisherman.demo


@OptIn(ExperimentalWasmJsInterop::class)
external class Howl constructor(options: JsAny) : JsAny {
    fun play(id: Double = definedExternally): Double
    fun pause(id: Double = definedExternally): Howl
    fun stop(id: Double = definedExternally): Howl
    fun volume(vol: Double = definedExternally, id: Double = definedExternally): JsAny
    fun rate(rate: Double = definedExternally, id: Double = definedExternally): JsAny
    fun loop(loop: Boolean = definedExternally, id: Double = definedExternally): JsAny
    fun playing(id: Double = definedExternally): Boolean
    fun unload()
}

/**
 * Builds Howler's options object. Deliberately omits `html5: true` — Howler's
 * rate()/pitch control only works in Web Audio mode, not the HTML5 Audio fallback.
 */
@OptIn(ExperimentalWasmJsInterop::class)
internal fun howlOptions(src: String, loop: Boolean, volume: Double): JsAny =
    js("({ src: [src], loop: loop, volume: volume })")