# OmniTune Windows Current Project Map

Audit date: 2026-07-16  
Project root: `D:\Omnitune Windoww`  
Branch at audit start: `main`  
HEAD at audit start: `951d421` (`refactor: complete player and settings responsibility split`)

## Repository state observed

- Remote: `origin https://github.com/soupashh-ship-it/OmniTune-Windows.git`
- Worktree at audit start: clean except existing untracked `docs/qa/screenshots/`
- Existing untracked screenshot evidence: 20 PNG files, about 5.7 MB
- No production code was modified for this audit.

## Modules

Configured in `settings.gradle.kts`:

- `:composeApp` — Compose Desktop Windows application
- `:innertube` — YouTube/InnerTube provider implementation
- `:kugou`
- `:lrclib`
- `:lastfm`
- `:simpmusic`
- `:betterlyrics`
- `:kizzy`
- `:canvas`

## Build and packaging

Primary build files:

- `settings.gradle.kts`
- `build.gradle.kts`
- `composeApp/build.gradle.kts`
- `gradle/libs.versions.toml`
- `gradle.properties`

Desktop app packaging is configured in `composeApp/build.gradle.kts`:

- Main class: `com.omnitune.app.MainKt`
- JVM toolchain: 21
- Native distribution formats: MSI and EXE
- Package name: `OmniTune`
- Package version source: `omnitune.version=0.2.0`
- Windows upgrade UUID: `7a8b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d`
- VLC bundling source: `VLC_HOME` or `C:/Program Files/VideoLAN/VLC`

## Application entry and shell

- Entry point: `composeApp/src/desktopMain/kotlin/com/omnitune/app/Main.kt`
- Window root: `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/OmniWindow.kt`
- Theme system: `OmniTuneTheme.kt`, `HomeReferenceMetrics.kt`, `NocturneBackdrop.kt`
- Global shell:
  - `Sidebar.kt`, `SidebarNav.kt`, `SidebarPlaylists.kt`
  - `OmniTopBar.kt`
  - `OmniBottomPlayer.kt`, `OmniBottomTransport.kt`, `OmniBottomMetadata.kt`, `OmniBottomProgress.kt`
  - `OmniMiniPlayer.kt`
- Responsive helper currently present: `OmniResponsiveLayout.kt`

## Dependency injection

- DI module: `composeApp/src/desktopMain/kotlin/com/omnitune/app/di/PlatformModule.kt`
- Major injected services:
  - `SettingsRepository`
  - `YouTubeService`
  - `VlcjAudioEngine`
  - `OmniDownloadManager`
  - `PlayerViewModel`
  - `PlatformContext`
  - `SmtcManager`

## Player and orchestration layer

Primary facade:

- `composeApp/src/desktopMain/kotlin/com/omnitune/app/player/PlayerViewModel.kt`

Extracted controllers currently present:

- `PlayerNavigationController.kt`
- `PlayerPlaylistController.kt`
- `PlayerQueueController.kt`
- `PlayerSearchController.kt`
- `PlayerDownloadController.kt`
- `PlayerRadioController.kt`
- `PlayerRelatedController.kt`
- `PlaybackRequestGate.kt`
- `RadioSessionPolicy.kt`
- `RelatedContentPolicy.kt`

Ownership observed:

- `PlayerViewModel` still coordinates the native audio engine, current song state, listen tracking, liked/followed state, library pins, discovery loading, navigation handoff, and controller delegation.
- `PlayerQueueController` owns queue list, queue index, shuffle, repeat, queue mutation, and queue reordering.
- `PlayerSearchController` owns search state, playlist-search state, request cancellation, and latest-token checks.
- `PlaybackRequestGate` guards stale async stream-resolution results using request tokens and current-song identity.
- `PlayerDownloadController` bridges player actions to `OmniDownloadManager`.
- `PlayerRadioController` owns active radio session identity and continuation loading.
- `PlayerRelatedController` owns related-content loading with token/current-song validation.

## Media engine

- Native audio engine: `composeApp/src/desktopMain/kotlin/com/omnitune/app/platform/VlcjAudioEngine.kt`
- Runtime VLC selection/extraction: `NativeRuntime.kt`
- SMTC bridge: `SmtcManager.kt`

Observed media lifecycle:

- VLC runtime is selected from packaged resources, embedded resources, working directory, `VLC_HOME`, or system VLC.
- `VlcjAudioEngine` owns `MediaPlayerFactory`, media player, event listeners, a position poller, and release logic.
- A shutdown hook calls `releaseSync()`.

