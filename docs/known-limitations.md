# OmniTune Windows known limitations

Date: 2026-07-16

This document classifies remaining known items at development freeze. These are not release blockers unless new evidence shows they break a primary user flow.

## Non-blocking known limitations

1. Now Playing retains a small known stable visual residual, including the previously measured transport-height difference.
2. Full PlayerViewModel-backed production-route Compose runtime focus coverage is not exhaustive.
3. Broader four-theme focused-state screenshot coverage is not exhaustive.
4. Physical Windows SMTC shell/flyout validation is incomplete unless separately performed on the target machine.
5. Provider behavior can change externally; representative provider QA is a smoke signal, not a permanent guarantee.
6. Browse, Radio, and Related live failure coverage is not exhaustive beyond implemented retry/stale-safety/bounds.
7. Some external artist/album facts are unavailable from reliable provider data and are not fabricated.
8. The app is unsigned without a legitimate Authenticode certificate, so Windows SmartScreen warnings may occur.
9. Core user collections now have a SQLite-backed local database path with JSON backup/migration for playlists, liked songs, playback history, sessions, and download metadata. Some lightweight preferences such as recent searches and UI settings intentionally remain in Preferences/JSON.
10. Windows notification delivery is implemented through local desktop tray notifications where the OS/session supports `SystemTray`. Unsupported content-notification categories remain hidden/unavailable rather than fake.
11. MSIX packaging exists for Store/sideload evaluation, but public MSIX installation still requires signing and final Store identity decisions.

## Deferred by user

1. Clean Windows VM / no Java / no VLC validation.
2. Physical no-network installed-app offline proof.
3. Multi-hour playback soak.

## External blockers

1. Legitimate Windows code-signing certificate unavailable.
2. External screen-reader runtime validation unavailable/not performed.

## Explicitly not claimed

- Pixel-perfect 1:1 visual match.
- Permanent provider reliability.
- Physical hardware media-key proof unless separately tested.
- Clean-machine installer proof.
- Physical offline installed-app proof.
- Multi-hour playback soak stability.
- Signed Windows release.
- Service-style background updater that polls and installs without user action.
- Hosted telemetry/analytics crash ingestion.

## Diagnostics and crash reporting

OmniTune now installs a local uncaught-exception handler and can export a diagnostics ZIP from Settings > About.

The diagnostics export is opt-in and local. It includes:

- basic app/runtime information;
- the latest local crash report, if present;
- `omnitune.log`, if present.

It does not silently upload anything and does not include downloaded songs, playlist files, liked-song data, search history, or settings files.

The issue-report flow can open a prefilled GitHub issue. This is not silent hosted crash analytics. If public crash analytics are added later, they must be explicit and opt-in.

## Visual regression status

OmniTune now has a repeatable visual-regression capture/diff runner:

```powershell
.\docs\qa\visual_regression.ps1
```

The runner captures agreed routes and sizes, compares them to curated baselines when present, and writes a JSON report. It is not yet a mandatory CI gate because approved baselines still need to be curated.
