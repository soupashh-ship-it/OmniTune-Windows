# Release Process

OmniTune Windows is not yet a 1.0 release. Use this process for future tagged releases after release criteria are met.

## 1. Update Version

Update the Compose Desktop package version in:

```text
gradle.properties
```

Current field:

```properties
omnitune.version=0.1.0
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
.\gradlew.bat :composeApp:packageDistributionForCurrentOS
```

Expected output location:

```text
composeApp/build/compose/binaries/
```

The Gradle configuration currently declares MSI and EXE targets.

Packaging requires a VLC/libVLC runtime. If VLC is not installed at `C:\Program Files\VideoLAN\VLC`, set `VLC_HOME` before packaging:

```powershell
$env:VLC_HOME = "C:\Path\To\VLC"
```

The currently validated packaging command is the non-minified `:composeApp:packageDistributionForCurrentOS` task. The `packageRelease*` tasks run ProGuard and currently fail until dependency-specific rules are added for unresolved optional references.

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
Get-ChildItem composeApp\build\compose\binaries -Recurse -File |
  Where-Object { $_.Extension -in ".exe", ".msi", ".zip" } |
  ForEach-Object {
    $hash = Get-FileHash -Algorithm SHA256 -LiteralPath $_.FullName
    "$($hash.Hash)  $($_.Name)"
  } | Set-Content SHA256SUMS.txt
```

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
