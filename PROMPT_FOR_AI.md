# OmniTune Windows — AI Developer Prompt

## Overview
OmniTune is an open-source YouTube Music player for Windows, built with **Compose Multiplatform Desktop** + **VLCj** audio engine. It's a port of the Android OmniTune app (reference at `D:\code\omnitune`). Most Android modules (8 JVM sub-projects) are copied AS-IS — only the desktop UI under `composeApp/src/desktopMain/` needs work.

## Tech Stack
- **Compose Multiplatform 1.8.0** + **Kotlin 2.1.20**
- **Gradle 8.12**, **JDK 21 Temurin**
- **Koin 4.1** for DI, **Ktor 3.1** for HTTP
- **VLCj 4.11** + **VLC 3.0.21** at `C:\Program Files\VideoLAN\VLC`
- **Coil 3** for async image loading
- **Java Preferences API** for settings persistence

## Project Structure (desktop UI only — the rest is shared JVM modules)

```
composeApp/src/desktopMain/kotlin/com/omnitune/app/
├── Main.kt                          # Entry point, window, tray, VLC cleanup
├── di/
│   └── PlatformModule.kt            # Koin DI: YouTubeService, VlcjAudioEngine, SettingsRepository, PlayerViewModel
├── platform/
│   ├── VlcjAudioEngine.kt           # VLC wrapper: play/pause/seek/volume, PlaybackState, PlayerPosition, onTrackFinished callback
│   ├── SettingsRepository.kt        # Java Preferences: volume, windowWidth, windowHeight
│   ├── PlatformContext.kt, SmtcManager.kt, EqualizerManager.kt  # (stubs)
├── player/
│   └── PlayerViewModel.kt           # State: nav, search, queue, playback, volume. Actions: playSong, search, nextTrack, etc.
├── service/
│   └── YouTubeService.kt            # Wraps InnerTube API: search, getPlayer
└── window/
    ├── OmniTuneTheme.kt             # THEME — all color/spacing/shape/motion/glass constants
    ├── OmniWindow.kt                # Main layout: sidebar + AnimatedContent views + PlaybackBar
    ├── Sidebar.kt                   # 200dp nav: Search, NowPlaying, Queue, Playlists, Settings
    ├── NowPlayingView.kt            # Full-screen player: album art, seek, controls, volume
    ├── QueueView.kt                 # Queue list: play/remove items
    ├── PlaylistsView.kt             # Search/browse YouTube Music playlists
    ├── SettingsView.kt              # App info, version, credits
    └── SystemTrayManager.kt         # System tray icon/menu
```

## Current Design System (OmniTuneTheme.kt)

### Architecture
Android inspired: `Spacing`, `Shapes`, `OmniMotion`, `OmniColors`, `GlassSurfaceStyle`, `GlassDefaults` objects + `OmniGlassSurface` composable. All UI code uses these constants instead of hardcoded values.

### Colors
- **Accent**: Lavender `#8B8FFF` (Android match)
- **Background**: `#060912` (deep dark blue-black)
- **Surfaces**: `#0C101A` (surface), `#131928` (card), `#0A0E18` (elevated)
- **Text**: `#F2F3F8` primary, `#A8B0C4` secondary, `#7A8299` dim
- **Glass**: Subtle white overlays at 1%/2%/4% alpha
- **Error**: `#FF6363`

### Spacing
`micro=4dp`, `compact=8dp`, `small=12dp`, `medium=16dp`, `large=20dp`, `section=24dp`, `hero=32dp`, `screen=40dp`

### Shapes (RoundedCornerShape)
`tiny=6dp`, `small=10dp`, `medium=14dp`, `large=20dp`, `extraLarge=28dp`, `artworkSmall=10dp`, `artworkMedium=16dp`, `artworkLarge=24dp`, `player=28dp`, `dock=24dp`, `pill=999dp`

### Motion (OmniMotion)
- `fastFadeMs=140`, `screenTransitionMs=220`, `sectionTransitionMs=320`
- `pressSpring()`: MediumBouncy + Medium stiffness
- `gentleSpring()`: NoBouncy + MediumLow stiffness
- `screenTween()`: FastOutSlowInEasing

### Glass Surface
`GlassSurfaceStyle(surfaceTint, surfaceAlpha, overlayColor?, overlayAlpha, borderColor, borderAlpha, borderWidth, shadowElevation)` + `OmniGlassSurface(shape, style, modifier, content)` composable.
Pre-built: `GlassDefaults.miniPlayer`, `GlassDefaults.card`, `GlassDefaults.navBar`.
**IMPORTANT**: `OmniGlassSurface` uses `Box` under the hood — do NOT put `Modifier.fillMaxSize()` inside it as the immediate child (causes height expansion bugs). Use `Surface` with `BgElevated` + `shadowElevation` instead for now.

