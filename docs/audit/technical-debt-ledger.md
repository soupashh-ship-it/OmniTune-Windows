# OmniTune Windows Technical Debt Ledger

Date: 2026-07-15

This ledger records verified findings only. Severity is intentionally conservative.

## TD-PLAYBACK-001

| Field | Value |
| --- | --- |
| Severity | HIGH |
| Category | Playback / Concurrency |
| Title | Stale playback resolution could start an older track after a newer selection |
| Confidence | CONFIRMED |
| Affected files | `PlayerViewModel.kt` |
| Affected symbols | `doPlay`, `playSong`, `playSongList`, `playShuffledSongs`, `playAlbum`, `playPlaylist`, `playQueueIndex`, `startRadio` |
| Evidence | Playback starts are launched as coroutines. URL resolution is asynchronous. Before remediation, an older `doPlay` coroutine could resolve after a newer request and still call `audioEngine.play(...)`. |
| Root cause | No request identity/generation check around asynchronous source resolution. |
| User impact | Rapid track switching could play the wrong track or show mismatched current-song/stream state. |
| Engineering impact | Makes playback behavior race-prone and difficult to reason about under slow provider/network conditions. |
| Fix | Added a dedicated `PlaybackRequestGate`. Every playback request gets a token; `doPlay` verifies token and current song before local playback, stream playback, and failure recovery. |
| Risk | Medium; playback code is high-risk, but the change is localized and preserves existing queue semantics. |
| Test strategy | Compile; targeted desktop tests; final full desktop test suite. Future ideal test: fake delayed resolver/audio engine characterization. |
| Phase | A — critical playback/concurrency safety |
| Status | FIXED |

## TD-DOWNLOAD-001

| Field | Value |
| --- | --- |
| Severity | HIGH |
| Category | Downloads / Filesystem safety |
| Title | Download index local paths were trusted without managed-directory validation |
| Confidence | CONFIRMED |
| Affected files | `OmniDownloadManager.kt`, `OmniDownloadManagerTest.kt` |
| Affected symbols | `delete`, `completedDownloadFor`, `restoreTasks` |
| Evidence | `delete(id)` deleted `File(localFilePath)` from persisted metadata. `completedDownloadFor` accepted any non-empty file path from persisted metadata. |
| Root cause | Persisted download metadata was treated as trusted even though JSON can be corrupted or externally edited. |
| User impact | A corrupted or malicious downloads index could cause OmniTune to trust or delete a file outside `%LOCALAPPDATA%\OmniTuneData\downloads`. |
| Engineering impact | Weakens data-boundary safety around file-backed downloads. |
| Fix | Added canonical managed-download directory validation for completed-file trust and deletion. Outside paths are restored as failed and are not deleted. |
| Risk | Low-to-medium; affects restored/downloaded-file trust only. |
| Test strategy | Added tests for outside managed directory not being trusted and not being deleted. |
| Phase | A — filesystem/data-integrity safety |
| Status | FIXED |

## TD-DOWNLOAD-002

| Field | Value |
| --- | --- |
| Severity | MEDIUM |
| Category | Downloads / Path construction |
| Title | Provider-controlled track IDs were used directly in download filenames and partial filenames |
| Confidence | CONFIRMED |
| Affected files | `OmniDownloadManager.kt` |
| Affected symbols | `download`, `partialPath` |
| Evidence | Final filenames used `${song.id}` directly and partial filenames used raw task IDs. Track IDs come from external provider data. |
| Root cause | Filename sanitization was applied to title but not to all filename fragments. |
| User impact | Unusual provider IDs could create invalid filenames or path-like fragments. |
| Engineering impact | Leaves file handling more fragile than necessary. |
| Fix | Reused `safeFileName` for song ID fragments and partial task filenames. |
| Risk | Low; normal YouTube IDs remain unchanged except invalid characters are replaced. |
| Test strategy | Existing download tests passed; managed-path tests added. |
| Phase | A — filesystem safety |
| Status | FIXED |

