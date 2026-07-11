# Nocturne Prism — Phase 5 Library & Playlist Implementation Map

AREA: Library State
CURRENT FILE: `Screens.kt` (LibraryView), `PlayerViewModel.kt`
CURRENT COMPONENT: `LibraryView`
STATE SOURCE: `PlayerViewModel`
REFERENCE TARGET: Full page with tabs, pinned collections, recent additions, and track table.
REUSE STRATEGY: Extract `LibraryView` into a dedicated file. Extend `PlayerViewModel` to expose library content (or use dummy local state for pins if API lacks it).
FUNCTIONAL DEPENDENCY: Liked songs, local history.
RISK: Medium.
PLANNED CHANGE: Build `LibraryView.kt`. Implement Nocturne Prism tabs. Map `likedSongs` to the tracklist. Use `OmniSongRow` for the list. Implement "Pinned Collections" using actual categories or user playlists.

AREA: Playlist Detail State
CURRENT FILE: `PlayerViewModel.kt`, `OmniWindow.kt`
CURRENT COMPONENT: N/A
STATE SOURCE: `YouTubeService.playlist()`
REFERENCE TARGET: Cinematic hero header, tracklist, right-panel related playlists.
REUSE STRATEGY: Add `NavScreen.PlaylistDetail`, `currentPlaylistId`, and `openPlaylist` to `PlayerViewModel`. Add route to `OmniWindow`.
FUNCTIONAL DEPENDENCY: YouTube API.
RISK: High.
PLANNED CHANGE: Build `PlaylistDetailView.kt`. Implement `OmniPlaylistHero` with blur overlay and actions. Render tracks using `OmniSongRow`. Render right panel using similar logic to "More like this".

AREA: Playlist Actions
CURRENT FILE: `PlaylistDetailView.kt`
CURRENT COMPONENT: N/A
STATE SOURCE: `PlayerViewModel`
REFERENCE TARGET: Play button starts playback, click opens detail.
REUSE STRATEGY: Separate `playPlaylist(id)` (existing) from `openPlaylist(id)` (new).
FUNCTIONAL DEPENDENCY: Player.
RISK: Low.
PLANNED CHANGE: Ensure UI correctly uses both methods to differentiate navigation vs playback.
