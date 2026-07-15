# OmniTune Windows Performance and Smoothness Audit

Date: 2026-07-15

Scope: final performance, smoothness, responsiveness, animation, and perceived-quality pass.

## Summary

The primary locally confirmed performance issue was live window resizing. The root cause was not rendering complexity alone; the app was also doing persistent settings I/O on every individual window-size change. During manual drag resizing, this could trigger repeated `SettingsRepository.flush()` calls while Compose was also relaying continuous layout changes.

The second confirmed issue was broad high-frequency playback-position collection. Playback position updates were collected at the main app shell and main application level, which allowed 100 ms progress updates to invalidate more UI than necessary.

## Findings

### P0 — visible major jank

1. Window-size persistence ran on every resize frame.
   - Location: `composeApp/src/desktopMain/kotlin/com/omnitune/app/Main.kt`
   - Previous behavior: every `windowState.size` change immediately wrote width/height and flushed settings.
   - User-visible impact: manual resize could feel less direct because filesystem persistence happened during the drag.

### P1 — meaningful smoothness issue

1. Playback position was collected at the root application level for SMTC updates.
   - Location: `Main.kt`
   - Previous behavior: `player.position.collectAsState()` caused Compose state updates for SMTC position.
   - Impact: high-frequency position updates could recompose application-level content unnecessarily.

2. Playback position was collected in `OmniWindow` shell.
   - Location: `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/OmniWindow.kt`
   - Previous behavior: `player.position.collectAsState()` lived beside route, sidebar, top bar, navigation, and bottom-player state.
   - Impact: progress updates could invalidate the whole shell scope instead of only player surfaces.

### P2 — optimization opportunities not changed in this pass

1. Some responsive metrics recalculate continuously during resize.
   - Current decision: left unchanged because they are lightweight and required for immediate visual adaptation.

2. Backdrop and gradient objects may still be candidates for draw caching.
   - Current decision: no speculative effect changes were made because canonical visual fidelity is currently close.

3. VLC position poller and VLC native time events can both update position.
   - Current decision: not changed in this pass because UI recomposition scope was the higher-value bottleneck and tests were already stable.

### P3 — not worth touching now

1. Minor animation tuning.
   - No evidence of a release-blocking motion issue beyond resize smoothness.

2. Canonical 3 px Now Playing visual residual.
   - Not performance-related; intentionally preserved.

## Retained optimizations

1. Debounced persistent window-size writes.
   - Visual resize remains immediate.
   - Only secondary persistence waits for the resize stream to settle.

2. SMTC updates moved out of Compose root state collection.
   - SMTC now collects player flows in coroutine side effects.
   - Position changes update SMTC without forcing application-level Compose state.

3. Playback position collection scoped to player UI surfaces.
   - Now Playing collects position only inside `NowPlayingRoute`.
   - Bottom Player collects position only inside `BottomPlayerRoute`.
   - Sidebar, Top Bar, Browse, Settings, and unrelated route shell content are no longer directly invalidated by every position tick from `OmniWindow`.

## Rejected optimizations

1. Debouncing actual layout resizing.
   - Rejected because the content must follow the mouse immediately.

2. Size-bucketing all layout geometry.
   - Rejected because it could make resizing feel stepped or laggy.

3. Broad visual/effect simplification.
   - Rejected because canonical appearance should not be degraded without measured need.

4. New animation layer.
   - Rejected because the current complaint is responsiveness, not lack of decorative motion.

## Validation

- `.\gradlew.bat :composeApp:compileKotlinDesktop` — PASS
- `.\gradlew.bat :composeApp:assemble` — PASS
- `.\gradlew.bat test` — PASS
- `.\gradlew.bat :composeApp:desktopTest` — PASS, 74 tests, 0 failures, 0 errors
- `.\gradlew.bat :composeApp:desktopTest --tests com.omnitune.app.window.OmniComposeFocusRuntimeTest` — PASS, 11 tests, 0 failures, 0 errors

## Remaining limitations

1. Manual live-resize perception was not converted into an automated FPS metric in this pass.
2. No new broad profiling dependency was added.
3. Further gains may be possible from deeper artwork/effect caching, but no safe concrete bottleneck justified visual-risk changes during this pass.