## TD-LIKED-001

| Field | Value |
| --- | --- |
| Severity | MEDIUM |
| Category | Liked Songs / User-visible correctness |
| Title | Liked Songs Download All message counted all visible songs instead of actually queued downloads |
| Confidence | CONFIRMED |
| Affected files | `LikedSongsView.kt` |
| Affected symbols | Liked Songs hero download action |
| Evidence | UI filtered out already-downloaded songs before enqueuing but displayed `visibleSongs.size` in the success message. |
| Root cause | Message used the pre-filtered list instead of the pending download list. |
| User impact | User could be told downloads were queued when no eligible downloads were queued. |
| Engineering impact | Misleading UI state around download operations. |
| Fix | Message now uses the pending list and reports when all visible liked songs are already downloaded. |
| Risk | Low. |
| Test strategy | Compile and desktop tests. |
| Phase | A — current feature correctness |
| Status | FIXED |

## TD-LIKED-002

| Field | Value |
| --- | --- |
| Severity | LOW |
| Category | Liked Songs / User-visible correctness |
| Title | Liked Songs rail downloaded count counted all completed downloads instead of liked-song downloads |
| Confidence | CONFIRMED |
| Affected files | `LikedSongsView.kt` |
| Affected symbols | `LikedSongsRail` input mapping |
| Evidence | Rail received `downloadedIds.size`, which includes downloads outside the liked-song collection. |
| Root cause | Aggregate was computed from global download state rather than liked-song records. |
| User impact | Right-rail collection stat could be inflated. |
| Engineering impact | Incorrect derived state in collection-specific UI. |
| Fix | Count now uses `records.count { it.song.id in downloadedIds }`. |
| Risk | Low. |
| Test strategy | Compile and desktop tests. |
| Phase | A — current feature correctness |
| Status | FIXED |

## TD-ARCH-001

| Field | Value |
| --- | --- |
| Severity | MEDIUM |
| Category | Architecture / Maintainability |
| Title | `PlayerViewModel` owns too many unrelated responsibilities |
| Confidence | CONFIRMED |
| Affected files | `PlayerViewModel.kt` |
| Evidence | File is over 1100 lines and owns navigation, search, playlist search, playback, queue, lyrics, related, radio, settings mutations, liked songs, followed artists, downloads, history, and persistence coordination. |
| Root cause | Feature growth accumulated into a single application-level state owner. |
| User impact | No immediate user-facing bug by itself. |
| Engineering impact | Increases regression risk for future changes; unrelated features can affect playback state and vice versa. |
| Fix | Extracted the concrete playback request-generation/state-checking responsibility into `PlaybackRequestGate`. A broad ViewModel split was intentionally not performed because it would be a high-risk architecture rewrite outside the verified defect. |
| Risk | High if over-refactored. |
| Test strategy | Any future split requires characterization tests for playback, queue, search, radio, downloads, and persistence. |
| Phase | F — architecture/state-management debt |
| Status | FIXED WITH LIMITED-SCOPE EXTRACTION |

## TD-PROVIDER-001

| Field | Value |
| --- | --- |
| Severity | MEDIUM |
| Category | Provider parsing / Reliability |
| Title | Provider parsers contain multiple force-null assertions on external response fields |
| Confidence | CONFIRMED |
| Affected files | `innertube/src/main/kotlin/com/omnitune/innertube/YouTube.kt`, `innertube/pages/*.kt` |
| Evidence | Repository scan found multiple `!!` assertions in provider response parsing paths, including browse IDs, headers, sections, and playlist panel renderers. A follow-up scan now finds none in `innertube` or the audited desktop route/player paths. |
| Root cause | External API schema assumptions are enforced through `!!` rather than safe fallback or record skipping. |
| User impact | Provider response shape changes can crash route loading instead of producing partial/empty/failure state. |
| Engineering impact | Fragile provider integration and harder incident debugging. |
| Fix | Replaced provider force-null assertions with clear `IllegalStateException` for required top-level fields, empty-list fallback for optional shelves/sections, or item skipping for invalid partial records. Removed remaining `!!` in `innertube`. |
| Risk | Medium; parser changes can alter provider results. |
| Test strategy | Compile; full test gate. Future provider fixture tests are still useful for schema drift, but no parser force unwraps remain. |
| Phase | H — provider reliability |
| Status | FIXED |

