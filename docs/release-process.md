# Release Process

OmniTune Windows is not yet a 1.0 release. Use this process for future tagged releases after release criteria are met.

## 1. Update Version

Update the Compose Desktop package version in:

```text
gradle.properties
```

Current field:

```properties
omnitune.version=0.1.2
```

## 2. Verify Source

```powershell
git status
.\gradlew.bat --version
.\gradlew.bat :composeApp:desktopTest
.\gradlew.bat build -x :innertube:test
```

Run the provider-backed test manually when appropriate:

```powershell
.\gradlew.bat :innertube:test
```

## 3. Build Windows Packages

```powershell
.\scripts\release\build-windows-release.ps1
```

Expected output location:

```text
build/release/windows/
```

The release wrapper runs compile, assemble, root tests, desktop tests, release EXE/MSI packaging, checksum generation, and release manifest generation. The Gradle configuration declares MSI and EXE targets.

Packaging requires a VLC/libVLC runtime. If VLC is not installed at `C:\Program Files\VideoLAN\VLC`, set `VLC_HOME` before packaging:

```powershell
$env:VLC_HOME = "C:\Path\To\VLC"
```

The validated release package tasks are `:composeApp:packageReleaseExe` and `:composeApp:packageReleaseMsi`. Release ProGuard minification is intentionally disabled for the desktop package because optional transitive dependencies expose unresolved non-runtime references.

## 4. Validate Artifacts

Before uploading:

- Install the MSI/EXE on a clean Windows machine.
- Confirm VLC/libVLC requirements are documented or bundled.
- Launch the app.
- Verify search, playback, queue, downloads, settings, mini player, and exit behavior.
- Check Windows Defender/SmartScreen behavior.
- Confirm generated artifacts are not committed to Git.

## 5. Generate Checksums

```powershell
.\scripts\release\build-windows-release.ps1
```

The release wrapper writes `.sha256` files plus `SHA256SUMS.txt` under `build/release/windows/`.

## 6. Tag

Use a version tag only after the release commit is ready:

```powershell
git tag vX.Y.Z
git push origin vX.Y.Z
```

The release workflow is tag-triggered and does not create fake version tags.

## 7. GitHub Release

Attach:

- MSI/EXE artifacts
- checksums
- release notes
- known limitations

Do not attach secrets, signing certificates, keystores, local logs, or generated cache directories.
