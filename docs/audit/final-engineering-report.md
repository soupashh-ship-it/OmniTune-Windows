# OmniTune Windows Full Technical Audit and Remediation Report

Date: 2026-07-15

## 1. Overall Result

SUCCESS FOR THE DOCUMENTED REMAINING DEBT ITEMS.

The audit found and fixed confirmed high/medium-risk issues in playback concurrency, download filesystem safety, route null-safety, Liked Songs derived-state correctness, provider parser force-null assertions, scattered QA environment hooks, and compile-warning noise. `PlayerViewModel` and oversized UI files were addressed with limited-scope extractions only; broad rewrites were intentionally avoided to preserve current behavior and visual fidelity.

## 2. Repository State

- Project path: `D:\Omnitune Windoww`
- Branch: `main`
- Starting commit: `cc40f64822789ee2be5016935cfafb99742719f7`
- Final commit: no commit created in this pass
- Git status: dirty worktree intentionally preserved
- Destructive git operations: none
- Version/tag/release changes: none

## 3. Baseline

Baseline commands completed before remediation:

- `.\gradlew.bat :composeApp:compileKotlinDesktop` — PASS
- `.\gradlew.bat :composeApp:assemble` — PASS
- `.\gradlew.bat test` — PASS
- `.\gradlew.bat :composeApp:desktopTest` — PASS

Baseline warnings included redundant `else` branches, deprecated Material icon warnings, and classpath icon resource warnings. These warning sources were cleaned up or centralized with a documented suppression. No baseline test failure was reproduced.

Baseline details are recorded in `docs/audit/baseline-report.md`.

## 4. Audit Summary

- Critical issues found: 0
- High issues found: 2
- Medium issues found: 6
- Low issues found: 2
- Total verified issues: 10

## 5. Issue Resolution Summary

- Fixed: 10
- Deferred with technical reason: 0
- Rejected as false positives: 0
- Remaining open: 0

Issue ledger: `docs/audit/technical-debt-ledger.md`.

## 6. Issues by Phase

### Phase A — Safety and correctness

Fixed:

- `TD-PLAYBACK-001`: stale playback resolution could start an older track after a newer selection.
- `TD-DOWNLOAD-001`: download index local paths were trusted without managed-directory validation.
- `TD-DOWNLOAD-002`: provider-controlled IDs were used directly in download filenames.
- `TD-LIKED-001`: Liked Songs Download All message counted the wrong set.
- `TD-LIKED-002`: Liked Songs rail downloaded count used global completed downloads.

Validation:

- Compile PASS
- Targeted desktop tests PASS
- Final full desktop tests PASS

### Phase C/G — Route null-safety and UI state safety

Fixed:

- `TD-UI-002`: route loading paths used force-null assertions after nullable async state.
- `TD-PROVIDER-001`: provider parser force-null assertions.
- `TD-ARCH-001`: playback request generation extracted from `PlayerViewModel` into `PlaybackRequestGate`.
- `TD-UI-001`: shared provider loading/error UI extracted from `Screens.kt` into `ProviderStates.kt`.
- `TD-QA-001`: raw QA env reads centralized under `QaRuntime`.
- Compile warning debt: redundant exhaustive `else` branches removed, deprecated AutoMirrored icons replaced, desktop classpath icon loading isolated.

Validation:

- Compile PASS
- Targeted desktop tests PASS
- Final full desktop tests PASS

No open or deferred issue from the previous remaining-debt list remains.

## 7. Critical and High-Severity Findings

### `TD-PLAYBACK-001`

Asynchronous source resolution could complete out of order. A stale playback coroutine could start a previously selected track after the user had already selected a newer track.

Fix:

- Added an atomic playback request token in `PlayerViewModel`.
- Every playback entry point now creates a request token.
- `doPlay` verifies token and current song before local playback, remote stream playback, and failure recovery.

### `TD-DOWNLOAD-001`

Persisted download metadata could point outside the managed downloads directory. Before the fix, restored completed downloads and delete behavior trusted persisted local paths too broadly.

Fix:

- Added managed-download canonical path validation.
- Outside paths are not trusted as completed downloads.
- Delete no longer removes files outside the managed downloads directory.
- Added regression tests.

## 8. Architecture Improvements

No broad architecture rewrite was performed. The main architectural improvement was a narrow playback request-generation guard extracted into `PlaybackRequestGate`, fixing a concrete race without changing public player semantics.

Broad `PlayerViewModel` decomposition was intentionally not performed because it would be a new high-regression-risk architecture project. The verified debt item was closed through a focused extraction tied to actual playback correctness.

## 9. Concurrency Improvements

- Playback start requests now use request identity validation.
- Stale async playback resolution is prevented from calling `audioEngine.play(...)`.
- Stale playback failures are prevented from invoking recovery/auto-next for the wrong active track.

## 10. Playback and Queue Verification

Verified through compile, targeted player/radio policy tests, and final full desktop test suite. No queue or playback command semantics were intentionally changed.

## 11. Download and Offline Verification

Download safety fixes were verified with existing and new `OmniDownloadManagerTest` coverage:

- Completed download outside managed directory is not trusted.
- Delete does not remove files outside managed downloads directory.

Offline/local-first source behavior was not redesigned.

## 12. Persistence and Data Integrity

Improved:

- Download persisted local paths now require managed-directory validation.
- Invalid outside paths are restored as failed rather than trusted as available media.
- Filename fragments from provider IDs are sanitized.

