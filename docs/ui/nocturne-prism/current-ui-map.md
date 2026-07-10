# Nocturne Prism — Current UI Component Map

## Application Window

COMPONENT: Main application window
FILE: composeApp/src/desktopMain/kotlin/com/omnitune/app/Main.kt
PURPOSE: Creates Compose Window, tray integration, window state persistence
STATE SOURCE: WindowState, SettingsRepository (windowWidth/windowHeight)
CURRENT REUSABILITY: HIGH — window setup is framework-standard
RISK: LOW — window creation is decoupled from UI content
TARGET STRATEGY: Preserve; may need undecorated mode for mini player

## Application Shell

COMPONENT: OmniWindow (main layout container)
FILE: composeApp/src/desktopMain/kotlin/com/omnitune/app/window/OmniWindow.kt
PURPOSE: Column layout with Row(Sidebar + AnimatedContent) + MacPlaybackBar
STATE SOURCE: PlayerViewModel (navScreen, currentSong, playbackState, position, volume, query, searchResults, searchLoading, searchError, playerError, streamUrl)
CURRENT REUSABILITY: LOW — contains hardcoded SearchView, MacSongRow, MacPlaybackBar
RISK: HIGH — this is the main layout orchestrator; all navigation and player state flows through here
TARGET STRATEGY: Restructure layout to match reference shell (sidebar + topbar + content + player); extract SearchView and playback bar to separate files; add missing screen routes (Home, Browse, Radio, Library, Artist, Album, Downloads)

## Left Sidebar

COMPONENT: Sidebar + NavItem
FILE: composeApp/src/desktopMain/kotlin/com/omnitune/app/window/Sidebar.kt
PURPOSE: Navigation column with icons for Search, Now Playing, Queue, Playlists, Settings
STATE SOURCE: NavScreen (activeScreen), SongItem (currentSong for Now Playing indicator)
CURRENT REUSABILITY: MEDIUM — NavItem pattern is reusable, but limited to 5 entries
RISK: MEDIUM — needs expansion to include Home, Browse, Radio, Library sub-items, playlist thumbnails
TARGET STRATEGY: Expand navigation items, add playlist thumbnails, add Library sub-navigation, style per reference

## Bottom Playback Bar

COMPONENT: MacPlaybackBar
FILE: composeApp/src/desktopMain/kotlin/com/omnitune/app/window/OmniWindow.kt (lines 320-479)
PURPOSE: Persistent bottom bar with album art, track info, transport controls, progress slider, volume
STATE SOURCE: currentSong, playbackState, position, volume
CURRENT REUSABILITY: MEDIUM — functional but needs visual overhaul
RISK: MEDIUM — seek/volume interactions are working; must preserve
TARGET STRATEGY: Rename to OmniBottomPlayer; restructure to 3-section layout (left info, center transport, right controls); restyle per reference

## Search View

COMPONENT: SearchView + ShimmerSongRow + MacSongRow
FILE: composeApp/src/desktopMain/kotlin/com/omnitune/app/window/OmniWindow.kt (lines 156-317)
PURPOSE: Search input + results list
STATE SOURCE: PlayerViewModel (search query, results, loading, error)
CURRENT REUSABILITY: LOW — embedded in OmniWindow.kt, uses hardcoded styles
RISK: MEDIUM — search functionality must be preserved
TARGET STRATEGY: Extract to separate SearchView.kt; redesign layout per reference (categories, top result, genre grid, right panel)

## Now Playing View

COMPONENT: NowPlayingView + LyricsPanel + RelatedPanel + TransportCircle + PanelTab
FILE: composeApp/src/desktopMain/kotlin/com/omnitune/app/window/NowPlayingView.kt
PURPOSE: Full-screen now playing with album art, lyrics, related tracks
STATE SOURCE: PlayerViewModel (currentSong, playbackState, position, volume, lyricsText, lyricsLoading, queue, queueIndex)
CURRENT REUSABILITY: MEDIUM — has LRC parser, lyrics sync, panel tabs
RISK: MEDIUM — lyrics sync logic is working, must preserve
TARGET STRATEGY: Restyle to match reference; keep LRC parser and lyrics sync; restructure layout

## Queue View

COMPONENT: QueueView + QueueRow
FILE: composeApp/src/desktopMain/kotlin/com/omnitune/app/window/QueueView.kt
PURPOSE: Queue list with reorder (move up/down), remove, play controls
STATE SOURCE: PlayerViewModel (queue, queueIndex, playbackState)
CURRENT REUSABILITY: MEDIUM — QueueRow pattern is reusable
RISK: LOW — queue state management is in PlayerViewModel
TARGET STRATEGY: Restyle per reference; add Session History, After Queue Ends, Queue Controls sections

## Playlists View

COMPONENT: PlaylistsView
FILE: composeApp/src/desktopMain/kotlin/com/omnitune/app/window/PlaylistsView.kt
PURPOSE: Search and display YouTube playlists
STATE SOURCE: PlayerViewModel (playlistResults, playlistLoading, playlistError)
CURRENT REUSABILITY: LOW — limited implementation
RISK: LOW
TARGET STRATEGY: Evolve into Library view; add tabs for Songs/Albums/Artists/Playlists/Downloads

## Settings View

COMPONENT: SettingsView + SettingsGroup + SettingsRow + SettingsSwitch + ShortcutRow
FILE: composeApp/src/desktopMain/kotlin/com/omnitune/app/window/SettingsView.kt
PURPOSE: Settings page with groups for Account, Audio Quality, Playback, Appearance, Downloads, Keyboard Shortcuts, About
STATE SOURCE: SettingsRepository (volume, reduceMotion, miniPlayerAlwaysOnTop)
CURRENT REUSABILITY: MEDIUM — SettingsGroup/Row/Switch patterns are reusable
RISK: LOW — settings are persisted via java.util.prefs
TARGET STRATEGY: Restructure to 3-column card grid per reference; add theme selection, accent color, audio quality segmented control, notification toggles

## Playing Indicator

COMPONENT: PlayingIndicator
FILE: composeApp/src/desktopMain/kotlin/com/omnitune/app/window/PlayingIndicator.kt
PURPOSE: Animated bars indicator for currently playing track
STATE SOURCE: isPlaying boolean
CURRENT REUSABILITY: HIGH — simple standalone composable
RISK: NONE
TARGET STRATEGY: Keep and restyle colors to Nocturne Prism palette

## System Tray

COMPONENT: AppWindowIcon, AppTrayIcon
FILE: composeApp/src/desktopMain/kotlin/com/omnitune/app/window/SystemTrayManager.kt
PURPOSE: Window and tray icons
STATE SOURCE: None (static resources)
CURRENT REUSABILITY: HIGH
RISK: NONE
TARGET STRATEGY: Keep as-is

## Theme System

COMPONENT: OmniTuneTheme + OmniTuneTypography + color/spacing/shape tokens + OmniGlassSurface + OmniGradients + OmniMotion
FILE: composeApp/src/desktopMain/kotlin/com/omnitune/app/window/OmniTuneTheme.kt
PURPOSE: Centralized design system: colors, typography, shapes, spacing, gradients, glass surfaces, motion
STATE SOURCE: None (static tokens)
CURRENT REUSABILITY: HIGH — already has Nocturne Prism palette partially implemented
RISK: LOW — modifying tokens affects all consumers, but that is the intent
TARGET STRATEGY: Refine and extend: add missing semantic tokens, adjust values to match reference measurements, add layout constants, add component-specific tokens
