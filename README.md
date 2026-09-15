# 🔥 The Balls of Fury 🔥 🪩🪩🪩

Try the WebAssembly version [here](https://thanosfisherman.github.io/ComposeCreativeDemo/).

## What it is

A little Kotlin Compose Multiplatform toy featuring a series of creative coding demos using ball-shaped kinematics and audio.

Everything else in the scene grows out of that one idea:

- **The balls beep in tune to musical scales and occasionally they shift between scale modes**
- **The background music is a piece I wrote myself to accompany the balls' sounds.**
- **A fixed virtual viewport** (borrowed straight from libGDX's `FitViewport`) keeps the whole scene's proportions locked, whether you're rotating a phone into portrait or dragging a desktop window around.
- **A debug overlay** (title, live FPS, current scale, speed).
- It runs the same way, same code, same look as an **Android APK, a cross-platform desktop fat JAR, and a Kotlin/Wasm web build.** Thanks to Compose Multiplatform. Check out the Release artifacts.

## Built with

Kotlin Multiplatform, Compose Multiplatform's Canvas API, a hand-rolled `withFrameNanos` + delta-time game loop, and entirely too much trial and error, especially on the math stuff.

## Screenshot

<img width="995" height="711" alt="Screenshot From 2026-09-07 01-29-37" src="https://github.com/user-attachments/assets/ec032c95-d3a0-452a-b688-cc5bbb35c676" />
