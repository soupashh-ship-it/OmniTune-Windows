# OmniTune Windows Improvement Roadmap

Audit date: 2026-07-16

This roadmap is based on the current source at HEAD `951d421`. It intentionally avoids broad rewrites. Implement in phases with tests and release smoke checks.

## Phase 1 — Critical correctness and data safety

Linked issues:

- `PERSIST-001`
- `UX-001`
- `PRODUCT-003`
- `PRODUCT-005`
- `REL-001`
- `REL-002`

Recommended order:

1. Fix playlist custom cover persistence by importing selected artwork into managed app data.
2. Remove/disable or implement active Settings controls that only persist flags.
3. Clarify Settings/About update behavior until a real updater exists.
4. Create installed-version verification checklist and clean legacy install guidance.
5. Start code-signing plan.

Expected benefit:

- Removes misleading UI and prevents user-visible playlist artwork loss.
- Reduces release/install confusion.

Risk:

- Medium for playlist migration and Settings behavior changes.

Prerequisite tests:

- Playlist cover import/reload test.
- Settings controls behavior/honesty tests.
- Installed app version smoke.

## Phase 2 — Playback and queue reliability

Linked issues:

- `PLAYBACK-001`
- `PLAYBACK-002`
- `QUEUE-001`
- `PRODUCT-007`

Recommended order:

1. Add regression test for stale playback source resolution using delayed fake resolver.
2. Redesign `VlcjAudioEngine.release()` to avoid unbounded caller blocking while preserving shutdown safety.
3. Decide and implement deterministic shuffle order/history.
4. Run real SMTC/hardware media key smoke.

Expected benefit:

- Protects the most damaging class of playback regressions.

Risk:

- High around playback lifecycle; keep changes small.

Prerequisite tests:

- Fake playback resolver A/B race.
- Queue shuffle next/previous behavior tests.
- Shutdown/release smoke.

## Phase 3 — Persistence and downloads

Linked issues:

- `DOWNLOAD-001`
- `DOWNLOAD-002`
- `PERSIST-002`

Recommended order:

1. Add explicit HTTP response-code handling in downloads.
2. Run installed offline proof.
3. Plan but do not immediately execute local DB migration.
4. Add migration tests before moving persisted data.

Expected benefit:

- Stronger offline confidence and clearer failure handling.

Risk:

- Low for HTTP response handling; high for DB migration.

Prerequisite tests:

- Fake HTTP 403/404/500 download tests.
- Clean VM/offline installed smoke.

## Phase 4 — Responsive layout and UI correctness

Linked issues:

- `UI-001`
- `UI-003`
- `ACCESS-001`
- `TEST-001`

Recommended order:

1. Expand `OmniResponsiveLayout` from Settings-only helper into global shell/content/table/card contract.
2. Fix Queue, Playlist Detail, Liked Songs, Search, Album, Artist, Downloads fixed-table and tiny-font hotspots.
3. Define minimum interactive target sizes.
4. Add automated screenshot capture/diff or at least controlled screenshot artifact generation.

Expected benefit:

- App behaves intentionally at real Windows sizes/DPI.

Risk:

- Medium due visual regression potential.

Prerequisite tests:

- Responsive decision unit tests.
- Manual 1906x1066, 1672x941, 1366x768, 1280x720, 1187x789, 1012x643 visual QA.

## Phase 5 — Product functionality gaps

Linked issues:

- `UX-002`
- `UX-003`
- `PRODUCT-001`
- `PRODUCT-002`
- `PRODUCT-004`
- `PRODUCT-006`

Recommended order:

1. Replace heuristic playlist mood chips with honest metadata.
2. Decide drag-and-drop reordering scope.
3. Implement real update check before any auto-update installer path.
4. Add opt-in log export/support bundle.
5. Keep account/social/cloud UI local-only until backend exists.

Expected benefit:

- Reduces fake-feeling product surfaces.

Risk:

- Medium; account/cloud/social should not be started without product/legal plan.

Prerequisite tests:

- Playlist metadata tests.
- Update-check mock tests.
- Log export tests.

## Phase 6 — Architecture and state management

Linked issues:

- `ARCH-001`
- `ARCH-002`
- `ARCH-003`
- `PERF-001`

Recommended order:

1. Extract listen/session tracker from `PlayerViewModel`.
2. Extract liked/followed/library state controller.
3. Extract discovery loader.
4. Move QA runtime hooks behind diagnostic build mode or centralized runtime-gated adapter.
5. Extract screen sections only when touching those screens.

Expected benefit:

- Smaller blast radius for future features and better test seams.

Risk:

- Medium-high if done without characterization tests.

Prerequisite tests:

- Player facade tests.
- Persistence reload tests.
- UI compile/smoke after every extraction.

## Phase 7 — Performance

Linked issues:

- `PERF-001`
- `UI-001`

Recommended order:

1. Profile large Liked Songs, Playlist, Downloads, Queue.
2. Move expensive derived state out of high-frequency composition paths.
3. Ensure playback position updates do not recompose large screens.
4. Add large-list test data modes.

Expected benefit:

- Better resize, scroll, and playback-progress smoothness.

Risk:

- Medium.

Prerequisite tests:

- Large collection runtime QA.
- Recomposition/profiler evidence.

## Phase 8 — Accessibility

Linked issues:

- `UI-002`
- `ACCESS-001`

Recommended order:

1. Label interactive icon-only controls.
2. Add keyboard traversal checks for menus/sheets/dialogs.
3. Test Windows Narrator/NVDA.
4. Test high contrast and 125%/150% scaling.

Expected benefit:

- App becomes more usable and professional.

Risk:

- Low.

Prerequisite tests:

- Semantics tests where Compose Desktop supports them.
- Manual screen-reader checklist.

## Phase 9 — Tests and documentation

Linked issues:

- `TEST-001`
- `TEST-002`
- `TEST-003`
- `DOCS-001`
- `REPO-001`
- `REPO-002`
- `BUILD-001`
- `BUILD-002`

Recommended order:

1. Add provider fixture corpus.
2. Add visual regression artifact policy.
3. Move `local.properties` to example/ignored real file.
4. Clean unused version catalog entries.
5. Archive superseded audit docs.

Expected benefit:

- Cleaner repo and safer regression workflow.

Risk:

- Low-medium.

Prerequisite tests:

- Clean build after dependency/catalog cleanup.
- Fixture tests.

## Phase 10 — Final hardening

Linked issues:

- `REL-001`
- `REL-002`
- `REL-003`
- `DOWNLOAD-001`
- `TEST-002`

Recommended order:

1. Reproducible packaging on clean VM/CI.
2. Signed installer.
3. Clean install/upgrade/uninstall test.
4. Offline installed playback test.
5. Multi-hour playback soak.
6. Final accessibility and visual QA sign-off.

Expected benefit:

- Moves OmniTune from strong beta toward credible public Windows app.

Risk:

- Medium due environment dependencies.

Prerequisite tests:

- Full build/test gate.
- Installed smoke.
- Offline smoke.
- Soak report.
