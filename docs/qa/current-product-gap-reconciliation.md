# OmniTune current product gap reconciliation

Date: 2026-07-14

Scope: local product-completion reconciliation after the desktop runtime/focus passes. This document records current product capability status from source inspection plus targeted runtime tests. It does not claim clean-VM validation, physical offline validation, soak testing, code signing, or external screen-reader validation.

## Summary counts

| Status | Count |
|---|---:|
| PROVEN | 29 |
| PARTIAL | 14 |
| INFORMATIONAL | 5 |
| REMOVED AS UNSUPPORTED | 2 |
| FAIL | 0 |
| NOT IMPLEMENTED | 2 |

## Current capability matrix

| Area | Capability | Status | Notes |
|---|---|---|---|
| Home | Provider-backed home content and actions | PROVEN | Existing baseline protected; no new regression found in this pass. |
| Browse | Explore, charts, mood/genre drill-in | PARTIAL | Real provider-backed shelves; provider failure now exposes a local Retry action instead of a terminal dead state. Full live provider failure matrix not exhaustively runtime-tested. |
| Radio | Start radio from track/provider endpoint | PARTIAL | Current-song synchronization, bounded/deduped radio queue seed, session identity, continuation guard, stale-session protection, and bounded continuation failure policy are implemented. |
| Search | Enter/NumPadEnter/multi-category search | PROVEN | Existing search routing, stale cancellation, and history preserved. |
| Search | Representative provider query depth | PROVEN | `docs/qa/provider-reliability-runtime-qa.json`: 6/6 representative queries returned non-empty results. |
| Playlists search | Stale response handling | PROVEN | Added cancellation/token guard and result dedupe. |
| Library | Tabs, pins, sort, row actions | PARTIAL | Product wiring exists; full route runtime proof remains blocked by concrete `PlayerViewModel`. |
| Playlist Detail | Provider/local playlist open/play/save | PARTIAL | Queue-saved playlists supported; full route runtime proof remains partial. |
| Artist Detail | Real artist page actions | PARTIAL | Provider data only; external facts remain unavailable. |
| Album Detail | Album play/download/open artist | PARTIAL | Provider data only; no fake production/studio credits. |
| Now Playing | Playback controls/state sync | PARTIAL | Core controls wired; full route runtime proof still partial. |
| Now Playing | Visual 3 px transport residual | PARTIAL | Retained to avoid broader regression. |
| Lyrics | Synced/unsynced/no-lyrics states | PROVEN | Stale lyric updates guarded by current-song ID. |
| Related | Provider related content | PARTIAL | Related fetch is real, stale track-safe, deduped, current-track-excluding, capped, and exposes loading/failure/retry UI. |
| Queue | Up Next/remove/clear/shuffle/repeat/save | PROVEN | Queue Save as Playlist runtime modal proof exists. |
| Queue | Crossfade/gapless | INFORMATIONAL | Shown as not exposed; no fake engine feature. |
| Queue | Mix Mood | INFORMATIONAL | Queue-based informational control; no fake mood engine. |
| Playback history | Meaningful threshold/session grouping | PROVEN | Existing tests and persistence preserved. |
| Playback | Consecutive error reset | PROVEN | Reset on `PLAYING` state in lifecycle collector. |
| Playback | Bounded recovery | PARTIAL | Resolution/audio errors skip within bounded consecutive-error handling; provider failures cannot be eliminated. |
| Playback | Seek safety | PROVEN | Added clamp for negative and beyond-known-duration seek targets. |
| Playback | Radio current song | PROVEN | Fixed radio start to update `_currentSong` before playback. |
| Downloads | Start/progress/pause/resume/retry/delete | PROVEN | Existing manager and failure tests preserved. |
| Downloads | Media-write failure handling | PROVEN | Permission, no-space, resume, finalization paths covered. |
| Downloads | `totalBytes` semantics | PARTIAL | Final file requires non-empty verified file; strict byte equality is not enforced where provider size is unreliable. |
| Settings | Theme/reduced motion/volume/download settings | PROVEN | Real persistence/effect for exposed controls. |
| Settings | Account/notifications | INFORMATIONAL | Truthful unavailable/local states; no fake provider. |
| Mini Player | Transport component callbacks | PROVEN | Production component callback tests pass; full route still `PlayerViewModel`-coupled. |
| Bottom Player | Control band callbacks | PROVEN | Production `PlayerControlBand` callback tests pass; full route still `PlayerViewModel`-coupled. |
| Sidebar | Main navigation/submenu | PROVEN | Runtime tests cover activation and Library submenu collapsed/expanded behavior. |
| Keyboard shortcuts | Text ownership/repeat/modal suppression | PROVEN | Router, pressed-key tracker, and Compose runtime tests pass. |
| System tray | Show/mini/quit | PARTIAL | Existing runtime smoke evidence; not expanded in this pass. |
| SMTC/media session metadata | OS media-session integration | PARTIAL | Existing JMTC dependency is now wired through `SmtcManager` for metadata/status/timeline and Play/Pause/Next/Previous/Seek callbacks. Physical Windows shell UI validation is not claimed. |
| External screen reader | Narrator/NVDA runtime | NOT IMPLEMENTED | External validation unavailable/not performed. |
| Windows signing | Authenticode signing | NOT IMPLEMENTED | Blocked by no legitimate certificate. |
| Smart Offline Mixes | Automated smart mixes | REMOVED AS UNSUPPORTED | UI truthfully states unsupported. |
| Artist socials/tours | External factual metadata | INFORMATIONAL | No reliable provider source; not fabricated. |

## Prioritized remaining gaps

### P0

No current known P0 local app-owned failures after this pass.

### P1

1. Full route-level runtime proof for PlayerViewModel-backed Library/Playlist Detail/Now Playing/Downloads/Mini/Bottom Player remains partial.
2. Provider failure UX can still be runtime-expanded for Browse/Radio/Related under more live upstream failure cases.
3. Physical Windows SMTC/media-session flyout validation remains unproven even though the JMTC integration is implemented.

### P2

1. Browse/Radio depth: Browse continuation/See-all paging remains limited to provider semantics already exposed by the app; Radio continuation is implemented but still needs live provider stress coverage.
2. Library lifecycle depth: richer local playlist edit operations remain limited to currently exposed product behavior.
3. Related tab: retry and empty-state recovery can be clearer.

### P3

1. Canonical visual stable-error residuals remain, especially Now Playing surface/effect differences and a 3 px transport-height residual.
2. Four-theme focus screenshot coverage is still narrow.

## Pass result

Actual product completion advanced through narrow playback/provider fixes:

- radio now updates the current song when a radio session starts;
- radio queue seeds are deduped and bounded to 50 items;
- playlist search now uses stale-response cancellation and dedupe;
- seek targets clamp to known media duration where available;
- representative provider query QA now covers popular, Indian, long-title, album, playlist, and Unicode searches.
- Browse and Radio now expose retry affordances for provider-load failures.
- Radio continuation is session-safe, deduped, bounded, and guarded against concurrent requests/stale old sessions.
- Related content now has explicit loading, failure, retry, dedupe, current-track exclusion, and result-cap behavior.
- `SmtcManager` now uses the existing JMTC dependency for media metadata/status/timeline and transport callbacks, without claiming physical Windows shell validation.

No broad architecture rewrite or release action was performed.
