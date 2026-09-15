package io.github.thanosfisherman.demo


@OptIn(ExperimentalWasmJsInterop::class)
external class Howl constructor(options: JsAny) : JsAny {
    fun play(id: Double = definedExternally): Double
    fun pause(id: Double = definedExternally): Howl
    fun stop(id: Double = definedExternally): Howl
    fun volume(vol: Double = definedExternally, id: Double = definedExternally): JsAny
    fun rate(rate: Double = definedExternally, id: Double = definedExternally): JsAny
    fun loop(loop: Boolean): JsAny
    fun loop(loop: Boolean = definedExternally, id: Double = definedExternally): JsAny
    fun playing(id: Double = definedExternally): Boolean
    fun unload()
}

/**
 * Builds Howler's options object.
 *
 * html5 is intentionally omitted because rate()/pitch control
 * requires Web Audio mode.
 */
@OptIn(ExperimentalWasmJsInterop::class)
internal fun howlOptions(
    src: String,
    loop: Boolean,
    volume: Double,
    pool: Int = 16,
    html5: Boolean = false
): JsAny =
    js("({ src: [src], loop: loop, volume: volume, pool: pool, html5: html5 })")