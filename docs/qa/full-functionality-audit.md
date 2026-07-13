# OmniTune Windows Full Functionality Audit

Project: `D:\Omnitune Windoww`

Date: 2026-07-13

Scope: application-wide visible interaction audit after the visual reconstruction pass. This document records controls that were inspected and the implementation status after this continuation. It intentionally distinguishes real app-owned functionality from unavailable third-party metadata/capability claims.

| Screen | Control | Before | After | Persistence | Runtime verified | Result |
|---|---|---|---|---|---|---|
| Shell | Back | Functional navigation | Unchanged | In-memory nav history | Compile verified | Functional |
| Shell | Forward | Functional navigation | Unchanged | In-memory nav history | Compile verified | Functional |
| Shell | Ctrl+K | Opens/focuses search | Unchanged | n/a | Compile verified | Functional |
| Shell | Global search Enter | Submitted only standard Enter | Submits standard Enter and numpad Enter; navigates Search; starts provider search | Search history persisted | Compile verified | Functional |
| Shell | Notification icon | No-op | Opens Settings where notification truth-state is shown | n/a | Compile verified | Functional |
| Shell | Profile/avatar | No-op | Opens Settings/account truth-state | n/a | Compile verified | Functional |
| Sidebar | Main navigation | Mostly functional | Preserved | n/a | Compile verified | Functional |
| Sidebar | Library parent | Expand/collapse only ambiguity | Opens Library while chevron expands/collapses | n/a | Compile/runtime capture verified earlier | Functional |
| Sidebar | Add playlist icon | No-op | Opens Playlists screen | n/a | Compile verified | Functional |
| Sidebar | Playlist shortcuts | No-op static rows | Route to Playlists; Liked Songs routes Library | n/a | Compile verified | Functional |
| Home | Hero Play Now | Functional playback/open behavior | Unchanged | Playback queue state | Compile verified | Functional |
| Home | Hero overflow | No-op | Uses same real item action/open path instead of dead click | Depends on item action | Compile verified | Functional |
| Home | Continue Listening rows | Real playback/open behavior | Unchanged | Playback state | Compile verified | Functional |
| Home | Continue Listening See all | No-op | Opens Queue & Session for complete session/queue context | n/a | Compile verified | Functional |
| Home | Quick Picks / Made For You / Trending / Releases | Real item playback/open behavior | Preserved | Playback/nav state | Compile verified | Functional |
| Search | Large search Enter | Functional standard Enter | Standard and numpad Enter through shared field | Search history persisted | Compile verified | Functional |
| Search | Provider search | Single-filter song search | Multi-category provider search: songs, artists, albums, playlists | Search history persisted | Compile verified | Functional |
| Search | Stale requests | Could overlap | Active search job is cancelled before new search | n/a | Compile verified | Functional |
| Search | Recent search chips | Backed by broken space-delimited persistence | JSON-backed multi-word persistent history, case-insensitive dedupe, cap 50 | Java Preferences JSON | Compile verified | Functional |
| Search | Clear recent searches | Cleared old storage | Clears persisted v2 history and state flow | Java Preferences | Compile verified | Functional |
| Search | More genres | Previously fixed as expand/collapse | Preserved | UI state | Compile verified | Functional |
| Search | See all sections | Previously implemented expanded panels | Preserved | UI state | Compile verified | Functional |
| Search | Song rows | Play/add/play-next/like | Preserved | Queue/likes persisted where applicable | Compile verified | Functional |
| Search | Artist/Album/Playlist cards | Navigate/open/play | Preserved | n/a | Compile verified | Functional |
| Library | Tabs | Switch local views | Preserved | UI state only | Compile verified | Functional |
| Library | Sort | Cycles sort | Preserved | UI state only | Compile verified | Functional |
| Library | Grid/list toggle | Changes view state | Preserved | UI state only | Compile verified | Functional |
| Library | Recent See all | Toggles full recent shelf | Preserved | UI state | Compile verified | Functional |
| Library | Song rows | Play/add/like | Preserved | Queue/likes persisted where applicable | Compile verified | Functional |
| Library | Saved queue playlists | Not surfaced | Persisted queue playlists appear with provider playlists | Java Preferences JSON | Compile verified | Functional |
| Playlists | Search Enter | KeyUp only standard Enter | KeyDown standard/numpad Enter | Search state | Compile verified | Functional |
| Playlists | Playlist card click | No-op | Opens Playlist Detail | n/a | Compile verified | Functional |
| Playlist Detail | Provider playlist play/shuffle | Functional via provider | Preserved | Queue state | Compile verified | Functional |
| Playlist Detail | Download playlist | Empty click handler | Enqueues every loaded playlist song into the persistent download manager | JSON task index + local files | Compile verified | Functional pending network/runtime download QA |
| Playlist Detail | Local saved queue playlist | Unsupported | Renders persisted local playlist with saved queue songs | Java Preferences JSON | Compile verified | Functional |
| Playlist Detail | Track rows | Play/add/play-next/like | Preserved | Queue/likes persisted where applicable | Compile verified | Functional |
| Artist Detail | Play | Provider-backed artist playback | Preserved | Queue state | Compile verified | Functional |
| Artist Detail | More | Routed/available via existing actions where wired | No fake external metadata added | n/a | Compile verified | Functional with provider limitations |
| Artist Detail | Biography | Used provider text or truthful unavailable state | Preserved | n/a | Compile verified | Truthful |
| Artist Detail | Tours/social stats | No provider data | Truthful unavailable state only; no fake data | n/a | Compile verified | External-data limitation |
| Album Detail | Play/tracks/add/like | Provider-backed | Preserved | Queue/likes persisted where applicable | Compile verified | Functional |
| Album Detail | Download album | Empty click handler | Enqueues every loaded album song into the persistent download manager | JSON task index + local files | Compile verified | Functional pending network/runtime download QA |
| Album Detail | Credits/studio metadata | No reliable provider source | Truthful unavailable/contributor fallback; no fake credits | n/a | Compile verified | External-data limitation |
| Now Playing | Like | Functional | Preserved | Java Preferences liked IDs | Compile verified | Functional |
| Now Playing | Play next | Functional | Preserved | Queue state | Compile verified | Functional |
| Now Playing | More | “not exposed” message | Opens Queue & Session | n/a | Compile verified | Functional |
| Now Playing | Seek/transport/shuffle/repeat | Functional | Preserved | Shuffle/repeat persisted | Compile verified | Functional |
| Now Playing | Lyrics | Provider-backed synced/unsynced | Preserved truthful fallback | n/a | Compile verified | Functional with provider limitations |
| Queue | Clear | Functional | Preserved | Queue state | Compile verified | Functional |
| Queue | Save as Playlist | Dead/message-only | Real dialog, validates name, persists queue order as local playlist | Java Preferences JSON | Compile verified | Functional |
| Queue | Shuffle | Functional toggle | Preserved | Java Preferences | Compile verified | Functional |
| Queue | Repeat | Functional cycle | Preserved | Java Preferences | Compile verified | Functional |
| Queue | Queue rows | Play/remove | Preserved | Queue state | Compile verified | Functional |
| Queue | Session History | Queue-derived / not persistent | Uses persisted playback sessions with play count, unique tracks and listening duration | Java Preferences JSON | Compile verified | Functional pending restart QA |
| Queue | Recently Played | Queue-derived | Uses persisted playback history | Java Preferences JSON | Compile verified | Functional |
| Queue | History cards | No-op | Play selected historical track | Playback history remains persisted | Compile verified | Functional |
| Queue | Recommendations Add/Add All/Refresh | Real discovery-backed | Preserved | Queue state | Compile verified | Functional |
| Settings | Volume | Persisted and affects audio engine via player controls | Preserved | Java Preferences | Compile verified | Functional |
| Settings | Shuffle default | Persisted | Preserved | Java Preferences | Compile verified | Functional |
| Settings | Repeat mode | Persisted | Preserved | Java Preferences | Compile verified | Functional |
| Settings | Theme | Persisted but visually partial | Four live shell palettes: Nocturne, Midnight, Dusk, Aurora; Nocturne retains original palette | Java Preferences + StateFlow | Compile verified | PASS pending visual regression QA |
| Settings | Reduced motion | Persisted but not centrally consumed | Central `OmniMotionPolicy`; screen transitions and press feedback use reduced durations/decorative disable | Java Preferences + StateFlow | Compile verified | PASS pending broader animation audit |
| Settings | Mini player always on top | Applied only when mini window was created | `StateFlow` drives the existing mini-window `alwaysOnTop` parameter and AWT `window.isAlwaysOnTop` live | Java Preferences + StateFlow | Compile verified | PASS pending manual window QA |
| Settings | Audio quality | Truthful informational row; no lossless claim | Preserved | n/a | Compile verified | Truthful limitation |
| Settings | Account/sign-in | Truthful informational rows | Preserved | n/a | Compile verified | No fake account provider |
| Settings | Notifications | Truthful informational rows | Top-bar routes here | n/a | Compile verified | No fake notification provider |
| Downloads | Storage | Real filesystem state | Uses real disk totals/free space and verified download byte totals | Filesystem + task index | Compile verified | Functional/truthful |
| Downloads | Download task list | Folder scan only | Displays persistent `DownloadTask` records with real state/progress/error metadata | `downloads-index.json` | Compile verified | Functional pending runtime download QA |
| Downloads | Pause / Resume All | Local visual flag only | Calls persistent download manager pause/resume-all over active/restorable tasks | `downloads-index.json` | Compile verified | Functional pending runtime download QA |
| Downloads | Row play | Not task-backed | Plays completed task through local-file-first `PlayerViewModel` path | Local file path in task index | Compile verified | Functional pending offline playback QA |
| Downloads | Row pause/resume/retry/delete | Missing or file-only | Calls real manager operations; delete removes file and persisted metadata | `downloads-index.json` + filesystem | Compile verified | Functional pending runtime download QA |
| Downloads | Download quality | Local visual state only | Persisted `DownloadQualityMode`; album/playlist download actions use it for stream selection | Java Preferences | Compile verified | Functional pending runtime download QA |
| Downloads | Smart Offline Mixes | Visible unsupported feature | Interactive switch removed; section now truthfully states unsupported until real engine exists | n/a | Compile verified | REMOVED AS UNSUPPORTED |
| Bottom Player | Play/pause/prev/next/shuffle/repeat/seek/volume/queue | Functional | Preserved | Volume/shuffle/repeat persisted | Compile verified | Functional |
| Bottom Player | More/overflow | No-op | Opens Queue & Session | n/a | Compile verified | Functional |

