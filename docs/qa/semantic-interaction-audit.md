# OmniTune Windows semantic interaction audit

Date: 2026-07-14

Scope: visible controls across the desktop app. Statuses are intentionally conservative.

| Area | UI promise | Backend/state effect | Persistence expectation | Status |
|---|---|---|---|---|
| Top bar search | Search provider content | `PlayerViewModel.search()` provider-backed categories | Recent searches persisted | PROVEN |
| Top bar navigation | Back/forward/settings/profile routes | Navigation state changes | In-memory history | PROVEN |
| Sidebar navigation | Open major destinations | `NavScreen` changes | n/a | PROVEN |
| Home cards | Open/play provider content | Provider item open/play path | Playback/queue state | PROVEN |
| Search results | Play/add/like/open artist/album/playlist | Player, queue, liked IDs, navigation | Likes/search history persisted | PROVEN |
| Search More/Less | Expand/collapse genre/results sections | UI state only | n/a | PROVEN |
| Provider query breadth | Popular, Indian, long-title, album, playlist, and Unicode searches | `YouTubeService.search()` with representative filters | n/a | PROVEN |
| Playlist search stale handling | Latest playlist search wins | Older playlist search job is cancelled; stale responses are ignored; returned items deduped | n/a | PROVEN |
| Library tabs/sort/view | Switch visible library views | UI state and sorted provider/local data | n/a | PROVEN |
| Library row actions | Play/add/like/song actions | Playback, queue, liked IDs | Likes persisted | PROVEN |
| Playlist Detail | Play/shuffle/download/add/like/open | Playback, queue, download manager, liked IDs | Downloads/likes persisted where applicable | PROVEN |
| Artist Detail | Play artist, open related content, add/like songs | Playback, queue, liked IDs, navigation | Likes persisted | PROVEN |
| Artist external facts | Followers/social/tours | No reliable provider source | n/a | INFORMATIONAL |
| Album Detail | Play/download/add/like/open artist | Playback, download manager, queue, liked IDs | Downloads/likes persisted | PROVEN |
| Album credits/studio | Detailed credits | No reliable provider source | n/a | INFORMATIONAL |
| Now Playing | Seek/transport/like/play-next/lyrics | Playback engine, queue, liked IDs, lyrics provider | Likes persisted | PROVEN |
| Playback seek safety | Seek target stays valid | Negative targets clamp to 0; targets beyond known duration clamp to duration; unknown duration remains lower-bounded | n/a | PROVEN |
| Browse provider failure recovery | Failed Browse/genre provider loads expose retry instead of a dead error state | Retry re-runs the relevant provider load generation | n/a | PROVEN/PARTIAL |
| Radio start synchronization | Starting radio updates active track and queue | Radio seed loads deduped bounded songs, sets queue/index/current song, then starts playback | Queue/player state | PROVEN/PARTIAL |
| Radio continuation | Near-end radio queue continuation is deduped, bounded, stale-session-safe, and concurrent-request-safe | Provider continuation appends only valid new songs for the active session; bounded failures stop hammering upstream | Queue/player state | PROVEN/PARTIAL |
| Related retry | Current-track related content exposes loading, empty, failure, retry, dedupe, and current-track exclusion | Retry uses the current track and request token; stale older track results are ignored by token/current-song check | n/a | PROVEN/PARTIAL |
| Queue | Clear/shuffle/repeat/remove/play rows | Queue/player state | Shuffle/repeat persisted | PROVEN |
| Queue Save as Playlist | Persist current queue as local playlist | `SettingsRepository.saveQueueAsPlaylist()` | JSON/Preferences-backed local playlist | PROVEN |
| Queue Save as Playlist failure | Do not report success when persistence fails | File write failure propagates through `Result`; dialog stays open and shows `Couldn't save playlist.` | Prior good file preserved by atomic write | PROVEN |
| Downloads | Pause/resume/retry/delete/play completed/download quality | `OmniDownloadManager` and settings | JSON task index/files/settings | PROVEN |
| Downloads Smart Offline Mixes | Smart generated mixes | Not implemented truthfully | n/a | REMOVED AS UNSUPPORTED |
| Settings volume | Control player volume | Player/audio engine state | Persisted | PROVEN |
| Settings theme | Switch theme palette | Live theme state | Persisted | PROVEN |
| Settings reduced motion | Shorten/disable motion where wired | `OmniMotionPolicy` | Persisted | PROVEN/PARTIAL |
| Settings mini-player always on top | Native window always-on-top | AWT window property | Persisted | PROVEN |
| Bottom player | Transport/progress/volume/queue | Player engine, seek, settings, navigation | Volume/shuffle/repeat persisted | PROVEN |
| Keyboard text input safety | Text fields accept spaces without triggering global playback shortcut | `OmniKeyboardShortcutRouter` ignores Space/N/P while editable text owns focus; all inventoried desktop text fields report focus ownership | n/a | PROVEN |
| Keyboard repeat safety | Held one-shot global shortcuts do not repeatedly toggle/skip | Pressed-key guard marks repeated key-down events; router ignores repeated one-shot commands | n/a | PROVEN |
| Keyboard focus order model | Intended Tab/Shift+Tab progression for complex screens | `OmniFocusTraversalModel` defines forward/reverse scope order and is tested | n/a | PROVEN/PARTIAL |
| Compose runtime focus proof | Real Compose focus assertion and key injection | `OmniComposeFocusRuntimeTest` renders test surfaces, asserts focus, injects Tab/Shift+Tab, verifies real `OmniSearchField`, production TopBar search, production Queue Save dialog, Sidebar actions/submenu behavior, production bottom-player `PlayerControlBand`, and production mini-player transport button components | n/a | PROVEN/PARTIAL |
| Modal shortcut containment | Global shortcuts do not steal focus while modal is open | `modalOpen` is passed to `OmniKeyboardShortcutRouter`; modal-open state suppresses shell shortcuts | n/a | PROVEN |
| Queue Save as Playlist keyboard behavior | Enter confirms valid name; Escape cancels | Production `QueueSaveAsPlaylistDialog` handles name focus, Space/N/P text, blank validation, Enter, NumPadEnter, Escape, and opener restoration in a production-style harness | Saved playlist persists through existing queue-playlist persistence | PROVEN/PARTIAL |
| Mini Player | Compact transport/now playing | Player engine and native mini window | Always-on-top persisted | PROVEN/PARTIAL |
| System tray | Show, Mini Player, Quit | Window visibility, mini player, engine shutdown | n/a | PROVEN |
| Windows media session | Publish current media and accept OS transport commands | Existing JMTC dependency maps title/artist/album/timeline/status and Play/Pause/Next/Previous/Seek callbacks into player actions | Windows media transport controls | PROVEN/PARTIAL |
| Code signing | Signed installer | No certificate available | n/a | EXTERNAL BLOCKER |

