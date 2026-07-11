# Nocturne Prism — Phase 7 Now Playing + Lyrics Implementation Map

## AREA: Now Playing Route
CURRENT FILE: composeApp/src/desktopMain/kotlin/com/omnitune/app/window/OmniWindow.kt
CURRENT COMPONENT: OmniWindow → NavScreen.NowPlaying case
STATE SOURCE: player.navScreen StateFlow
REFERENCE TARGET: NowPlaying route opens immersive full-screen player
REUSE STRATEGY: Existing nav case; no second route stack created
FUNCTIONAL DEPENDENCY: NavScreen.NowPlaying enum, PlayerViewModel.navigateTo()
RISK: LOW — route already wired
PLANNED CHANGE: NowPlayingView.kt complete rewrite

## AREA: Now Playing Root
CURRENT FILE: composeApp/src/desktopMain/kotlin/com/omnitune/app/window/NowPlayingView.kt
CURRENT COMPONENT: NowPlayingView (previously sparse)
STATE SOURCE: PlayerViewModel, PlaybackState, PlayerPosition
REFERENCE TARGET: Two-column layout: Player left | Lyrics right
REUSE STRATEGY: Full rewrite; no second player engine or sidebar
FUNCTIONAL DEPENDENCY: player, currentSong, playbackState, pos, volume
RISK: LOW
PLANNED CHANGE: Complete reconstruction per reference

## AREA: Artwork
CURRENT FILE: NowPlayingView.kt
CURRENT COMPONENT: AsyncImage inside Box with clip/shadow
STATE SOURCE: currentSong.thumbnail.toHighResThumbnail()
REFERENCE TARGET: Large square artwork, subtle shadow, artworkLarge radius
REUSE STRATEGY: Coil3 AsyncImage; same loading as Phase 3-6
FUNCTIONAL DEPENDENCY: toHighResThumbnail() extension
RISK: LOW
PLANNED CHANGE: Animate scale on play/pause; artworkLarge radius; iris ambient shadow

## AREA: Track Title
CURRENT FILE: NowPlayingView.kt
CURRENT COMPONENT: Text(currentSong.title)
STATE SOURCE: currentSong.title
REFERENCE TARGET: headlineMedium bold centered
REUSE STRATEGY: MaterialTheme typography
FUNCTIONAL DEPENDENCY: SongItem.title
RISK: NONE
PLANNED CHANGE: headlineMedium, FontWeight.Bold, centered, truncated

## AREA: Artist
CURRENT FILE: NowPlayingView.kt
CURRENT COMPONENT: Text(artists.joinToString)
STATE SOURCE: currentSong.artists
REFERENCE TARGET: titleMedium, TextSecondary
REUSE STRATEGY: Same pattern as Phase 5-6
FUNCTIONAL DEPENDENCY: SongItem.artists
RISK: NONE
PLANNED CHANGE: titleMedium, TextSecondary, 1 line ellipsis

