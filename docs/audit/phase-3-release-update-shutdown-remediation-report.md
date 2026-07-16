# Phase 3 Release, Update, and Shutdown Remediation Report

Project: `D:\Omnitune Windoww`

Date: 2026-07-16

## Scope

This phase continued the post-audit remediation items that were still code-actionable:

1. Legacy `OmniTuneWindows` install cleanup handling.
2. Real Settings/About update detection instead of a browser-only shortcut.
3. Bounded VLC shutdown/release behavior.

External items that cannot be completed from source alone remain documented:

- production code signing requires a real certificate;
- clean Windows VM proof requires a clean VM;
- physical offline installed-app proof requires installed-app environment testing;
- long playback soak requires elapsed runtime.

## Changes

### Legacy install cleanup

Added:

- `scripts/remove-legacy-omnitune-install.ps1`

Behavior:

- dry-run by default;
- finds legacy `OmniTuneWindows` uninstall entries;
- prints registered uninstall commands;
- only launches uninstallers when explicitly passed `-Execute`.

Observed local dry-run result:

- detected `OmniTuneWindows 1.0.0`;
- uninstall command: `MsiExec.exe /X{A7702B81-5121-33F8-9C8D-5914E940ACED}`;
- no uninstall was performed during the dry run.

### Update check

Added:

- `ReleaseUpdateChecker`
- `UpdateCheckResult`
- generated `latestReleaseApiUrl` in `omnitune-version.properties`

Changed Settings/About:

- `Check for Updates` now performs a GitHub latest-release metadata check;
- compares the running app version against the latest release tag;
- reports current/update/failure status in the Settings page;
- opens the release page only when a newer release is found.

Tests added:

- release version comparison;
- GitHub release metadata parsing;
- update/current result selection.

### VLC release/shutdown

Changed:

- `VlcjAudioEngine.release()` no longer uses unbounded `runBlocking`.
- Normal disposal schedules release asynchronously on the VLC dispatcher.
- Explicit Quit uses `releaseBlocking(timeoutMs = 3000)` so shutdown can wait briefly without hanging indefinitely.
- VLC commands are ignored once release is requested.
- Release completion is idempotent.

Tests added:

- release coordinator permits only one release start;
- release coordinator completion is idempotent.

## Validation

Targeted validation:

```powershell
.\gradlew.bat :composeApp:compileKotlinDesktop :composeApp:desktopTest --tests "com.omnitune.app.platform.*"
```

Result:

- PASS

Dry-run cleanup validation:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\remove-legacy-omnitune-install.ps1
```

Result:

- detected the legacy install entry;
- did not uninstall because `-Execute` was not supplied.

## Remaining external blockers

1. Code signing still requires a certificate and signing identity.
2. Legacy uninstall requires deliberate user/admin execution of the cleanup script or Windows Apps removal.
3. Clean VM proof is still pending.
4. Physical offline installed-app proof is still pending.
5. Multi-hour playback soak is still pending.

