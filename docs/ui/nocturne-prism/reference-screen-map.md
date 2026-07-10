# Nocturne Prism — Reference Screen Map

| REFERENCE SCREEN | CURRENT EXISTING SCREEN | CURRENT FILE(S) | CURRENT ROUTE | REFERENCE IMAGE PROVIDED | IMPLEMENTATION COMPLEXITY | FUNCTIONAL DEPENDENCIES | EXPECTED LATER PHASE |
|------------------|-------------------------|-----------------|---------------|--------------------------|---------------------------|-------------------------|----------------------|
| Home / Discover | None | N/A | `NavScreen.Home` | Yes (Image 9) | High | `PlayerViewModel` data flows (needs generic feed/mix data) | Phase 3 |
| Search & Discovery | Search View | `OmniWindow.kt` (SearchView) | `NavScreen.Search` | Yes (Image 10) | High | `PlayerViewModel.search`, `YouTubeService` | Phase 4 |
| Library | Playlists View (partial) | `PlaylistsView.kt` | `NavScreen.Library` | Yes (Image 1) | High | Local persistence, `PlayerViewModel` | Phase 5 |
| Playlist Detail | None | N/A | `NavScreen.Playlists` | Yes (Image 2) | Medium | Route parameters (Playlist ID), YouTube API | Phase 5 |
| Artist Profile | None | N/A | `NavScreen.Artist` | No | Medium | Route parameters (Artist ID), YouTube API | Phase 6 |
| Album Detail | None | N/A | `NavScreen.Album` | Yes (Image 4 - Note: Identified as Album Detail, though user prompt called it Home) | Medium | Route parameters (Album ID), YouTube API | Phase 6 |
| Full Now Playing + Lyrics | Now Playing View | `NowPlayingView.kt` | `NavScreen.NowPlaying` | Yes (Image 5) | High | `PlayerViewModel` (lyrics, playbackState), LrcLib | Phase 7 |
| Queue & Session | Queue View | `QueueView.kt` | `NavScreen.Queue` | Yes (Image 6) | High | `PlayerViewModel` (queue management, reorder) | Phase 8 |
| Settings & Personalization | Settings View | `SettingsView.kt` | `NavScreen.Settings` | Yes (Image 7) | Medium | `SettingsRepository` | Phase 9 |
| Downloads & Offline | None | N/A | `NavScreen.Downloads` | Yes (Images 3, 8) | High | File I/O, Download Manager (needs building or mocking) | Phase 9 |
| Compact Mini Player | None | N/A | Toggle from App Shell | No | High | Multiple Windows or undecorated Window manipulation | Phase 10 |

## Analysis
- **Missing Screens:** Home, Playlist Detail, Artist Profile, Album Detail, Downloads, Mini Player. These require building entirely new UI structures.
- **Existing but Needs Major Overhaul:** Search, Library (currently just a basic playlist search), Now Playing, Queue, Settings. These require ripping out the old layouts and dropping in the new Nocturne Prism layouts while preserving the state connections to `PlayerViewModel` and `SettingsRepository`.
- **Reference Images:** We have visual references for almost all critical screens, except Artist Profile and the Mini Player. We will need to extrapolate their design based on the visual language established in the other reference images.