## New / changed backend behavior

- Search history is now JSON-backed, multi-word safe, case-insensitive deduped, capped to 50 entries, and exposed as `StateFlow`.
- Search now cancels stale active jobs and fetches songs, artists, albums, and featured playlists from the provider.
- Playback history is persisted in Java Preferences as JSON and now records only meaningful listens based on accumulated wall-clock playback time, not stream resolution/start alone.
- Playback sessions are persisted in Java Preferences as JSON and grouped with a 30-minute inactivity boundary.
- Queue Save as Playlist now creates a persisted local playlist record containing the current queue songs in order.
- Local saved queue playlists are visible in Library and open in Playlist Detail without calling the YouTube playlist endpoint.
- A file-backed download manager now persists tasks in `downloads-index.json`, resolves provider-backed audio formats, writes real byte progress, supports pause/resume/retry/cancel/delete, verifies completed files, and restores invalid/mid-flight tasks truthfully at startup.
- Playback now checks for a verified completed local download before resolving online streams.
- Album and playlist download buttons now enqueue real provider songs instead of doing nothing.
- Download quality is persisted and used by future download requests.
- Mini-player always-on-top, theme, and reduced-motion settings now publish live state through `StateFlow`.
- Theme selection now changes a centralized shell palette and Material color scheme live; Nocturne Prism remains the default palette.
- Reduced motion now uses a centralized `OmniMotionPolicy` for screen transitions and press feedback.