## TD-UI-001

| Field | Value |
| --- | --- |
| Severity | MEDIUM |
| Category | Compose / Maintainability |
| Title | Several composable files are oversized and mix layout, state derivation, and actions |
| Confidence | CONFIRMED |
| Affected files | `HomeView.kt`, `SearchView.kt`, `PlaylistDetailView.kt`, `LikedSongsView.kt`, `NowPlayingView.kt`, `Screens.kt`, `OmniBottomPlayer.kt` |
| Evidence | Multiple UI files range from ~780 to 1550 lines. Some route composables contain local state, visual layout, menu actions, data derivation, and routing callbacks in one file. |
| Root cause | Rapid feature implementation without component extraction boundaries. |
| User impact | No immediate user-facing failure by itself. |
| Engineering impact | Higher risk of accidental UI regressions and duplicated component styling. |
| Fix | Extracted shared provider loading/error states from `Screens.kt` into `ProviderStates.kt`. Broad route decomposition remains intentionally avoided because it would be visual-regression-prone and not tied to a concrete defect. |
| Risk | High if changed broadly. |
| Test strategy | Visual regression screenshots and desktop UI tests for any touched screen. |
| Phase | G/J — Compose maintainability and duplication |
| Status | FIXED WITH LIMITED-SCOPE EXTRACTION |

## TD-UI-002

| Field | Value |
| --- | --- |
| Severity | MEDIUM |
| Category | UI route reliability / Null safety |
| Title | Route loading paths used force-null assertions after nullable async state |
| Confidence | CONFIRMED |
| Affected files | `AlbumView.kt`, `ArtistView.kt`, `PlaylistDetailView.kt`, `HomeView.kt`, `SearchView.kt` |
| Evidence | Route composables used `albumId!!`, `artistId!!`, `playlistId!!`, `page!!`, `home!!`, `expandedSection!!`, and `actionMessage!!` in code paths dependent on async provider state. |
| Root cause | Nullable loading state was checked in `when`/`if` branches, but actions and rendering reused nullable state through force-null assertions. |
| User impact | A race between provider loading, route changes, and recomposition could crash instead of showing a loading/empty state. |
| Engineering impact | Force-null assertions hide route state invariants and make future route changes brittle. |
| Fix | Captured non-null local values before rendering/actions, removed redundant nested coroutine launches inside `LaunchedEffect`, and replaced status-message force unwrap with `let`. |
| Risk | Low; no intended UI behavior changed. |
| Test strategy | Compile; final desktop test suite. |
| Phase | C/G — route state safety |
| Status | FIXED |

## TD-QA-001

| Field | Value |
| --- | --- |
| Severity | LOW |
| Category | Production hygiene |
| Title | Production source contains QA environment hooks |
| Confidence | CONFIRMED |
| Affected files | `Main.kt`, `OmniWindow.kt` |
| Evidence | Production code previously read `OMNITUNE_QA_*` variables directly in several files. A follow-up scan now shows those variables centralized in `QaRuntime`. |
| Root cause | Runtime QA harnesses were integrated directly into desktop production source. |
| User impact | Low in normal use; dormant unless env vars are set. |
| Engineering impact | Blurs production/test boundaries and can confuse packaging audits. |
| Fix | Added `QaRuntime` and routed QA variable reads through it. Existing QA workflows remain available, but raw `OMNITUNE_QA_*` access is now centralized and auditable. |
| Risk | Low if left dormant; medium if removed without replacing QA workflows. |
| Test strategy | Existing runtime QA scripts before and after any isolation. |
| Phase | K — test/documentation hygiene |
| Status | FIXED |