## Current Features (Working)
- Search YouTube Music (Enter key or button), tap song → plays audio via VLCj
- Play/Pause, seek slider, volume slider
- Keyboard shortcuts: Space=toggle, Left/Right=±5s, N=next track, P=previous track
- Track queue: playNext, addToQueue, removeFromQueue, clearQueue
- Auto-play next track on track finish
- Configuration persistence (volume, window size)
- VLCj graceful shutdown on quit
- High-res thumbnails via `toHighResThumbnail()`
- Sidebar nav: Search, Now Playing, Queue, Playlists, Settings
- AnimatedContent transitions between views
- Apple Music–inspired dark theme (just updated to Android lavender accent)

## Critical Rules (DO NOT BREAK)
1. **Only edit files under `composeApp/src/desktopMain/kotlin/com/omnitune/app/`** — the JVM sub-modules (`innertube`, `kugou`, `lrclib`, `lastfm`, `simpmusic`, `betterlyrics`, `kizzy`, `canvas`) are copied AS-IS from Android and must NOT be modified.
2. **Use theme constants everywhere** — never hardcode colors, shapes, spacing, or dp values. Import from `com.omnitune.app.window.*` (e.g., `Spacing.small`, `Shapes.medium`, `BgCard`, `TextGray`).
3. **Build with** `.\gradlew.bat :composeApp:compileKotlinDesktop` (run after every change to verify).
4. **Keep existing layout structure** — `OmniWindow.kt` uses `Column { Row(weight=1f) { Sidebar + Content } + PlaybackBar }`. Don't restructure this.
5. **Don't change DI, services, platform layer** unless asked. Only polish UI files in `window/` and `player/PlayerViewModel.kt`.

## What Needs Work (Priority Order)

### Phase 1 — Visual Overhaul (urgent)
- **NowPlayingView.kt**: Still basic. Add animated artwork scale on play/pause, persistent controls layout, gradient background that reacts to album art colors (static radial gradient is OK as fallback), show remaining time properly. Currently uses emoji for play/pause — replace with proper Material icons.
- **PlaybackBar** (in OmniWindow.kt): Still basic `Surface`. Convert to glass-style `OmniGlassSurface` (but fix the `fillMaxSize()` bug — use `Modifier.heightIn(max=80.dp)` instead). Add spring-animated progress bar (already wired), show remaining time, add swipe gestures.
- **Sidebar.kt**: Text-only nav items. Add small artwork thumbnail for "Now Playing" when a song is active. Active indicator should be more prominent. Add divider between nav and settings.
- **SearchView** (in OmniWindow.kt): Results are flat list. Add loading shimmer placeholders. Show search suggestions/recents. Results should have proper duration formatting.
- **QueueView.kt**: Show "now playing" highlight more clearly. Add drag-to-reorder. Show album art thumbnails instead of numbered boxes.
- **PlaylistsView.kt**: Show grid layout instead of list. Empty state with icon. Click playlist → `player.searchPlaylists(query)` is already wired.

### Phase 2 — Polish & Micro-interactions
- Press-scale animation on buttons (`OmniMotion.pressSpring()`)
- `AnimatedContent` transitions between screens (already wired with fade)
- `PlayingIndicator` — animated dots for active song
- Shimmer loading placeholders for search results
- Consistent empty states with icons
- Proper formatting: durations, counts, relative time

### Phase 3 — Features
- **Lyrics**: Wire `betterlyrics`/`lrclib` modules in NowPlayingView
- **LastFM scrobbling**: Wire `lastfm` module
- **Equalizer**: Wire `EqualizerManager.kt`
- **Playlist detail**: Click playlist → fetch its songs → show list → tap to play
- **Audio visualizer**: Wire `canvas` module

## Android Reference Patterns (in D:\code\omnitune)

### Key files to study (read-only, do NOT edit):
- `app/src/main/kotlin/com/omnitune/app/ui/component/Items.kt` — `ListItem`, `GridItem`, `ItemThumbnail`, `SongListItem`, `PlayingIndicatorBox`
- `app/src/main/kotlin/com/omnitune/app/ui/player/MiniPlayer.kt` — Glass MiniPlayer with spring progress, swipe gestures, dynamic accent colors
- `app/src/main/kotlin/com/omnitune/app/ui/player/PlayerScreen.kt` — Full player with artwork, lyrics, queue, dynamic background
- `app/src/main/kotlin/com/omnitune/app/ui/component/SearchBar.kt` — Animated search bar with suggestions
- `app/src/main/kotlin/com/omnitune/app/ui/component/EmptyPlaceholder.kt` — Empty states
- `app/src/main/kotlin/com/omnitune/app/ui/component/PlayingIndicator.kt` — Animated playing dots
- `app/src/main/kotlin/com/omnitune/app/ui/component/OmniTuneLoader.kt` — Loading spinner
- `app/src/main/kotlin/com/omnitune/app/ui/theme/OmniColors.kt` — Full color system
- `app/src/main/kotlin/com/omnitune/app/ui/theme/GlassEffectDefaults.kt` — Glass surface styles (with Android blur — skip blur for desktop, use tints only)
- `app/src/main/kotlin/com/omnitune/app/ui/player/PlayerBackgroundEffect.kt` — Dynamic background from album art (use static gradient as fallback on desktop)