## Counts

| Status | Count |
|---|---:|
| PROVEN | 29 |
| PROVEN/PARTIAL | 10 |
| INFORMATIONAL | 2 |
| REMOVED AS UNSUPPORTED | 1 |
| EXTERNAL BLOCKER | 1 |
| FAIL | 0 |

## Notes

- This audit goes beyond checking for `onClick {}`. It maps visible promises to backend effects and persistence expectations.
- External factual metadata remains informational unless a reliable provider exposes it.
- Reduced-motion, mini-player behavior, and keyboard text-input safety are implemented but still need deeper full-surface runtime traversal/accessibility validation.
- Deterministic `OmniKeyboardShortcutRouterTest` covers text-field ownership, modal suppression, key-up suppression, repeated key-down suppression, stuck-key clearing, and global shortcut command mapping.
- Deterministic `OmniFocusTraversalModelTest` covers intended forward/reverse focus order for complex screens. Compose runtime focus traversal is proven by `OmniComposeFocusRuntimeTest` on minimal and representative production surfaces, but full PlayerViewModel-backed production-screen traversal remains incomplete.
- Queue Save as Playlist now has explicit runtime-tested keyboard policy for Enter, NumPadEnter, Escape, blank validation, and opener focus restoration through the extracted production dialog composable.
- Production Sidebar submenu behavior, bottom-player transport callbacks, and mini-player transport button callbacks now have representative Compose runtime proof. The full Library/Playlist Detail/Now Playing/Downloads/Mini Player/Bottom Player routes remain runtime-unproven because mounting them still requires safe seams around `PlayerViewModel` side effects.
- Provider QA with explicit opt-in covered six representative real queries with non-empty results, recorded in `docs/qa/provider-reliability-runtime-qa.json`.
- This pass fixed playlist-search stale result handling, radio current-song synchronization, bounded/deduped radio queue seeding, radio continuation guards, Related retry/failure state, Browse/Radio retry affordances, Windows media-session mapping/integration, and seek clamping to known duration.
- Windows SendKeys and WScript SendKeys attempts did not reliably inject text into the Compose Desktop window in this environment, so those captures are not counted as complete runtime traversal proof.

## Verdict

Semantic interaction audit: **STRONG PARTIAL PASS**.

No known dead app-owned control remains in the audited matrix, but full runtime traversal through every visible control is still broader than this local pass.
