# OmniTune Windows 80% Product Completion Audit

Date: 2026-07-13

Continuation update: 2026-07-14

## 1. Current functional inventory
- Shell: Sidebar navigation, Back/Forward, global search, shortcut keys
- Search: Standard/numpad Enter, multi-category provider search (songs, artists, albums, playlists), stale request cancellation, recent search chips (deduped, JSON persisted, capped to 50), clear search history.
- Home: Play Now, Continue Listening, Quick Picks, Trending, Releases.
- Library: Local views, sort, grid/list toggle, recent shelf, saved queue playlists.
- Playlists: Provider-backed playback, queue save-as-playlist API, download tracks.
- Artist/Album Detail: Provider-backed metadata and playback.
- Now Playing: Like, play next, seek, transport, shuffle, repeat, synced/unsynced lyrics fallback.
- Queue & Session: Playback history (deduped, pause/resume safe, session grouped), recently played, recommendations, queue shuffle/repeat/clear, save as playlist.
- Settings: Volume, shuffle/repeat persistence, theme selection (4 live shell palettes), reduced motion policy, mini-player always-on-top, audio quality, accounts/notifications.
- Downloads: Real filesystem state, local download index, pause/resume/retry/cancel/delete, local-source selection, offline playback support.

## 2. Current screen inventory
- Home (Reference locked)
- Search & Discovery (Reference locked)
- Bottom Player (Reference locked, true center alignment)
- Library
- Playlist Detail
- Artist Detail
- Album Detail
- Now Playing + Lyrics
- Queue & Session
- Settings
- Downloads & Offline

## 3. Current dead/partial controls
- Smart Offline Mixes: Removed as unsupported.
- External artist metadata (Followers, socials, tours): Truthful unavailable states only.
- External album credits (Studios/producers): Truthful unavailable states only.
- Browse/Radio: Real provider-backed Browse drill-down and Radio seed/endpoints implemented; runtime captures generated in `docs/qa/premium-completion/`.
- Volume controls: `player.setVolume()` is wired but UI control was missing (may be added now).
- Download pause/resume/delete: Real manager operations wired, manual UI proof pending.

## 4. Current visual mismatches
- Now Playing: Major difference in badge, artwork, and transport layout. Lyrics panel needs reconstruction.
- Downloads: Canonical capture is vertically shifted upward relative to reference.
- Queue: Major panels are consistently ~20–33 px lower than target.
- Settings: Card grid is close but consistently 13–18 px lower, and right column is ~11 px right of target.
- Library: Lower sections sit too low; All Songs is about 29 px below target.
- Playlist: Right rail is too short and starts too low.
- Artist: Lower region and tab strip sit too low.
- Album: Info and related right panels sit slightly down/right.

## 5. Current playback risks
- Seek reliability on adaptive streams.
- VLC thread/resource handling on release was hardened: poller cancellation, stop-before-release, released-state guards, and dispatcher close.
- New-track volume override was fixed; VLC no longer resets playback volume to 100 on every `playing` event, and `PlayerViewModel` reapplies the persisted/current app volume when playback starts.
- Missing crossfade/gapless implementation.

## 6. Current provider risks
- Signature cipher deobfuscation (naive URL parsing).
- Token expiry (poToken/visitorData refresh).
- ANDROID_VR client reliance (may break, needs fallback).

## 7. Current persistence risks
- Large-data JSON storage (performance of history/playlists as size grows).
- Java Preferences bounds/limitations.

## 8. Current installation risks
- Unsigned installer.
- Clean machine installation validation.
- JNA/VLC discovery paths on various Windows setups.

## 9. Current performance risks
- VLC memory leak on rapid interactions.
- Artwork cache unbounded growth.
- Loading large JSON records.

## 10. Current accessibility risks
- Screen reader completeness.
- Keyboard focus trapping.

## Status Matrix

| Area | Implemented | Fully functional | Runtime proven | Visually polished | Release-ready |
|---|---|---|---|---|---|
| Home | PASS | PASS | PASS | PASS | PASS |
| Browse | PASS | PASS | PASS | PARTIAL | PARTIAL |
| Radio | PASS | PASS | PASS | PARTIAL | PARTIAL |
| Search | PASS | PASS | PASS | PASS | PASS |
| Library | PASS | PASS | PASS | PARTIAL | PARTIAL |
| Songs | PASS | PASS | PASS | PASS | PASS |
| Albums | PASS | PASS | PASS | PASS | PASS |
| Artists | PASS | PASS | PASS | PASS | PASS |
| Playlists | PASS | PASS | PASS | PARTIAL | PARTIAL |
| Playlist Detail | PASS | PASS | PASS | PARTIAL | PARTIAL |
| Artist Detail | PASS | PASS | PASS | PARTIAL | PARTIAL |
| Album Detail | PASS | PASS | PASS | PARTIAL | PARTIAL |
| Now Playing | PASS | PASS | PASS | FAIL | FAIL |
| Lyrics | PASS | PASS | PASS | FAIL | FAIL |
| Queue | PASS | PASS | PASS | PARTIAL | PARTIAL |
| Downloads | PASS | PARTIAL | PARTIAL | FAIL | FAIL |
| Settings | PASS | PASS | PASS | PARTIAL | PARTIAL |
| Mini Player | PASS | PASS | PASS | PASS | PASS |
| System Tray | PASS | PARTIAL | PARTIAL | PASS | PARTIAL |
| Search provider | PASS | PASS | PASS | PASS | PARTIAL |
| Playback | PASS | PARTIAL | PARTIAL | PASS | PARTIAL |
| Offline playback | PASS | PARTIAL | PARTIAL | PASS | PARTIAL |
| History | PASS | PASS | PASS | PASS | PASS |
| Persistence | PASS | PASS | PASS | PASS | PASS |
| Installer | PASS | PASS | PARTIAL | PASS | PARTIAL |

## 2026-07-14 continuation evidence

- Browse now loads real `YouTubeService.explore()` / chart data, supports genre drill-down through the provider `BrowseEndpoint`, and no longer routes genre browse IDs as albums.
- Radio now builds real seeds from the current song, queue, playback history, discovery results, charts, and provider radio endpoints. No static fake stations were added.
- Manual top-bar search was repaired by removing the search field from the full-window `WindowDraggableArea`; only non-interactive top-bar gaps remain draggable.
- Runtime search QA produced `docs/qa/search-runtime-qa.json` for `Blinding Lights`: 71 total results, 20 songs, 11 artists, 20 albums, 20 playlists, no provider error.
- Home/Search post-fix regression captures were generated:
  - `docs/qa/premium-completion/home-after-search-dragfix-1672x941.png`
  - `docs/qa/premium-completion/search-after-search-dragfix-1672x941.png`
- Settings/file-backed persistence bug fixed: JSON stores now write to Preferences only when no `PlatformContext` exists, avoiding Java Preferences size failures for large playlist/history datasets.
- Validation after changes:
  - `.\gradlew.bat :composeApp:compileKotlinDesktop :composeApp:assemble test :composeApp:desktopTest` — PASS
  - forced live Innertube search test — PASS