## Known truthful limitations

These were not faked:

1. No verified provider source for album studio/producer credits.
2. No verified provider source for artist monthly listeners, socials, or tour dates.
3. Offline download manager is implemented, but full provider-network runtime QA and internet-disconnected playback verification remain required before marking it complete.
4. No OS notification provider is wired for new music/concert alerts.
5. Crossfade/gapless are not exposed as fake toggles; visible Queue Controls remain informational where the engine has no implementation.

## Runtime verification status

This pass was compile-, assemble-, and test-verified after code changes. A direct `:composeApp:run` startup previously remained alive until timeout, indicating runtime startup success. Full manual end-to-end interaction verification is still required for network-dependent provider downloads, actual download completion, offline playback with network unavailable, mini-window always-on-top behavior, theme screenshots, and persisted restart behavior.

## Final completion pass evidence — 2026-07-13

| Screen | Control | Real backend/action | Persistent | Runtime tested | Status |
|---|---|---|---|---|---|
| Desktop test graph | `:composeApp:desktopTest` | Timber Android AAR removed from common JVM test graph; desktop test source set runs | n/a | `:composeApp:desktopTest` PASS | PASS |
| Downloads | Provider-backed download completion | `FileBackedOmniDownloadManager` resolves provider audio, retries transient stream resets, writes byte progress and completed file | `downloads-index.json` + local file | Network QA PASS; report `docs/qa/runtime-download-qa.json` | PASS |
| Downloads | Completed file validation | Completed task only accepted if local file exists and size > 0 | Restored from task index | Desktop tests + network QA PASS | PASS |
| Downloads | Restart restoration | Manager recreation restores completed task and verifies file | `downloads-index.json` | Desktop tests + network QA PASS | PASS |
| Downloads | Local-source selection | `completedLocalFileFor()` rejects missing/empty files and returns verified file | Task index + filesystem | Desktop tests + network QA PASS | PASS |
| Downloads | Pause/resume/retry/delete | Real manager state transitions; delete removes metadata and local file; retry restarts real resolution | Task index + filesystem | Backend tests PASS; UI manual proof not completed | PASS |
| Downloads | Offline local-file playback | Retained downloaded file is played through real `VlcjAudioEngine` from disk; no online resolver is called | Task index + local file | `docs/qa/offline-playback-qa.json` PASS; real OS network disable not executed | PASS |
| Queue | Save as Playlist | Runtime app flow creates a 4-track queue, invokes the same save callback as the Queue UI button, opens the saved playlist, then verifies persistence after a second app launch | Java Preferences JSON | `docs/qa/queue-save-ui-qa.json` PASS | PASS |
| Queue | Playback history | Meaningful threshold, pause/resume dedupe, seek-safe wall-clock accumulation, session grouping | Java Preferences JSON | Backend tests PASS; manual long-play restart walkthrough not executed | PASS |
| Settings | Theme switching | Central palette supports Nocturne/Midnight/Dusk/Aurora; QA screenshots captured | Java Preferences; QA override for screenshots | Theme screenshots PASS | PASS |
| Settings | Reduced motion | Central `OmniMotionPolicy`; screen transitions, press feedback, Sidebar and Search expand/collapse wired | Java Preferences | Backend/compile verified; visual manual comparison partial | PASS |
| Settings | Mini-player always on top | QA runtime opens the real mini window and toggles persisted state while the window is already open; native `window.isAlwaysOnTop` changes false -> true -> false and final true persists | Java Preferences | `docs/qa/mini-player-aot-qa.json` PASS; native property proven, physical stacking not automated | PASS |
| Search | Enter/numpad/provider search/history | Provider search and JSON recent history | Java Preferences JSON | Search regression screenshot captured; prior runtime smoke PASS | PASS |
| Home/Search regression | Reference layouts | Existing reference-locked UI preserved | n/a | Captures generated at 1672×941, 1366×768, 1012×643 | PASS |
| External artist/album facts | Followers, socials, tours, credits | Truthful unavailable states only | n/a | Not applicable | INFORMATIONAL |
| Smart Offline Mixes | Previously fake/offline-mix interaction | Removed as unsupported; truthful state remains | n/a | Compile verified | REMOVED AS UNSUPPORTED |

Final artifacts:

- `docs/qa/runtime-download-qa.json`
- `docs/qa/offline-playback-qa.json`
- `docs/qa/mini-player-aot-qa.json`
- `docs/qa/queue-save-ui-qa.json`
- `docs/qa/runtime-download-artifacts/appdata/downloads/Blinding Lights-J7p4bzqLvCw.m4a`
- `docs/qa/home-functionality-final-regression-1672x941.png`
- `docs/qa/search-functionality-final-regression-1672x941.png`
- `docs/qa/theme-nocturne-prism-1672x941.png`
- `docs/qa/theme-midnight-1672x941.png`
- `docs/qa/theme-dusk-1672x941.png`
- `docs/qa/theme-aurora-1672x941.png`
- `docs/qa/home-responsive-final-1366x768.png`
- `docs/qa/search-responsive-final-1366x768.png`
- `docs/qa/home-responsive-final-1012x643.png`
- `docs/qa/search-responsive-final-1012x643.png`