## Provider/network layer

- App service wrapper: `composeApp/src/desktopMain/kotlin/com/omnitune/app/service/YouTubeService.kt`
- Provider implementation: `innertube/src/main/kotlin/com/omnitune/innertube/YouTube.kt`
- Network client/core: `innertube/src/main/kotlin/com/omnitune/innertube/InnerTube.kt`

Provider responsibilities include search, home, charts, mood/genres, browse pages, next/radio, related content, and player stream metadata.

## Downloads and offline playback

- Download manager: `composeApp/src/desktopMain/kotlin/com/omnitune/app/platform/OmniDownloadManager.kt`
- Download model and states are in the same file.
- Download metadata index: `%LOCALAPPDATA%\OmniTuneData\downloads-index.json`
- Download files root: `%LOCALAPPDATA%\OmniTuneData\downloads`
- Atomic write utility: `AtomicFileStore.kt`

Verified source-level safeguards:

- Completed local files are only trusted if inside managed download root.
- Delete only removes files passing `isManagedDownloadFile`.
- Download and partial filenames use `safeFileName`.
- Download index writes use `AtomicFileStore`.
- Corrupt index preservation and backup recovery exist.

## Persistence

Primary facade:

- `SettingsRepository.kt`

Extracted persistence helpers currently present:

- `SettingsPreferences.kt`
- `JsonFileStore.kt`
- `SongJsonCodec.kt`
- `PlaylistPersistence.kt`
- `PlaylistPersistenceRules.kt`
- `LikedSongsPersistence.kt`
- `PlaybackHistoryPersistence.kt`
- `AtomicFileStore.kt`

Persistent state includes:

- preferences, theme, reduced motion, volume, shuffle, repeat, download quality
- playlists and playlist tracks
- liked songs and liked timestamps
- playback history and sessions
- pinned library collections
- followed artists
- search history
- download metadata

## Screens

Major screen files:

- Home: `screens/HomeView.kt`
- Browse and Radio: `screens/Screens.kt`
- Search: `screens/SearchView.kt`, `SearchPanels.kt`, `SearchComponents.kt`
- Library: `screens/LibraryView.kt`
- Playlists index: `PlaylistsView.kt`
- Playlist detail: `PlaylistDetailView.kt`, `PlaylistDetailHero.kt`, `PlaylistDetailTrackList.kt`, `PlaylistDetailSheets.kt`, `PlaylistDetailHelpers.kt`, `PlaylistDetailLoading.kt`
- Liked Songs: `LikedSongsView.kt`, `LikedSongsTable.kt`, `LikedSongsActions.kt`, `LikedSongsSheets.kt`
- Album: `screens/AlbumView.kt`
- Artist: `screens/ArtistView.kt`
- Now Playing/Lyrics/Related: `NowPlayingView.kt`, `NowPlayingLyrics.kt`, `NowPlayingPlayerRegion.kt`, `NowPlayingRelated.kt`
- Queue and session: `QueueView.kt`
- Downloads: `screens/DownloadsView.kt`
- Settings: `SettingsView.kt`

## Test roots

Desktop tests:

- `composeApp/src/desktopTest/kotlin`

Provider tests:

- `innertube/src/test/kotlin`

Current observed test categories include:

- atomic file store
- settings repository
- playlist persistence rules
- download manager safety
- VLC engine
- SMTC mapping
- platform context migration
- provider runtime QA
- offline playback runtime QA
- queue controller
- navigation controller
- radio policy
- related policy
- keyboard routing
- focus traversal
- responsive layout helper
- home reference model
- provider fixtures

## Largest files still present

Files at or above 500 lines:

- `innertube/.../YouTube.kt` — 1283 lines
- `HomeView.kt` — 927 lines
- `LibraryView.kt` — 795 lines
- `PlayerViewModel.kt` — 778 lines
- `SearchPanels.kt` — 759 lines
- `Screens.kt` — 748 lines
- `SettingsView.kt` — 702 lines
- `QueueView.kt` — 695 lines
- `innertube/.../InnerTube.kt` — 665 lines
- `ArtistView.kt` — 575 lines
- `OmniDownloadManager.kt` — 568 lines
- `DownloadsView.kt` — 552 lines
- `OmniComponents.kt` — 548 lines
- `OmniTuneTheme.kt` — 545 lines

Size alone is not a defect, but these files define the current maintainability hotspots.
