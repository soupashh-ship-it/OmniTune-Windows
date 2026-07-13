# OmniTune Windows 80% product-completion audit

Project: `D:\Omnitune Windoww`

Audit date: 2026-07-13

Requested starting release: `v0.1.0-rc.2`

Actual current release baseline: `v0.1.1-rc.1`

Current Gradle package version: `0.1.1`

Public repository: `https://github.com/soupashh-ship-it/OmniTune-Windows`

## Scope and source-of-truth policy

This is the Phase 0 forensic baseline for the 80% product-completion program. It intentionally treats `left stuff to do.md` as low-trust because several entries are stale: queue, packaging, volume, lyrics, Now Playing, downloads, and CI are now implemented in some form.

Primary evidence used:

- `README.md`
- `CHANGELOG.md`
- `docs/architecture.md`
- `docs/qa/full-functionality-audit.md`
- `docs/qa/all-reference-screens-pixel-lock.md`
- `docs/release/windows-packaging.md`
- `docs/qa/windows-installer-release-qa.md`
- desktop source under `composeApp/src/desktopMain/kotlin`
- desktop tests under `composeApp/src/desktopTest/kotlin`
- existing runtime QA JSON artifacts under `docs/qa/`
- GitHub release state for `v0.1.1-rc.1`

## 1. Current functional inventory

Implemented in current source:

- Compose Desktop shell with custom undecorated main window.
- Sidebar navigation, top bar, bottom player, mini player, and system tray.
- Provider-backed YouTube Music search through `YouTubeService` and `innertube`.
- Search submission through standard Enter and numpad Enter using preview key handling.
- Multi-category search aggregation for songs, artists, albums, and playlists.
- Per-category search failure tolerance so one failed category does not discard valid results from other categories.
- Persistent recent search history in Java Preferences JSON.
- VLC/libVLC playback through `VlcjAudioEngine`.
- Play, pause, previous, next, seek, volume, shuffle, and repeat.
- Local-file-first playback path for completed downloads.
- Queue, queue recommendations, and queue Save as Playlist.
- Persistent saved queue playlists in Java Preferences JSON.
- Liked song IDs persisted in Java Preferences.
- Playback history and playback sessions with meaningful-listen threshold and 30-minute session boundary.
- File-backed download manager with persistent task index, real byte progress, pause, resume, retry, cancel, delete, and completed-file validation.
- Provider-backed lyrics with truthful synced/unsynced/no-lyrics/error handling.
- Four live theme modes: Nocturne Prism, Midnight, Dusk, Aurora.
- Reduced-motion policy infrastructure consumed by some transitions and press/expand effects.
- Windows EXE/MSI packaging with private Java runtime and bundled VLC/libVLC runtime.
- Desktop backend tests for downloads, settings, history, queue playlist persistence, and QA-gated runtime paths.

Runtime/QA evidence already present:

- `docs/qa/runtime-download-qa.json`: provider-backed download of `Blinding Lights`, nonzero file, restart restore, local source selected.
- `docs/qa/offline-playback-qa.json`: real VLC playback from local file, position advance, seek, pause, resume; physical network disable not executed.
- `docs/qa/queue-save-ui-qa.json`: persisted 4-track queue playlist verified after restart.
- `docs/qa/mini-player-aot-qa.json`: native always-on-top property changes on an existing mini-player window.
- `docs/qa/all-reference-screens-pixel-lock.md`: all eight target screen captures exist; final verdict is close but measurable differences remain.
- `docs/qa/windows-installer-release-qa.md`: packaging passes; clean-machine installer QA remains unproven.

## 2. Current screen inventory

Protected/polished baselines:

- Home
- Search & Discovery
- Bottom player true-center alignment
- Nocturne Prism default theme

Implemented major screens:

- Browse
- Radio
- Library
- Playlists
- Playlist Detail
- Artist Detail
- Album Detail
- Now Playing + Lyrics
- Queue & Session
- Settings
- Downloads & Offline
- Mini Player

Screen maturity:

- Home and Search are the strongest visual surfaces.
- Settings and Queue are close visually but still have measurable panel dimension/offset differences.
- Library, Playlist Detail, Artist Detail, Album Detail, and Downloads are functional but need reference-geometry correction.
- Now Playing + Lyrics is the largest visual mismatch and should remain Phase 1 priority.
- Browse and Radio are currently implemented as thin provider-backed pages, not premium destination products.

## 3. Current dead/partial controls

No obvious empty `onClick {}` was found in the current scanned source, but semantic partials remain:

