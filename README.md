# Balls of Fury

Try the WebAssembly version [here]().

## What it is

A little Kotlin Compose Multiplatform toy: a row of glowing balls trapped in a V-shaped wedge, sweeping back and forth and bouncing a beep off each wall.

Everything else in the scene grows out of that one idea:

- **The balls beep in tune to musical scales. In particular, they play notes from the Tritone and Whole Tone scales.**
- **The background music is little piece I wrote myself to accomany the balls' beeps.**
- **A fixed virtual viewport** (borrowed straight from libGDX's `FitViewport`) keeps the whole scene's proportions locked, whether you're rotating a phone into portrait or dragging a desktop window around.
- **The ball count quietly ramps up** for the first 15 seconds, one ball at a time, so the wedge visibly grows on you before it settles.
- **The musical scale and sweep speed cycle on a timer** — Tritone and Whole Tone scales trade off, tempo shifts between three speeds — so no two minutes of watching it sound quite the same.
- **A debug overlay** (title, live FPS, current scale) rides on top, tucked safely past the status bar on Android.
- **Real sound**, per bounce, with a genuine pitch multiplier — matching the exact `Sound.play(volume, pitch, pan)` convention the original libGDX code used, right down to `1f = default, >1f = higher, <1f = lower`.
- It runs the same way — same code, same look — as an **Android APK, a cross-platform desktop fat JAR, and a Kotlin/Wasm web build.**

## Built with

Kotlin Multiplatform, Compose Multiplatform's Canvas API, a hand-rolled `withFrameNanos` + delta-time game loop, and entirely too much trial and error with the math stuff.

## Screenshot