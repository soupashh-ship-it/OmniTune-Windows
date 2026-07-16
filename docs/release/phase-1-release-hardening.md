# OmniTune Windows Phase 1 Release Hardening

Status: started  
Date: 2026-07-16

This document records the release-engineering fixes started after the full verification pass. It does not publish, tag, or change the current release.

## Installed-version verification

Use:

```powershell
.\scripts\verify-installed-omnitune.ps1 -ExpectedVersion 0.2.5
```

For release sign-off:

```powershell
.\scripts\verify-installed-omnitune.ps1 -ExpectedVersion 0.2.5 -FailOnMismatch
```

The script checks:

- current-user uninstall entries;
- machine-wide uninstall entries;
- legacy `OmniTuneWindows` install entries;
- Desktop and Start Menu shortcuts;
- install versions against the expected release.

Release blocker:

- Any legacy install entry or shortcut that launches an older OmniTune build must be resolved before announcing a release.

Legacy cleanup helper:

```powershell
.\scripts\remove-legacy-omnitune-install.ps1
```

The cleanup helper is dry-run by default. It only launches legacy uninstallers when explicitly run with:

```powershell
.\scripts\remove-legacy-omnitune-install.ps1 -Execute
```

## Installer signature verification

Use:

```powershell
.\scripts\verify-installer-signature.ps1 -Path .\build\release\windows\OmniTune-Setup-0.2.5-windows-x64.exe
```

For signed-release sign-off:

```powershell
.\scripts\verify-installer-signature.ps1 -Path .\build\release\windows\OmniTune-Setup-0.2.5-windows-x64.exe -RequireValid
```

Current limitation:

- A signing certificate is not stored in the repository.
- Release builds remain unsigned until a real certificate and signing step are added.

## Signing strategy

Required next implementation:

1. Choose a Windows code-signing certificate provider.
2. Store signing credentials outside the repository.
3. Sign EXE and MSI artifacts after packaging.
4. Verify Authenticode status with `verify-installer-signature.ps1`.
5. Publish SHA256 checksums with every release.
6. Keep unsigned local development builds clearly separate from public release artifacts.

Recommended CI/release behavior:

- Build package.
- Sign package.
- Verify signature.
- Generate SHA256SUMS.
- Run clean installed-app smoke.
- Run installed-version verification.
- Only then create or update GitHub release assets.

## Update strategy

The Settings/About `Check for Updates` action now performs a real latest-release metadata check against GitHub Releases and compares it with `AppInfo.version`.

Current behavior:

1. Query latest GitHub release metadata.
2. Compare latest tag with the running app version.
3. Report whether OmniTune is current.
4. If a newer release exists, open the release page intentionally.
5. If the check fails, show an update-check failure message.

This is an update checker, not an automatic updater.

Recommended native update path:

1. Move from GitHub latest release API to a signed update manifest.
2. Support release channels explicitly: stable, beta, prerelease.
3. Show current version, latest version, channel, and release notes.
4. Let the user download the installer intentionally.
5. Verify signature before offering install.
6. Never execute an unsigned installer automatically.

## Product-honesty changes in this phase

Unsupported Settings controls should not look active.

Changed direction:

- audio enhancement preferences are shown as unavailable instead of active toggles;
- Windows notification preferences are shown as unavailable instead of active toggles;
- automatic playlist download is shown as unavailable instead of an active toggle;
- real controls remain interactive: download quality, volume, shuffle default, repeat, reduced motion, mini-player always-on-top, and focused shortcuts.

## Playlist cover persistence direction

Playlist cover selection now imports the chosen image into managed app data before saving metadata.

Managed location:

```text
%LOCALAPPDATA%\OmniTuneData\playlist-covers
```

This prevents playlist artwork from depending on arbitrary external files that users may later move or delete.