- Browse category click currently attempts `player.openAlbum()` with a mood/genre browse ID. That is functionally questionable; Browse needs a real genre/mood destination or filtered discovery route.
- Radio currently displays new release albums, not a real seeded radio experience. It is provider-backed but conceptually incomplete.
- SMTC integration contains TODO stubs in `SmtcManager.kt`; media-key/system media metadata support should be treated as not implemented until wired.
- Some settings rows are intentionally informational, not interactive, because truthful capability is unavailable.
- Artist tours/social stats and album credits use truthful unavailable states; these are external-data limitations, not app-owned dead UI.
- Playlist/album download paths enqueue real songs, but full album/playlist runtime download QA remains incomplete.
- Reduced motion is implemented centrally but not yet audited across every animation usage.
- Mini-player always-on-top is proven by native property; physical stacking behavior remains not automated.

## 4. Current visual mismatches

Source: `docs/qa/all-reference-screens-pixel-lock.md` and diff metrics under `docs/qa/diff/`.

Highest priority mismatches:

1. Now Playing + Lyrics:
   - badge target x≈297, actual x≈600
   - artwork target x≈297, actual x≈548
   - transport roughly 73–90 px too high
   - lyrics panel target x≈983/y≈81/w≈663/h≈751, actual x≈1049/y≈148/w≈584/h≈628
2. Downloads:
   - page is significantly top-shifted
   - stat cards ~62 px too high
   - right-side modules ~44–66 px too high
3. Library:
   - lower sections sit too low, especially All Songs ~29 px below target
4. Playlist Detail:
   - right rail starts too low and is too short
5. Artist Detail:
   - tabs and lower columns sit too low/right
6. Album Detail:
   - info and related panels are slightly down/right
7. Queue:
   - major panels are close in X/Y but short in height
8. Settings:
   - close; middle/right columns and About width need small corrections

Diff metrics currently remain materially above any pixel-lock standard because dynamic data is not masked and several structural deltas remain.

## 5. Current playback risks

- `PlayerViewModel` still uses production `println()` statements around provider/player resolution, including partial stream URL logging. This should become structured diagnostics with sensitive URL/query handling.
- `left stuff to do.md` warns that YouTube stream seeking can be unreliable. Current VLC seek is wired and local-file seek QA passed, but broad stream edge-case seek QA remains needed.
- Provider fallback exists in playback resolution, but provider/client resilience needs deeper audit for token expiry, cipher URLs, playability failures, and bounded retry behavior.
- `VlcjAudioEngine.release()` launches asynchronous release work; shutdown should be audited for deterministic native cleanup.
- Long-running playback soak, rapid skipping, network interruption, invalid stream recovery, and queue-end cases are not yet proven at release quality.
- SMTC/media-key integration is currently TODO and should not be claimed complete.

## 6. Current provider risks

- Provider behavior is external and can change without repo changes.
- Search lower-level provider test passes, but broad search category/provider resilience needs more tests.
- `left stuff to do.md` flags signatureCipher/cipher deobfuscation, visitorData refresh, poToken, and client fallback as risks. These require current-code verification before implementation.
- Artist socials, monthly listeners, tours, and album studio/producer credits are not available from a verified current provider source and must remain truthful unavailable states.
- One failed category search no longer kills all search results; this is a current strength that must be preserved.

## 7. Current persistence risks

- Settings, recent searches, liked IDs, saved queue playlists, playback history, and sessions use Java Preferences.
- Downloads use `downloads-index.json` under app-data.
- Java Preferences is acceptable for small settings but may be weak for large history/playlists. Stress tests are needed before any storage migration decision.
- `SettingsRepository.playbackHistory` currently stores distinct-by-song and caps at 200; this may be too lossy for a serious history surface.
- Saved queue playlist implementation dedupes songs by ID when saving; useful for duplicates prevention but may violate exact queue semantics if a user intentionally queued duplicates.
- Corruption recovery exists in some JSON parsing paths through `runCatching`, but systematic malformed persistence tests are not complete.
- App-data location is `%LOCALAPPDATA%\OmniTune` for current runtime; legacy migration from `~\.omnitune` exists.

## 8. Current installation risks

- `v0.1.1-rc.1` exists and supersedes `0.1.0` to avoid Windows Installer same-version error `1638`.
- EXE/MSI installers are unsigned; SmartScreen warnings are expected.
- Clean Windows machine test remains unproven.
- No-system-Java and no-system-VLC tests remain unproven on a clean machine.
- Current machine has both an older `OmniTuneWindows 1.0.0` install and `OmniTune 0.1.0`; it is not a clean QA target.
- Installer lifecycle tests for install, Start menu launch, uninstall, reinstall, and upgrade remain incomplete.
- Release workflow passes on manual dispatch from `main`; the earlier tag-triggered failure was fixed in workflow after the tag.

