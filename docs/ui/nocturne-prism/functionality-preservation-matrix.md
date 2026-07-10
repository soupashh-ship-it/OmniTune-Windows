# Nocturne Prism — Functionality Preservation Matrix

## Overview
The UI reconstruction must not create a second independent playback architecture. The existing functional player (`VlcjAudioEngine` + `PlayerViewModel`) must be preserved.

## Matrix

| FEATURE | CURRENT STATUS | RELATED FILES | STATE OWNER | REGRESSION RISK | PHASE TO VERIFY |
|---------|----------------|---------------|-------------|-----------------|-----------------|
| App Launch & Close | Working | `Main.kt` | Desktop OS | LOW | All Phases |
| Window Minimize/Maximize/Restore | Working | `Main.kt`, `OmniWindow.kt` | WindowState | LOW | Phase 2, 10 |
| Window Resize & Responsiveness | Working (adaptive UI) | `Main.kt`, UI Views | WindowState | MEDIUM | Phase 2, 11-15 |
| Navigation (Sidebar) | Working | `Sidebar.kt`, `PlayerViewModel.kt` | `PlayerViewModel.navScreen` | MEDIUM | Phase 2 |
| Back/Forward Navigation | Working | `PlayerViewModel.kt` | `PlayerViewModel` history | LOW | All Phases |
| Global Search | Working | `OmniWindow.kt` (SearchView), `PlayerViewModel.kt`, `YouTubeService.kt` | `PlayerViewModel` (query, results, loading, error) | HIGH | Phase 4 |
| Search Result Playback | Working | `OmniWindow.kt`, `PlayerViewModel.kt` | `PlayerViewModel` | HIGH | Phase 4 |
| Play/Pause | Working | `VlcjAudioEngine.kt`, `PlayerViewModel.kt`, UI Views | `VlcjAudioEngine.playbackState` | HIGH | Phase 7, 15 |
| Previous/Next Track | Working | `PlayerViewModel.kt`, UI Views | `PlayerViewModel.queueIndex` | HIGH | Phase 7, 8, 15 |
| Seek (Timeline) | Working | `VlcjAudioEngine.kt`, UI Views | `VlcjAudioEngine.position` | MEDIUM | Phase 7, 15 |
| Volume Control | Working | `VlcjAudioEngine.kt`, `SettingsRepository.kt` | `PlayerViewModel.volume`, `SettingsRepository` | MEDIUM | Phase 9, 15 |
| Shuffle Toggle | Working | `PlayerViewModel.kt`, `SettingsRepository.kt` | `PlayerViewModel.shuffleMode`, `SettingsRepository` | MEDIUM | Phase 8, 15 |
| Repeat Toggle | Working | `PlayerViewModel.kt`, `SettingsRepository.kt` | `PlayerViewModel.repeatMode`, `SettingsRepository` | MEDIUM | Phase 8, 15 |
| Queue Management (Add, Remove, Reorder, Clear) | Working | `QueueView.kt`, `PlayerViewModel.kt` | `PlayerViewModel.queue` | HIGH | Phase 8 |
| Playlist Display | Working (basic search) | `PlaylistsView.kt`, `PlayerViewModel.kt`, `YouTubeService.kt` | `PlayerViewModel` (playlistResults) | HIGH | Phase 5 |
| Like/Unlike (Library persistence) | Working | `PlayerViewModel.kt`, `SettingsRepository.kt` | `PlayerViewModel.likedSongs`, `SettingsRepository` | LOW | Phase 5, 9 |
| Downloads | Not Implemented | `SettingsView.kt` | None (UI says "not enabled") | NONE | Phase 9 |
| Settings Persistence | Working | `SettingsView.kt`, `SettingsRepository.kt` | `SettingsRepository` | LOW | Phase 9 |
| Lyrics (Sync, Display) | Working | `NowPlayingView.kt`, `PlayerViewModel.kt` (LrcLib) | `PlayerViewModel.lyricsText` | HIGH | Phase 7 |
| Artwork Loading | Working | `Coil` integration, UI Views | `Coil` ImageLoader | MEDIUM | All Phases |
| Media Keys (Hardware) | Working | `SmtcManager.kt` | `SmtcManager` / OS | LOW | Phase 15 |
| System Tray | Working | `Main.kt`, `SystemTrayManager.kt` | Compose Tray | LOW | Phase 15 |

## Verification Strategy
- **Core Playback:** Must be verified in Phase 2 (Application Shell / Player Bar rebuild), Phase 7 (Now Playing), and Phase 8 (Queue).
- **Navigation:** Must be verified in Phase 2 (App Shell) and Phase 4-6 (Detail Views).
- **Search:** Must be verified in Phase 4.
- **Queue/Sync:** Must be verified in Phase 8 to ensure UI syncs perfectly with `PlayerViewModel`.
- **State Persistence:** Must be verified in Phase 9 (Settings).