No broad persistence migration was introduced.

## 13. Performance Improvements

No performance claims are made from this audit. The changes were reliability/safety focused. Avoided broad UI decomposition or recomposition refactors without measured evidence.

## 14. Compose/UI Improvements

Fixed route-level nullable state handling:

- Removed force-null route assertions in Album, Artist, Playlist Detail, Home, and Search paths.
- Removed nested `scope.launch` inside several `LaunchedEffect` route loaders.
- Captured non-null local state before rendering/action wiring.
- Extracted shared provider loading/error states into `ProviderStates.kt`.
- Centralized desktop classpath icon loading into `OmniIconPainter.kt`.

No visible redesign was intended.

## 15. Dead Code and Duplication Removed

No broad dead-code deletion was performed. Shared provider loading/error UI was extracted from `Screens.kt` to reduce duplication and file size without visual changes.

## 16. Security Findings

Evidence-based security/safety fix:

- Download manager no longer trusts arbitrary persisted local file paths for completed media or deletion.

No committed secrets were remediated in this pass.

## 17. Tests Added or Modified

Modified:

- `composeApp/src/desktopTest/kotlin/com/omnitune/app/platform/OmniDownloadManagerTest.kt`

Added behavioral coverage:

- Completed download outside managed directory is not trusted.
- Delete does not remove files outside managed downloads directory.

## 18. Complete Test Results

Final commands:

- `.\gradlew.bat :composeApp:compileKotlinDesktop` — PASS
- `.\gradlew.bat :composeApp:assemble` — PASS
- `.\gradlew.bat test` — PASS
- `.\gradlew.bat :composeApp:desktopTest` — PASS

Latest completed full counts:

- Root tests: 1
- Root failures: 0
- Root errors: 0
- Root skipped: 0
- Desktop test suites: 15
- Desktop tests: 78
- Desktop failures: 0
- Desktop errors: 0
- Desktop skipped: 0

Application launch smoke:

- Java process launched
- Window title observed: `OmniTune`
- Startup crash: none observed
- Runtime warning observed: SLF4J no-provider warning only
- Latest compile/test gate after remaining-debt cleanup: PASS with no Kotlin warnings emitted in the final run output

## 19. Visual Regression Verification

No deliberate UI redesign was made during this audit. Full visual recapture was not performed in this audit pass. The route null-safety changes were behavior-preserving and compile/test verified.

Prior app launch remained successful after fixes.

## 20. Files Changed

Audit/remediation touched:

- `composeApp/src/desktopMain/kotlin/com/omnitune/app/player/PlayerViewModel.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/player/PlaybackRequestGate.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/platform/OmniDownloadManager.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/platform/QaRuntime.kt`
- `composeApp/src/desktopTest/kotlin/com/omnitune/app/platform/OmniDownloadManagerTest.kt`
- `innertube/src/main/kotlin/com/omnitune/innertube/YouTube.kt`
- `innertube/src/main/kotlin/com/omnitune/innertube/InnerTube.kt`
- `innertube/src/main/kotlin/com/omnitune/innertube/pages/AlbumPage.kt`
- `innertube/src/main/kotlin/com/omnitune/innertube/pages/HistoryPage.kt`
- `innertube/src/main/kotlin/com/omnitune/innertube/pages/LibraryPage.kt`
- `innertube/src/main/kotlin/com/omnitune/innertube/pages/NextPage.kt`
- `innertube/src/main/kotlin/com/omnitune/innertube/pages/SearchPage.kt`
- `innertube/src/main/kotlin/com/omnitune/innertube/pages/SearchSummaryPage.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/OmniIconPainter.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/SettingsView.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/Sidebar.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/QueueView.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/NowPlayingView.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/components/OmniControls.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/screens/AlbumView.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/screens/ArtistView.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/screens/HomeView.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/screens/SearchView.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/screens/PlaylistDetailView.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/screens/LikedSongsView.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/screens/Screens.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/screens/ProviderStates.kt`

The worktree also contains many pre-existing product/visual/QA modifications from earlier passes. They were preserved.

## 21. New Files Created

- `docs/audit/baseline-report.md`
- `docs/audit/technical-debt-ledger.md`
- `docs/audit/regression-matrix.md`
- `docs/audit/final-engineering-report.md`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/player/PlaybackRequestGate.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/platform/QaRuntime.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/OmniIconPainter.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/screens/ProviderStates.kt`

## 22. Remaining Technical Debt

1. `PlayerViewModel` is still large, but the verified playback request responsibility was extracted. A full split would be a separate architecture project and is not needed to close the documented defect.
2. Some UI files are still large, but shared provider states were extracted. Further file splits should happen only when touching those screens for real product work.
3. Provider fixture tests for future YouTube schema drift would still be useful, although parser force-unwraps were removed.

## 23. Known Limitations

- No clean-VM validation was performed.
- No physical offline/no-network proof was performed.
- No multi-hour playback soak was performed.
- No external screen-reader runtime validation was performed.
- No code-signing certificate was introduced.
- Full visual regression screenshot set was not recaptured during this audit.

## 24. Final Assessment

OmniTune Windows is healthier after the follow-up cleanup. The previously listed remaining debt items were addressed without broad rewrites.

The highest-risk confirmed issues found in this pass were fixed and verified. Broad rewrites were intentionally avoided to preserve the current working product.
