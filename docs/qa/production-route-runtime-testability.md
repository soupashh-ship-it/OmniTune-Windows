# Production route runtime testability

Date: 2026-07-14

Scope: final local Compose-runtime focus pass. This map exists to prevent unsafe UI tests from accidentally starting VLC, network discovery, downloads, tray integration, or real filesystem work.

## Library

- Production composable: `LibraryView(player: PlayerViewModel)`
- Direct parameters: `PlayerViewModel`
- Indirect dependencies: `PlayerViewModel` state flows for liked IDs, queue, current song, playback state, discovery trending/new, saved queue playlists, pinned collections
- PlayerViewModel members consumed: `likedSongs`, `queue`, `currentSong`, `playbackState`, `discoveryTrending`, `discoveryNew`, `savedQueuePlaylists`, `pinnedLibraryCollections`, plus playback/queue callbacks
- External side effects triggered by mounting a real ViewModel: `PlayerViewModel.init` wires VLC lifecycle and starts discovery loading
- Current blocker to full route runtime UI test: concrete `PlayerViewModel` requires concrete `VlcjAudioEngine`, `YouTubeService`, `SettingsRepository`, `OmniDownloadManager`
- Smallest safe test seam: extract or expose an internal production `LibraryReferenceContent` state/action surface, or add a real test ViewModel seam later
- Real production composable mounted this pass: no full route; lower production route not changed

## Playlist Detail

- Production composable: `PlaylistDetailView(player: PlayerViewModel)`
- Direct parameters: `PlayerViewModel`
- Indirect dependencies: Koin-injected `YouTubeService`, current playlist state, queue/playback/download callbacks
- PlayerViewModel members consumed: current playlist ID/loading/error, playback state, queue actions, download actions, liked IDs
- External side effects triggered by mounting: provider playlist fetch through `YouTubeService`; real ViewModel also risks VLC/discovery initialization
- Current blocker: route loads provider data and requires concrete ViewModel/service
- Smallest safe test seam: expose internal production `PlaylistDetailReferenceContent` with deterministic state/actions, or add service/test ViewModel seam later
- Real production composable mounted this pass: no full route

## Now Playing

- Production composable: `NowPlayingView(player, currentSong, playbackState, pos, volume)`
- Direct parameters: `PlayerViewModel`, `SongItem?`, `PlaybackState`, `PlayerPosition`, `Int`
- Indirect dependencies: `lyricsResult`, repeat/shuffle, queue, related content, seek/playback callbacks
- PlayerViewModel members consumed: `lyricsResult`, `repeatMode`, `shuffleMode`, `queue`, `queueIndex`, `discoveryRelated`, `seek`, `previousTrack`, `togglePlayPause`, `nextTrack`, `toggleShuffle`, `cycleRepeat`
- External side effects triggered by mounting a real ViewModel: VLC lifecycle and discovery loading
- Current blocker: controls are coupled to concrete `PlayerViewModel`
- Smallest safe test seam: expose internal production transport/action components that already accept callbacks; avoid full ViewModel until a playback interface exists
- Real production composable mounted this pass: transport lower component only where exposed/tested

## Downloads

- Production composable: `DownloadsView(player: PlayerViewModel)`
- Direct parameters: `PlayerViewModel`
- Indirect dependencies: Koin `PlatformContext`, download task state, quality setting, pause/resume/retry/delete/play callbacks
- PlayerViewModel members consumed: `downloadTasks`, `downloadQuality`, `setDownloadQuality`, `pauseDownload`, `resumeDownload`, `retryDownload`, `deleteDownload`, `playDownload`
- External side effects triggered by mounting a real ViewModel: download manager state, filesystem paths, real ViewModel VLC/discovery initialization
- Current blocker: route requires concrete ViewModel and platform context
- Smallest safe test seam: expose internal production `DownloadsReferenceContent` with deterministic task state/actions, or add safe manager/ViewModel seam later
- Real production composable mounted this pass: no full route

## Mini Player

- Production composable: `OmniMiniPlayer(player, currentSong, playbackState, position, volume)`
- Direct parameters: `PlayerViewModel`, current media state
- Indirect dependencies: navigation/playback/volume callbacks
- PlayerViewModel members consumed: `previousTrack`, `togglePlayPause`, `nextTrack`, `setVolume`, `navigateTo`
- External side effects triggered by mounting real ViewModel: concrete VLC/discovery
- Current blocker: control callbacks tied to concrete ViewModel
- Smallest safe test seam: expose/use internal production mini transport controls with callback counters
- Real production composable mounted this pass: mini transport lower component where exposed/tested

## Bottom Player

- Production composable: `OmniBottomPlayer(player, currentSong, playbackState, position, volume)`
- Direct parameters: `PlayerViewModel`, current media state
- Indirect dependencies: shuffle/repeat/liked flows and playback, seek, navigation callbacks
- PlayerViewModel members consumed: `shuffleMode`, `repeatMode`, `likedSongs`, `toggleShuffle`, `previousTrack`, `togglePlayPause`, `nextTrack`, `cycleRepeat`, `seek`, `setVolume`, `navigateTo`
- External side effects triggered by mounting real ViewModel: concrete VLC/discovery
- Current blocker: full route requires concrete ViewModel
- Smallest safe test seam: expose/use internal production `PlayerControlBand`, which already accepts state/actions and is used by `OmniBottomPlayer`
- Real production composable mounted this pass: bottom player control band where exposed/tested