## Key Files Current State

### OmniTuneTheme.kt (fully refactored, do NOT change unless adding colors)
All constants: `AccentLavender`, `BgDark`, `BgSurface`, `BgCard`, `BgCardHover`, `BgElevated`, `TextWhite`, `TextGray`, `TextDim`, `BorderColor`, `SurfaceHairline`, `OmniColors`, `Spacing`, `Shapes`, `OmniMotion`, `GlassSurfaceStyle`, `GlassDefaults`, `OmniGlassSurface`, `OmniTuneTypography`, `OmniTuneTheme`.

### OmniWindow.kt (main layout — keep structure)
- `OmniWindow()`: `Column { Sidebar + AnimatedContent + MacPlaybackBar }` with keyboard shortcuts
- Contains `SearchView()`, `MacSongRow()`, `MacPlaybackBar()` as private composables
- `MacPlaybackBar()` needs glass-style makeover (currently Surface with BgElevated)
- `MacSongRow()` uses `Shapes.small`, `Spacing.compact`, `Spacing.small`, `Shapes.artworkSmall`

### PlayerViewModel.kt (working, minor additions OK)
State: `navScreen`, `searchResults/loading/error`, `currentSong`, `streamUrl`, `volume`, `playbackState`, `position`, `playerError`, `queue`, `queueIndex`
Actions: `search()`, `searchPlaylists()`, `playSong(item, index)`, `playQueueIndex(index)`, `nextTrack()`, `previousTrack()`, `addToQueue()`, `playNext()`, `removeFromQueue()`, `clearQueue()`, `togglePlayPause()`, `seek()`, `seekRelative()`, `setVolume()`

### VlcjAudioEngine.kt (working, minor additions OK)
State: `playbackState`, `position`, `error`, `onTrackFinished` callback
Actions: `play(url)`, `pause()`, `resume()`, `stop()`, `seek(timeMs)`, `seekRelative(deltaMs)`, `setVolume(vol)`, `setRate(rate)`, `release()`

## Common Issues / Gotchas
- **VLC path**: `C:\Program Files\VideoLAN\VLC` — set via `System.setProperty("jna.library.path", ...)` in Main.kt
- **YouTube streams**: `ANDROID_VR_NO_AUTH` client works; `WEB_REMIX`/`WEB` return UNPLAYABLE. 5 clients tried in order.
- **Signature cipher**: URL cipher parsing is naive — works for now, will break when YouTube changes format.
- **poToken/visitorData**: Hardcoded on init — may expire. Refresh logic not implemented.
- **Window size**: Persisted via `SettingsRepository` (Java Preferences). Loaded/saved, not forced.
- **Build**: Run `.\gradlew.bat :composeApp:compileKotlinDesktop` to check. Full run: `.\gradlew.bat :composeApp:run`.
- **VLCj threading**: All VLC operations go through `vlcDispatcher` (single-thread executor). Don't call VLC APIs directly from UI thread.

## How to Approach Changes
1. Read the full file before editing
2. Make small, focused changes (one view at a time)
3. Build after each change
4. Never refactor just for style — only change what improves the user experience
5. Use theme constants (Spacing.*, Shapes.*) never raw dp values
6. Prefer existing patterns over creating new abstractions
7. When in doubt about design, check the Android reference project

## Files NOT to touch (ever)
- Any file in `D:\Omnitune Windoww\composeApp\src\commonMain\` — Android shared code
- Any file in sub-module directories: `innertube/`, `kugou/`, `lrclib/`, `lastfm/`, `simpmusic/`, `betterlyrics/`, `kizzy/`, `canvas/`
- `PlatformContext.kt`, `EqualizerManager.kt`, `SmtcManager.kt` — platform stubs
- `build.gradle.kts` or `settings.gradle.kts` — dependency configuration
- `PlatformModule.kt` — DI wiring (unless adding a new dependency)

## Appendix: Legacy TODO (`left stuff to do.md`)

Some items below are already done — included for completeness:

```
[✓ = done, blank = still needs work]

Audio & Playback:
- [✓] Volume slider
- [✓] Next/Previous track with queue
- [✓] Track queue / playlist management
- [ ] Repeat / Shuffle modes
- [ ] Proper signatureCipher/cipher deobfuscation

UI:
- [✓] Album art in song rows and playback bar
- [✓] Now-playing view with large art
- [✓] Volume controls
- [✓] Keyboard shortcuts (Space, arrows, N/P)
- [✓] Search result polish (duration, artist, art)
- [ ] Loading states (shimmer / buffering indicator)
- [ ] Error states

Features:
- [ ] Playlist detail browsing
- [ ] Lyrics (betterlyrics/lrclib)
- [ ] LastFM scrobbling
- [ ] Equalizer
- [ ] Audio visualizer

Infrastructure:
- [✓] Configuration persistence (volume, window size)
- [✓] Graceful VLCj shutdown
- [✓] High-res thumbnails
- [ ] Logging (replace println with SLF4J)
- [ ] poToken/visitorData refresh
```

