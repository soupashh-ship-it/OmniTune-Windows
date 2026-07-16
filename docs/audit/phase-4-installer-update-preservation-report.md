# Phase 4 Installer Branding, Update, and Preservation Report

Project: `D:\Omnitune Windoww`

Date: 2026-07-16

## Scope

This phase addressed installer-specific questions:

1. Installer/app branding and icon metadata.
2. Same-version install/update behavior.
3. Preservation of important user data during install/update.
4. Verification that the current installer build path still works.

## Branding

Current jpackage configuration uses:

- package name: `OmniTune`
- vendor: `OmniTune`
- menu group: `OmniTune`
- shortcut/menu creation: enabled
- app icon: `composeApp/src/desktopMain/resources/icon.ico`

The release manifest now records branding metadata and verifies that the configured icon file exists.

Limitation:

- The stock Compose Desktop/jpackage Windows installer UI does not provide a rich branded installer wizard surface comparable to a custom Inno Setup/NSIS/WiX-authored installer.
- App icon, shortcut icon, uninstall metadata, vendor, menu group, and package metadata are supported and configured.
- A fully custom branded installer UI should be a future installer-technology decision, not a fragile jpackage hack.

## Same-version install/update

Windows Installer can reject same-version reinstalls, especially with MSI status `1638`. This is normal Windows Installer behavior and is not Android-style replacement behavior.

Added:

- `scripts/install-omnitune-update.ps1`

Behavior:

- detects existing `OmniTune` install entries;
- uninstalls the existing app binary package first unless `-SkipUninstall` is supplied;
- runs the supplied EXE/MSI installer;
- optionally runs installed-version verification after install;
- never deletes `%LOCALAPPDATA%\OmniTuneData`.

Example:

```powershell
.\scripts\install-omnitune-update.ps1 `
  -InstallerPath .\build\release\windows\OmniTune-Setup-0.2.0-windows-x64.exe `
  -ExpectedVersion 0.2.0
```

For MSI passive install:

```powershell
.\scripts\install-omnitune-update.ps1 `
  -InstallerPath .\build\release\windows\OmniTune-0.2.0-windows-x64.msi `
  -ExpectedVersion 0.2.0 `
  -Passive
```

## User-data preservation

Runtime user data remains outside the install directory:

```text
%LOCALAPPDATA%\OmniTuneData
```

This includes settings, playlists, liked-song records, download metadata, playlist covers, and downloaded media.

Added:

- `scripts/verify-install-data-preservation.ps1`

Usage:

```powershell
.\scripts\verify-install-data-preservation.ps1 -Mode Before
.\scripts\install-omnitune-update.ps1 -InstallerPath .\build\release\windows\OmniTune-Setup-0.2.0-windows-x64.exe -ExpectedVersion 0.2.0
.\scripts\verify-install-data-preservation.ps1 -Mode After
```

The verifier:

- creates a preservation marker;
- snapshots user-data files;
- ignores volatile logs/cache/native runtime extraction files;
- verifies the marker and snapshotted files still exist after install/update;
- can hash downloads when explicitly passed `-HashDownloads`.

Script syntax was validated using a temporary `LOCALAPPDATA` directory.

## Installer build verification

Command:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\release\build-windows-release.ps1 -SkipTests
```

Result:

- PASS

Artifacts produced:

- `build/release/windows/OmniTune-Setup-0.2.0-windows-x64.exe`
- `build/release/windows/OmniTune-0.2.0-windows-x64.msi`
- `build/release/windows/SHA256SUMS.txt`
- `build/release/windows/release-manifest.json`

Manifest confirms:

- Java runtime bundled: `true`
- Native VLC runtime bundled: `true`
- icon file exists: `true`
- upgrade UUID recorded
- user-data preservation policy recorded

SHA-256:

- EXE: `675388eca05c9eaea4b72ca86462399b220569fa256cb4caf8c6654e97a2817a`
- MSI: `7b5558c94a6e5e25c09ed9feb788da5fa41ca836448613c761af8ed7dbece2f0`

Signature verification:

- EXE: `NotSigned`
- MSI: `NotSigned`

This is expected until a real code-signing certificate is configured.

## Final validation

Command:

```powershell
.\gradlew.bat --quiet :composeApp:compileKotlinDesktop :composeApp:assemble test :composeApp:desktopTest
```

Result:

- PASS

## Remaining installer limitations

1. Fully custom branded installer wizard UI requires moving beyond stock jpackage behavior or adding a dedicated installer authoring layer.
2. Same-version install cannot be made Android-like inside stock Windows Installer semantics without a wrapper/uninstall-reinstall flow or custom bootstrapper.
3. Public trust still requires signing the EXE/MSI with a real certificate.
4. Clean VM install and offline installed playback proof remain manual environment validations.

