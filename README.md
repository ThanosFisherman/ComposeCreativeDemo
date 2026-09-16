# 🔥 The Balls of Fury 🔥 🪩🪩🪩

Try the WebAssembly version [here](https://thanosfisherman.github.io/ComposeCreativeDemo/).

## What it is

A little **Kotlin-Compose Multiplatform** playground featuring a growing set of creative-coding demos built around **balls**, **motion**, and **audio**.

### Pendulums

- The **small pendulums** swing together from the **same shared angle**. The **big** ones swing **independently**, each with its own **thread length**.
- Crossing the center line triggers a synchronized glow pulse along with a sound in tune to **musical scales**, occasionally shifting between scale modes.
- An airy pad track plays in the **background**, one I wrote myself.

### Balls of Fury

- The balls beep in tune to **musical scales**, occasionally shifting between scale modes.
- The **background music** is a piece I wrote myself, to accompany the balls' sounds.
- Speed quietly ramps every 15 seconds, cycling between **SLOW**, **MEDIUM**, and **FAST**.

### Shared across both

- **A fixed virtual viewport** (borrowed straight from libGDX's `FitViewport`) keeps each scene's proportions locked, whether you're rotating a phone into portrait or dragging a desktop window around.
- **A debug overlay** shows the title, live FPS, and whatever else is demo-specific — like which scale is currently playing.
- The same code runs, unchanged, on **Android (APK)**, **desktop (a cross-platform fat JAR)**, and the **web (Kotlin/Wasm)** thanks to Compose Multiplatform. Check the Releases tab for downloadable builds.

## Built with

Kotlin Multiplatform, Compose Multiplatform's Canvas API, and a hand-rolled `GameLoopCanvas`, a small reusable `withFrameNanos` + delta-time game loop that gives every new demo frame timing and FPS tracking for free, instead of reimplementing it each time.

Audio is genuinely three different stacks under one shared interface: **Howler.js** on web, **LWJGL + OpenAL** with hand-rolled OGG streaming on desktop, and **SoundPool** on Android plus entirely too much trial and error getting all three to preload correctly and loop correctly.

## Screenshot

<img width="1782" height="1106" alt="Screenshot From 2026-09-15 15-56-41" src="https://github.com/user-attachments/assets/abca8673-c472-45ef-8f64-8113bec5f5df" />

<img width="995" height="711" alt="Screenshot From 2026-09-07 01-29-37" src="https://github.com/user-attachments/assets/ec032c95-d3a0-452a-b688-cc5bbb35c676" />