## AREA: Album Context
CURRENT FILE: NowPlayingView.kt
STATE SOURCE: SongItem has no explicit album field
REFERENCE TARGET: Compact album label below artist
REUSE STRATEGY: Field not directly available; omitted honestly
FUNCTIONAL DEPENDENCY: None (SongItem doesn't carry album string)
RISK: LOW — honest omission
PLANNED CHANGE: NOT SHOWN (data unavailable)

## AREA: Explicit Badge
CURRENT FILE: NowPlayingView.kt
STATE SOURCE: SongItem has no explicit flag
REFERENCE TARGET: Compact E badge
REUSE STRATEGY: Omitted — no data source
FUNCTIONAL DEPENDENCY: None
RISK: NONE
PLANNED CHANGE: NOT SHOWN — honest omission

## AREA: Favorite
CURRENT FILE: NowPlayingView.kt
STATE SOURCE: PlayerViewModel.likedSongs
REFERENCE TARGET: Heart icon, toggleable
REUSE STRATEGY: NpIconButton component
FUNCTIONAL DEPENDENCY: PlayerViewModel.toggleLike()
RISK: MEDIUM — Like API not wired yet
PLANNED CHANGE: Visible but reports NOT SUPPORTED in QA (no YouTube Music like endpoint)

## AREA: Add/Library
CURRENT FILE: NowPlayingView.kt
STATE SOURCE: N/A
REFERENCE TARGET: Add icon
REUSE STRATEGY: NpIconButton
FUNCTIONAL DEPENDENCY: None (playlist add not implemented)
RISK: NONE — honest omission/placeholder

## AREA: Overflow
CURRENT FILE: NowPlayingView.kt
STATE SOURCE: N/A
REFERENCE TARGET: Three-dot overflow
REUSE STRATEGY: NpIconButton stub (real menu = future work)
FUNCTIONAL DEPENDENCY: DropdownMenu
RISK: LOW

## AREA: Playback Visualizer/Waveform
CURRENT FILE: NowPlayingView.kt
CURRENT COMPONENT: PlaybackVisualizer()
STATE SOURCE: isPlaying boolean derived from PlaybackState
REFERENCE TARGET: Waveform-like element above timeline
REUSE STRATEGY: Decorative animated bars — NOT claimed as real waveform
FUNCTIONAL DEPENDENCY: PlaybackState
RISK: NONE — documented as DECORATIVE
PLANNED CHANGE: 18-bar infinite-transition animated bars; animate opacity/height on play/pause

## AREA: Progress Timeline
CURRENT FILE: NowPlayingView.kt
CURRENT COMPONENT: OmniProgressSlider from Phase 1/2
STATE SOURCE: PlayerPosition (real engine state)
REFERENCE TARGET: Thin seekbar, current time left, total right
REUSE STRATEGY: OmniProgressSlider (already exists)
FUNCTIONAL DEPENDENCY: pos.position, pos.timeMs, pos.lengthMs, player.seek()
RISK: LOW — already tested in bottom player
PLANNED CHANGE: Seek drag state: UI owns position during drag; engine not queried until drag end

## AREA: Current Position
CURRENT FILE: NowPlayingView.kt
STATE SOURCE: PlayerPosition.timeMs (real engine)
REFERENCE TARGET: MM:SS left of slider
FUNCTIONAL DEPENDENCY: PlayerPosition
RISK: NONE

## AREA: Duration
CURRENT FILE: NowPlayingView.kt
STATE SOURCE: PlayerPosition.lengthMs (real engine)
REFERENCE TARGET: -MM:SS right of slider (remaining)
FUNCTIONAL DEPENDENCY: PlayerPosition
RISK: NONE

## AREA: Seek
CURRENT FILE: NowPlayingView.kt
STATE SOURCE: sliderPos local state during drag; player.seek() on release
REFERENCE TARGET: Stable seek without UI fight
REUSE STRATEGY: isDragging flag; UI owns scrub during drag only
FUNCTIONAL DEPENDENCY: player.seek(timeMs)
RISK: LOW — same pattern as bottom player
PLANNED CHANGE: isDragging bool; sliderPos local; seek dispatched only on drag end

## AREA: Play/Pause
CURRENT FILE: NowPlayingView.kt
CURRENT COMPONENT: Central circular button
STATE SOURCE: PlaybackState from PlayerViewModel
REFERENCE TARGET: 60dp circle, iris gradient, dark icon
REUSE STRATEGY: pressBounce interaction; OmniGradients.irisToLavender
FUNCTIONAL DEPENDENCY: player.togglePlayPause()
RISK: NONE

## AREA: Previous
CURRENT FILE: NowPlayingView.kt
CURRENT COMPONENT: TransportButton(SkipPrevious)
STATE SOURCE: N/A
REFERENCE TARGET: 44dp icon button
FUNCTIONAL DEPENDENCY: player.previousTrack()
RISK: NONE

## AREA: Next
CURRENT FILE: NowPlayingView.kt
CURRENT COMPONENT: TransportButton(SkipNext)
STATE SOURCE: N/A
REFERENCE TARGET: 44dp icon button
FUNCTIONAL DEPENDENCY: player.nextTrack()
RISK: NONE

## AREA: Shuffle
CURRENT FILE: NowPlayingView.kt
CURRENT COMPONENT: 36dp icon button with active dot indicator
STATE SOURCE: player.shuffleMode StateFlow
REFERENCE TARGET: Shuffle icon with iris tint when active
FUNCTIONAL DEPENDENCY: player.toggleShuffle(), player.shuffleMode
RISK: NONE — already exists in PlayerViewModel

## AREA: Repeat
CURRENT FILE: NowPlayingView.kt
CURRENT COMPONENT: 36dp icon button cycling OFF/ALL/ONE
STATE SOURCE: player.repeatMode StateFlow
REFERENCE TARGET: Repeat/RepeatOne icon with iris tint when active
FUNCTIONAL DEPENDENCY: player.cycleRepeat(), player.repeatMode
RISK: NONE — already exists in PlayerViewModel

## AREA: Lyrics Source
CURRENT FILE: lrclib/src/main/kotlin/com/omnitune/lrclib/LrcLib.kt
CURRENT COMPONENT: LrcLib.getLyrics() → Track object
STATE SOURCE: LrcLib API (lrclib.net)
REFERENCE TARGET: Real synced LRC or plain lyrics
REUSE STRATEGY: Existing LrcLib module; modified to return full Track not just synced string
FUNCTIONAL DEPENDENCY: network; song title, artist, duration
RISK: MEDIUM — availability depends on lrclib.net having the track

## AREA: Timed Lyric Model
CURRENT FILE: PlayerViewModel.kt (top-level)
CURRENT COMPONENT: data class LyricLine(val timeMs: Long, val text: String)
STATE SOURCE: parseLrc() output
REFERENCE TARGET: Per-line timestamp + text
REUSE STRATEGY: New top-level data class in same file
FUNCTIONAL DEPENDENCY: LRC string from LrcLib.Track.syncedLyrics
RISK: NONE

## AREA: Parser
CURRENT FILE: PlayerViewModel.kt (top-level fun parseLrc)
CURRENT COMPONENT: parseLrc(text: String?): List<LyricLine>
STATE SOURCE: LRC string
REFERENCE TARGET: Parse [mm:ss.xx], [mm:ss.xxx], multiple timestamps per line, metadata tags
REUSE STRATEGY: Complete rewrite from private to top-level; handles 2/3 digit sub-second part
FUNCTIONAL DEPENDENCY: LrcLib.Track.syncedLyrics
RISK: LOW — standard LRC format

## AREA: Lyric Cache
CURRENT FILE: PlayerViewModel.kt
CURRENT COMPONENT: lyricsJob + capturedId guard
STATE SOURCE: lyricsResult StateFlow
REFERENCE TARGET: No re-fetch on recomposition/resize
REUSE STRATEGY: lyricsResult persists in StateFlow; doPlay resets to Loading but loadLyrics only runs when URL resolves
FUNCTIONAL DEPENDENCY: PlayerViewModel coroutine scope
RISK: LOW — bounded by song count in session (not unbounded)

## AREA: Active Lyric Calculation
CURRENT FILE: NowPlayingView.kt — SyncedLyricsDisplay
CURRENT COMPONENT: derivedStateOf binary search over lines
STATE SOURCE: displayTimeMs (real engine position or drag scrub)
REFERENCE TARGET: Last line with timestamp <= current position
REUSE STRATEGY: Binary search for O(log n) per position update
FUNCTIONAL DEPENDENCY: LyricLine.timeMs, displayTimeMs
RISK: NONE — binary search handles backward seek correctly by design

## AREA: Auto-Scroll
CURRENT FILE: NowPlayingView.kt — SyncedLyricsDisplay
CURRENT COMPONENT: LaunchedEffect(currentLine, userIsScrolling) → animateScrollToItem
STATE SOURCE: currentLine, userIsScrolling
REFERENCE TARGET: Smooth scroll keeping active line in focal region
REUSE STRATEGY: LazyListState.animateScrollToItem with negative offset
FUNCTIONAL DEPENDENCY: LazyListState
RISK: LOW — scrollOffset -180 keeps active near center

## AREA: User Scroll Override
CURRENT FILE: NowPlayingView.kt — SyncedLyricsDisplay
CURRENT COMPONENT: isScrollInProgress detection; userIsScrolling flag; 5s auto-resume
STATE SOURCE: listState.isScrollInProgress
REFERENCE TARGET: Suspend auto-follow on user scroll; show return button
REUSE STRATEGY: LaunchedEffect monitoring isScrollInProgress
FUNCTIONAL DEPENDENCY: LazyListState.isScrollInProgress
RISK: LOW

## AREA: Return to Current Lyric
CURRENT FILE: NowPlayingView.kt
CURRENT COMPONENT: AnimatedVisibility button at bottom of lyrics panel
STATE SOURCE: userIsScrolling flag
REFERENCE TARGET: Pill-shaped compact button, Iris border, click resumes
FUNCTIONAL DEPENDENCY: userIsScrolling state
RISK: NONE

## AREA: Race Condition Protection
CURRENT FILE: PlayerViewModel.kt — loadLyrics()
CURRENT COMPONENT: lyricsJob?.cancel() + capturedId guard
STATE SOURCE: capturedId = song.id captured at start; checked before writing result
REFERENCE TARGET: Stale lyrics never overwrite current track
REUSE STRATEGY: Job cancellation + ID validation (latest-request-wins)
FUNCTIONAL DEPENDENCY: CoroutineScope, Job, isActive
RISK: NONE — both cancel and ID guard in place

## AREA: Bottom Player Synchronization
CURRENT FILE: OmniWindow.kt / OmniBottomPlayer.kt
CURRENT COMPONENT: Same PlayerViewModel instance throughout
STATE SOURCE: PlayerViewModel singleton passed from Main.kt
REFERENCE TARGET: Same playback state in both views
REUSE STRATEGY: Single PlayerViewModel; no duplicate engine
FUNCTIONAL DEPENDENCY: PlayerViewModel (same reference)
RISK: NONE
