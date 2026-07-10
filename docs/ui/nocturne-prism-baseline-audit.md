# Nocturne Prism — Baseline Audit (Phase 0)

**Project path**: `D:\Omnitune Windoww` (note: user wrote `D:\Omnitune Windows`; real dir is `Windoww`).
**Branch**: `ui/nocturne-prism-reference-rebuild` (created from `master` @ `9d8e350`).
**Baseline build**: `.\gradlew.bat :composeApp:compileKotlinDesktop` → BUILD SUCCESSFUL.

## Stack
- Compose Multiplatform Desktop (JVM) + VLCj 4.11 (audio engine), Koin 4.1 DI, Ktor 3.1, Coil 3 (images).
- Kotlin 2.1.20, Compose 1.8.0, Gradle 8.12, JDK 21 Temurin.
- VLC 3.0.21 at `C:\Program Files\VideoLAN\VLC`.

## Architecture
```
Main.kt
  application { Window { OmniWindow() } + Tray }   // window size persisted via SettingsRepository
player/PlayerViewModel.kt   // Koin singleton; all UI StateFlows + actions
platform/VlcjAudioEngine.kt // play/pause/seek/volume/position; onTrackFinished
platform/SettingsRepository.kt // Java Preferences: volume, window size, (extend: liked, history, recents, appearance)
di/PlatformModule.kt        // wires YouTubeService, VlcjAudioEngine, SettingsRepository, PlayerViewModel
service/YouTubeService.kt   // wraps com.omnitune.innertube.YouTube (search, getPlayer, searchPlaylists)
window/*.kt                 // OmniWindow, Sidebar, NowPlayingView, QueueView, PlaylistsView, SettingsView,
                            // OmniTuneTheme, PlayingIndicator, SystemTrayManager
```

## Current UI component hierarchy
- `OmniWindow`: `Column { Row(weight=1f){ Sidebar + AnimatedContent } + MacPlaybackBar }` + global key shortcuts.
- `Sidebar` (200dp): logo + 5 nav items (Search, NowPlaying, Queue, Playlists, Settings).
- `SearchView` (private in OmniWindow): search field + `LazyColumn` of `MacSongRow` / `ShimmerSongRow`.
- `NowPlayingView`: radial-gradient bg, scaling art, seek + volume sliders, play/pause.
- `QueueView`, `PlaylistsView`, `SettingsView`.
- `OmniTuneTheme`: full token system already close to Nocturne Prism.

## Reusable components to create (Phase 1)
`OmniSurface`, `OmniSidebar`, `OmniTopBar`, `OmniBottomPlayer`, `OmniMediaCard`, `OmniAlbumCard`,
`OmniArtistCard`, `OmniPlaylistCard`, `OmniSongRow`, `OmniSearchField`, `OmniIconButton`,
`OmniPrimaryButton`, `OmniSecondaryButton`, `OmniProgressSlider`, `OmniVolumeControl`,
`OmniSectionHeader`, `OmniContextMenu`, `OmniTooltip`, `OmniEmptyState`, `OmniLoadingPlaceholder`.

## Fragile areas (must not regress)
- VLCj client-fallback stream resolution (`PlayerViewModel.doPlay`, 5 clients).
- All VLC ops via `vlcDispatcher` (single-thread executor) — never call from UI thread.
- `OmniGlassSurface` uses `Box`; do NOT put `fillMaxSize()` immediate child (height bug). Use `Surface` + `shadowElevation`.
- Window size persistence + tray + graceful `release()`.

## Functionality that must not regress
Play/pause/seek/volume/next/prev, queue add/next/remove/clear/reorder, autoplay-next, search→play,
window persistence, tray minimize/quit, keyboard shortcuts (Space/Left/Right/N/P).

## Reference screen mapping (Nocturne Prism)
1 Home/Discover, 2 Search, 3 Library, 4 Playlist Detail, 5 Artist, 6 Album,
7 Now Playing + Lyrics, 8 Queue & Session, 9 Downloads + Settings, 10 Mini Player, 11 Browse/Radio.

## Data layer feasibility (read-only `innertube` module — call, never edit)
`YouTube.home/explore/newReleaseAlbums/moodAndGenres/album/albumSongs/artist/artistItems/`
`playlist/playlistContinuation/library/libraryRecentActivity/lyrics/related/queue/`
`searchSuggestions/likeVideo/subscribeChannel`. All unauth-browse work; personal library/likes/
downloads need auth + local storage → handled via local persistence + honest empty states.

## Build commands
- Verify: `.\gradlew.bat :composeApp:compileKotlinDesktop`
- Run: `.\gradlew.bat :composeApp:run`

## Risk list
- Model cannot open the PNG references → QA is code-vs-spec, not pixel-diff.
- No unit/lint framework configured → only `compileKotlinDesktop` is a reliable gate.
- Runtime live-data needs network (YT API) + VLC.
- Real-time blur is expensive → use static gradients per existing guidance.

## Phase 0 Completion Gate
- [x] project path confirmed (D:\Omnitune Windoww)
- [x] safety branch created
- [x] baseline build attempted (SUCCESS)
- [x] architecture understood
- [x] reference screens mapped
