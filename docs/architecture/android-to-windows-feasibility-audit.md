# Android-to-Windows Feasibility Audit

## Overview
This document outlines the architectural differences between the existing Android OmniTune application and the target Windows desktop application, categorizing components based on portability and identifying necessary desktop replacements.

## Current Android Architecture
- **UI Framework:** Jetpack Compose.
- **Dependency Injection:** Hilt (Dagger).
- **Database:** Room (SQLite).
- **Playback Engine:** AndroidX Media3 (ExoPlayer).
- **Networking:** Ktor Client & OkHttp.
- **Stream Resolution:** Custom `innertube`, `simpmusic`, `lastfm`, etc., subprojects.

## Component Classification

### DIRECTLY_PORTABLE
- **Domain Models:** `Track`, `Album`, `Artist`, `Playlist`, `QueueEntity`.
- **Networking Logic:** `innertube` API client, Last.fm client (assuming pure Kotlin Ktor/Serialization).
- **State Holders / Logic:** Parts of `Queue` and `PlaylistPlaybackPlanner` (minus Android specifics).

### PORTABLE_WITH_SMALL_CHANGES
- **Compose UI:** Most `Screen.kt` files can be ported. Changes required: remove `Modifier.statusBarsPadding()`, `BackHandler`, and Android navigation components. Replace with desktop navigation (e.g., Voyager or Precompose) and desktop-optimized layouts.
- **ViewModels:** Convert from `androidx.lifecycle.ViewModel` to standard Kotlin classes or KMP ViewModel libraries.

### REQUIRES_DESKTOP_REPLACEMENT
- **Dependency Injection:** Hilt -> **Koin** or **Manual DI**. Hilt is strictly tied to Android's `Application` and `Activity` lifecycle.
- **Database:** Room -> **SQLDelight**. Room relies on Android SQLite bindings (though Room KMP exists, SQLDelight is more mature for pure JVM desktop).
- **Playback Engine:** Media3 -> **VLCJ** or **JavaFX Media**. Media3 cannot run on standard JVM. We will extract an `OmniPlayer` interface and provide a desktop implementation.

### HIGH_RISK
- **Stream Resolution & Playback:** InnerTube streams use various formats (Opus, M4A). The desktop player must support these. ExoPlayer does this well; VLCJ is the best desktop equivalent but requires native VLC binaries.
- **Navigation:** Android Navigation Compose is complex to port. We will use a desktop-friendly router.

## Recommended Architecture
- **`core-domain`**: Pure Kotlin models and interfaces.
- **`core-data`**: SQLDelight database, Ktor network clients.
- **`core-player`**: `OmniPlayer` interface and `vlcj` implementation.
- **`app-desktop`**: Compose Multiplatform desktop entry point (`Main.kt`), UI screens, DI wiring, and shell layout.

## Next Steps
Proceed to Phase 2: Bootstrap the desktop Gradle project, set up Compose Desktop, and verify basic window launch.
