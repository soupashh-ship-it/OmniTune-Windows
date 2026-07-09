# Phase 4 & 5 — Domain Models & Stream Resolution

**Status**: COMPLETED

## Verification
- **Domain Models**: Defined `Track`, `Album`, `Artist`, `QueueEntity`, `Playlist`, `Lyrics`, and `OmniPlayer` interface in `com.omnitune.windows.models` and `com.omnitune.windows.playback`.
- **Stream Resolution**: Directly ported the `innertube` module from `D:\code\omnitune\innertube` to `D:\Omnitune Windows\src\jvmMain\kotlin\com\omnitune\innertube`. 
- **Dependencies Adjusted**: Added Ktor (`3.0.0` for Kotlin `2.1.0` compatibility), Brotli, NewPipe extractor, Re2j, and Rhino to `build.gradle.kts`.
- **Compilation**: Added a `YouTube.search()` call inside a `LaunchedEffect` in `Main.kt` as a sanity test. Project compiles perfectly.

## Protection Check
- **Android Path**: `D:\code\omnitune` remains completely untouched.
- **Windows Path**: `D:\Omnitune Windows` now contains a self-contained InnerTube networking client.