## 9. Current performance risks

- No several-hour playback soak evidence.
- No large-data stress results for 1,000 history entries, 100 playlists, 5,000 playlist tracks, or 50 downloads.
- No CPU/thread/native-handle leak report for long playback.
- Artwork/cache behavior under prolonged use is not measured.
- JSON/Preferences persistence may become slow or lossy under large data unless stress-tested.
- Startup should be measured in installed app, especially VLC discovery and first provider initialization.

## 10. Current accessibility risks

- Reduced motion exists but is not fully audited across all animation usages.
- Keyboard shortcuts exist for shell controls, but full tab order/focus traversal is not documented.
- Icon-only controls need a full content-description/tooltip audit.
- Hand cursor/hover/press affordance is not uniformly verified across every clickable card/row.
- Multiple Windows DPI scale factors are not fully validated beyond screenshot responsiveness.
- Theme contrast across all four themes needs a systematic pass on every major screen.

## Status matrix

Legend: `PASS`, `PARTIAL`, `FAIL`, `NOT APPLICABLE`.

| Area | Implemented | Fully functional | Runtime proven | Visually polished | Release-ready |
|---|---|---|---|---|---|
| Home | PASS | PASS | PASS | PASS | PARTIAL |
| Browse | PASS | PARTIAL | PARTIAL | PARTIAL | FAIL |
| Radio | PASS | PARTIAL | PARTIAL | PARTIAL | FAIL |
| Search | PASS | PASS | PASS | PASS | PARTIAL |
| Library | PASS | PARTIAL | PARTIAL | PARTIAL | PARTIAL |
| Songs | PASS | PASS | PASS | PARTIAL | PARTIAL |
| Albums | PASS | PARTIAL | PARTIAL | PARTIAL | PARTIAL |
| Artists | PASS | PARTIAL | PARTIAL | PARTIAL | PARTIAL |
| Playlists | PASS | PARTIAL | PARTIAL | PARTIAL | PARTIAL |
| Playlist Detail | PASS | PARTIAL | PARTIAL | PARTIAL | PARTIAL |
| Artist Detail | PASS | PARTIAL | PARTIAL | PARTIAL | PARTIAL |
| Album Detail | PASS | PARTIAL | PARTIAL | PARTIAL | PARTIAL |
| Now Playing | PASS | PARTIAL | PARTIAL | FAIL | PARTIAL |
| Lyrics | PASS | PARTIAL | PARTIAL | PARTIAL | PARTIAL |
| Queue | PASS | PASS | PASS | PARTIAL | PARTIAL |
| Downloads | PASS | PARTIAL | PASS | PARTIAL | PARTIAL |
| Settings | PASS | PARTIAL | PARTIAL | PASS | PARTIAL |
| Mini Player | PASS | PARTIAL | PARTIAL | PARTIAL | PARTIAL |
| System Tray | PASS | PARTIAL | PARTIAL | NOT APPLICABLE | PARTIAL |
| Search provider | PASS | PARTIAL | PASS | NOT APPLICABLE | PARTIAL |
| Playback | PASS | PARTIAL | PARTIAL | NOT APPLICABLE | PARTIAL |
| Offline playback | PASS | PARTIAL | PARTIAL | NOT APPLICABLE | PARTIAL |
| Downloads | PASS | PARTIAL | PASS | PARTIAL | PARTIAL |
| History | PASS | PARTIAL | PASS | PARTIAL | PARTIAL |
| Persistence | PASS | PARTIAL | PASS | NOT APPLICABLE | PARTIAL |
| Installer | PASS | PARTIAL | PARTIAL | NOT APPLICABLE | PARTIAL |

## Phase 0 conclusion

OmniTune Windows is a real product-shaped desktop app, not a mockup. It has meaningful playback, provider search, downloads, offline local-file playback path, persistence, major screens, themes, and installers. The strongest blockers to an honest 80% premium gate are not raw feature absence; they are polish, proof, and hardening:

1. Now Playing + Lyrics needs significant reference reconstruction and behavior polish.
2. Browse and Radio need to become real premium destinations.
3. Playback needs reliability/soak testing and structured recovery/logging.
4. Library/playlists/history need stress testing and completion of management actions.
5. Downloads need full UI-path and physical offline QA.
6. Installer needs clean-machine no-Java/no-VLC validation and lifecycle testing.
7. Accessibility/focus/reduced-motion/theme coverage needs a full pass.

Phase 1 should begin with Now Playing + Lyrics visual reconstruction, but Home, Search, bottom-player centering, Nocturne Prism, provider-backed search, downloads, offline local playback path, history, queue Save as Playlist, persistent user state, and installer packaging must remain protected.
