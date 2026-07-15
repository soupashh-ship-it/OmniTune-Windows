# OmniTune Windows final development report

Date: 2026-07-15

Project: `D:\Omnitune Windoww`

Active development is frozen. This report records the final closure state, not a future roadmap.

## Final implemented capabilities

- Full desktop shell with Home, Browse, Radio, Search, Library, Playlist Detail, Artist, Album, Now Playing, Lyrics, Related, Queue, Downloads, Settings, Mini Player, and Bottom Player.
- Provider-backed search, category results, search history, stale-request cancellation, and representative provider smoke coverage.
- Playback through VLCJ with bounded recovery, seek clamping, queue synchronization, local-first playback, history, sessions, and meaningful-listening tracking.
- Downloads with pause/resume/retry/delete, restoration, media-write failure handling, permission/no-space handling, and invalid completed-file protection.
- Playlist persistence and Queue Save as Playlist.
- Radio session identity, dedupe, recent-repeat suppression, continuation, stale-session protection, and queue bounds.
- Related loading/failure/retry, stale-track protection, dedupe, current-track exclusion, and result cap.
- Windows media-session/SMTC implementation through the existing JMTC dependency.
- Keyboard shortcut routing, repeat suppression, editable-text safety, focus traversal model, and Compose Desktop runtime UI tests.
- Four themes, reduced motion foundation, canonical visual-reference mapping, and stable-geometry visual diff tooling.
- EXE/MSI packaging with private Java runtime, embedded VLC fallback, and `%LOCALAPPDATA%\OmniTuneData` data separation.

## Build status

| Gate | Result |
|---|---|
| `.\gradlew.bat :composeApp:compileKotlinDesktop` | PASS |
| `.\gradlew.bat :composeApp:assemble` | PASS |
| `.\gradlew.bat test` | PASS |
| `.\gradlew.bat :composeApp:desktopTest` | PASS |
| Targeted Compose runtime UI tests | PASS |
| Provider smoke QA | PASS, 6/6 representative queries non-empty |
| Local playback QA | PASS, local file play/seek/pause/resume |

## Installer status

Built with the existing wrapper:

`.\scripts\release\build-windows-release.ps1 -SkipTests`

Artifacts:

- `build/release/windows/OmniTune-Setup-0.1.4-windows-x64.exe`
- `build/release/windows/OmniTune-0.1.4-windows-x64.msi`
- `build/release/windows/SHA256SUMS.txt`
- `build/release/windows/release-manifest.json`

Manifest result:

- private Java runtime bundled: true
- embedded VLC/native audio runtime bundled: true
- signed: false
- app data path: `%LOCALAPPDATA%\OmniTuneData`

No GitHub release, tag, or upload was performed in this closure pass.

## Current-machine smoke status

- Packaged app-image launch: PASS
- App stayed alive after 12 seconds: PASS
- Packaged VLC selected from app image: PASS
- App data path observed: `%LOCALAPPDATA%\OmniTuneData`
- Silent installer install/upgrade: NOT PERFORMED in this pass to avoid mutating the current machine install state during closure.
- Clean VM validation: deferred by user.
- Physical no-network installed-app offline proof: deferred by user.
- Multi-hour playback soak: deferred by user.

## Known limitations

See `docs/known-limitations.md`.

Key non-blocking limitations:

1. Small Now Playing stable visual residual remains.
2. Full PlayerViewModel-backed production-route Compose runtime focus coverage is not exhaustive.
3. Physical Windows SMTC shell/flyout validation is not claimed.
4. Provider behavior can change externally.
5. Browse/Radio/Related live provider failure coverage is not exhaustive.

## Deferred by user

1. Clean Windows VM / no Java / no VLC validation.
2. Physical no-network installed-app offline proof.
3. Multi-hour playback soak.

## External blockers

1. No legitimate Windows code-signing certificate.
2. External screen-reader runtime validation unavailable/not performed.

## Final source commit

Final commit: recorded by Git for the commit containing this report; see the final handoff and `git log --oneline -1`.

Branch: `main`

## Final verdict

Development frozen. No release-blocking local defect is known from the final validation gates.
