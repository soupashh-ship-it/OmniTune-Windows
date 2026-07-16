# OmniTune Windows Executive Verification Summary

Audit date: 2026-07-16  
Project root: `D:\Omnitune Windoww`  
Branch: `main`  
HEAD: `951d421`

## 1. Overall project health

Result: NEEDS REMEDIATION

OmniTune Windows is buildable, testable, and substantially improved from earlier audit states. The current source has a green baseline and meaningful architecture extractions. It is safe to continue developing.

It is not yet a fully productized premium Windows app. The biggest remaining gaps are release/install trust, feature honesty in Settings, responsive/accessibility consistency, provider fixture depth, installed offline proof, and continued architecture slimming.

## 2. Safe to continue developing

Yes.

Reasons:

- Full baseline build/test gate passed.
- Desktop app launch smoke passed.
- Previous high-risk download safety fixes are present.
- Previous playback request gate is present.
- No production `!!` force-null assertions were found.
- Player/Settings refactors introduced useful controllers and persistence helpers.

## 3. Release-blocking issues

Release blockers for broad public/non-beta release:

1. Installed-version confusion and old shortcuts/identities can make users run stale builds.
2. Installer is unsigned.
3. Several Settings controls appear active but only persist preferences.
4. Clean installed offline proof is not available.
5. Responsive/accessibility proof is incomplete.

These do not prevent continued beta development, but they should block treating the app as polished/public-stable.

## 4. Top 10 confirmed or high-confidence defects/risks

1. `REL-001` — Installed-version confusion remains a release blocker.
2. `REL-002` — Windows installer is unsigned.
3. `UX-001` — Settings audio/notification/auto-download toggles persist flags without real runtime behavior.
4. `PERSIST-001` — Playlist cover edit persists external absolute paths.
5. `PLAYBACK-001` — VLC release uses `runBlocking` and can block caller.
6. `QUEUE-001` — Shuffle previous/next does not preserve deterministic shuffled order.
7. `UI-001` — Fixed reference metrics remain widespread across UI.
8. `UI-002` — Many clickable icon controls lack accessible labels.
9. `PROVIDER-001` — Provider parser/facade remains large and schema-fragile.
10. `TEST-001` — No automated screenshot/window-size regression gate.

## 5. Top 10 technical-debt risks

1. `ARCH-001` — `PlayerViewModel` remains a broad app facade.
2. `ARCH-002` — Major UI files still mix layout, interaction, and data shaping.
3. `PROVIDER-001` — `YouTube.kt` and `InnerTube.kt` remain large provider hotspots.
4. `PERSIST-002` — File/preference persistence is improved but not a scalable local library model.
5. `ARCH-003` — QA runtime hooks remain in production source.
6. `PERF-001` — Some screens still derive broad list state inside composition.
7. `BUILD-001` — Version catalog contains unused dependency aliases.
8. `DOCS-001` — Historical docs can drift from current source.
9. `REPO-001` — Generated screenshots are untracked and policy-ambiguous.
10. `REPO-002` — Tracked `local.properties` increases future secret-commit risk.

## 6. Top 10 product gaps

1. Signed installer.
2. Clean upgrade/install identity.
3. Real updater/check-for-updates flow.
4. Clean VM/no system Java/no system VLC proof.
5. Physical offline installed-app playback proof.
6. Multi-hour playback soak.
7. Real notification system or removal of notification toggles.
8. Real audio enhancement pipeline or removal of enhancement toggles.
9. Accessibility validation with screen reader/high contrast/DPI.
10. Visual regression workflow for reference-heavy screens.

## 7. Top 10 high-value improvements

1. Import playlist covers into managed app data.
2. Remove/disable fake active Settings toggles.
3. Add stale playback-resolution regression test.
4. Add provider fixture corpus.
5. Build deterministic shuffle history.
6. Expand global responsive layout contract.
7. Label interactive icon-only controls.
8. Add installed-app smoke/offline release checklist.
9. Sign Windows installer.
10. Add screenshot regression artifacts or controlled visual QA automation.

## 8. Quick wins

- Restrict `openUri` helper to `http`/`https`.
- Move `local.properties` to `local.properties.example` and ignore real local config.
- Remove or document unused version catalog aliases.
- Archive/supersede old audit docs.
- Replace heuristic playlist mood chips with neutral metadata chips.
- Add explicit HTTP response-code handling in downloads.
- Add content descriptions for obvious icon-only actions.

## 9. High-risk changes to avoid

- Do not rewrite `PlayerViewModel` in one pass.
- Do not migrate all persistence to a database without migration tests.
- Do not change playback engine lifecycle without shutdown/playback tests.
- Do not implement fake social/account/cloud features in UI.
- Do not add broad animations or scaling hacks to mask responsive problems.
- Do not change installer identity without upgrade/uninstall testing.

## 10. Recommended next implementation phase

Recommended next phase: Phase 1 — Critical correctness and data safety.

Order:

1. Fix managed playlist cover import.
2. Remove/disable or truly implement fake active Settings controls.
3. Add installed-version verification and release checklist.
4. Begin signing/update strategy.

Reason:

These items directly affect user trust and product honesty. They are more important than adding more visual features.
