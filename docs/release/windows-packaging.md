# OmniTune Windows packaging

## Packaging architecture

OmniTune Windows keeps the Compose Desktop native distribution pipeline, backed by `jpackage`, for compatibility artifacts. The preferred public installer is the custom Inno Setup installer because it provides OmniTune branding and better same-version reinstall/update behavior.

Primary commands:

```powershell
.\gradlew.bat :composeApp:packageReleaseExe
.\gradlew.bat :composeApp:packageReleaseMsi
```

Release wrapper:

```powershell
.\scripts\release\build-windows-release.ps1
```

The wrapper runs desktop compile/assemble/tests, builds EXE and MSI installers, copies them into `build/release/windows`, generates SHA-256 checksum files, and writes `release-manifest.json`.

Compatibility artifact naming:

```text
OmniTune-Setup-<version>-windows-x64.exe
OmniTune-<version>-windows-x64.msi
```

## Version source

The canonical app/package version is:

```properties
omnitune.version=<current release version>
```

in `gradle.properties`. `composeApp/build.gradle.kts` reads this value for the native installer version.

## Java runtime

The Compose/jpackage pipeline creates a private runtime image under the installed application directory. Users should not need a system Java/JDK/JRE installation.

## VLC/libVLC

OmniTune uses vlcj/libVLC for desktop playback. The packaging task copies a private VLC runtime from:

1. `VLC_HOME`, when set; otherwise
2. `C:\Program Files\VideoLAN\VLC`

The packaged runtime is placed under:

```text
OmniTune\native\vlc\
```

At startup, `NativeRuntime.configureNativeAudioRuntime()` searches for packaged `native/vlc` first, then explicit developer/system fallbacks. Normal installed builds should use the packaged runtime and should not require system VLC.

The VLC files `COPYING.txt`, `AUTHORS.txt`, and `NEWS.txt` are copied with the bundled runtime. Release owners must review VLC redistribution obligations for the exact VLC build shipped.

## User data

Runtime user data is stored outside the installation directory:

```text
%LOCALAPPDATA%\OmniTuneData
```

This includes settings, search/history/playlist persistence, download metadata, downloads, cache, and logs. A legacy `~\.omnitune` directory is copied once if the new app-data directory does not already exist.

## Logs

Startup/native-runtime diagnostics are written to:

```text
%LOCALAPPDATA%\OmniTuneData\logs\startup.log
```

## Signing

No signing certificate is configured in the repository. Produced installers are unsigned by default and may trigger Windows SmartScreen reputation warnings.

The release wrapper has an opt-in signing path for release owners who have a real Windows code-signing certificate. Certificate files and passwords must never be committed.

```powershell
$env:OMNITUNE_SIGNTOOL = "C:\Program Files (x86)\Windows Kits\10\bin\x64\signtool.exe"
$env:OMNITUNE_SIGN_CERT_PATH = "C:\secure\OmniTune-CodeSigning.pfx"
$env:OMNITUNE_SIGN_CERT_PASSWORD = "<set from secret manager>"
$env:OMNITUNE_TIMESTAMP_URL = "http://timestamp.digicert.com"
.\scripts\release\build-windows-release.ps1 -Sign
```

If `-Sign` is omitted, the script builds unsigned installers and records `"signed": false` in `release-manifest.json`.

## Release packaging policy

The compatibility Windows installer path is the Compose Desktop release native-distribution pipeline:

```powershell
.\gradlew.bat :composeApp:packageReleaseExe
.\gradlew.bat :composeApp:packageReleaseMsi
```

Release ProGuard minification is intentionally disabled in `composeApp/build.gradle.kts`. Earlier release packaging failed because desktop transitive dependencies expose optional Android, JSSE, JNA, and native-provider references that ProGuard attempted to resolve even though those paths are not part of the Windows desktop runtime. The release build remains a normal jpackage native distribution with a private runtime and bundled VLC files.

The VLC `plugins/plugins.dat` cache file is excluded from packaged output so VLC can build/use plugin discovery appropriate to the installed path instead of a stale cache generated on the build machine.

The jpackage Windows installer is configured as a GUI application with:

- no console window,
- per-user install preference,
- Start menu shortcut,
- desktop shortcut,
- directory chooser,
- OmniTune icon.

## Custom branded installer path

OmniTune also has an Inno Setup installer path for richer Windows installer behavior:

```powershell
.\scripts\release\build-inno-installer.ps1
```

This path uses:

```text
installer/inno/OmniTune.iss
```

It provides:

- OmniTune setup icon;
- generated OmniTune wizard branding artwork;
- Start menu shortcut;
- optional desktop shortcut;
- same-version reinstall/overwrite behavior through a stable Inno `AppId`;
- install location under `%LOCALAPPDATA%\OmniTune`;
- user data preserved under `%LOCALAPPDATA%\OmniTuneData`.

Prepare-only validation, without Inno Setup:

```powershell
.\scripts\release\build-inno-installer.ps1 -SkipTests -PrepareOnly
```

Required local tool:

```powershell
winget install --id JRSoftware.InnoSetup -e
```

After installing Inno Setup 6, build:

```powershell
.\scripts\release\build-inno-installer.ps1 -SkipTests
```

If `ISCC.exe` is not on `PATH`, pass it explicitly:

```powershell
.\scripts\release\build-inno-installer.ps1 -SkipTests -InnoCompiler "C:\Program Files (x86)\Inno Setup 6\ISCC.exe"
```

The custom installer output is:

```text
build/release/windows/inno/OmniTune-Setup-<version>-windows-x64-custom.exe
```

Important:

- This is the preferred public release asset when available.
- The custom installer solves the stock jpackage same-version reinstall friction.
- It does not delete `%LOCALAPPDATA%\OmniTuneData`.
- It still needs Authenticode signing for public trust.

## In-app update behavior

Settings > About > Check for Updates queries the GitHub releases API endpoint:

```text
https://api.github.com/repos/soupashh-ship-it/OmniTune-Windows/releases
```

This endpoint includes prerelease/beta releases. When an update is available, OmniTune chooses the preferred Windows installer asset in this order:

1. `*-custom.exe`
2. setup `.exe`
3. any `.exe`
4. `.msi`

The app downloads the selected asset to:

```text
%LOCALAPPDATA%\OmniTuneData\updates
```

If the release also provides a matching `<installer>.sha256` asset, OmniTune verifies the downloaded installer before execution. Verified custom Inno installers are launched with:

```text
/VERYSILENT /SUPPRESSMSGBOXES /NORESTART /CLOSEAPPLICATIONS /RESTARTAPPLICATIONS
```

Verified MSI installers are launched through:

```text
msiexec.exe /i <installer> /qn /norestart
```

After starting a verified silent update, OmniTune exits so the installer can replace application files cleanly. If checksum verification is unavailable or silent installation is unsupported for the selected asset, OmniTune falls back to opening the installer manually.

This is a verified silent installer handoff from the in-app update check. It is not yet a service-style background updater that polls and installs without user action.

## MSIX path

MSIX packaging is available for future Store/sideload evaluation:

```powershell
.\scripts\release\build-msix-package.ps1
```

See:

```text
docs/release/msix-packaging.md
```

The branded Inno Setup installer remains the preferred public release asset until the MSIX identity, signing, and Store strategy are finalized.

## Signing preflight

Before building a signed public release, run:

```powershell
.\scripts\release\test-signing-prerequisites.ps1
```

Required environment:

```powershell
$env:OMNITUNE_SIGNTOOL = "C:\Program Files (x86)\Windows Kits\10\bin\<sdk-version>\x64\signtool.exe"
$env:OMNITUNE_SIGN_CERT_PATH = "C:\secure\OmniTune-CodeSigning.pfx"
$env:OMNITUNE_SIGN_CERT_PASSWORD = "<from secret manager>"
$env:OMNITUNE_TIMESTAMP_URL = "http://timestamp.digicert.com"
```

This verifies that `signtool.exe` exists, the certificate file is readable, the certificate contains a private key, and it has not expired.

## User-data preservation verification

Before install/update:

```powershell
.\scripts\verify-install-data-preservation.ps1 -Mode Before
```

Install/update:

```powershell
.\scripts\install-omnitune-update.ps1 -InstallerPath .\build\release\windows\inno\OmniTune-Setup-<version>-windows-x64-custom.exe -ExpectedVersion <version>
```

After install/update:

```powershell
.\scripts\verify-install-data-preservation.ps1 -Mode After
```

Use `-HashDownloads` when you want downloaded media files hashed too.

## Tooling still needed for signed public releases

Install Inno Setup:

```powershell
winget install --id JRSoftware.InnoSetup -e
```

Install Windows SDK signing tools:

```powershell
winget install --id Microsoft.WindowsSDK.10.0.26100 -e
```

Then set signing environment variables:

```powershell
$env:OMNITUNE_SIGNTOOL = "C:\Program Files (x86)\Windows Kits\10\bin\<sdk-version>\x64\signtool.exe"
$env:OMNITUNE_SIGN_CERT_PATH = "C:\secure\OmniTune-CodeSigning.pfx"
$env:OMNITUNE_SIGN_CERT_PASSWORD = "<from secret manager>"
$env:OMNITUNE_TIMESTAMP_URL = "http://timestamp.digicert.com"
```

Build signed custom installer:

```powershell
.\scripts\release\build-inno-installer.ps1 -Sign
```

The repository cannot include a real certificate or password.

## Clean-install QA

Required manual QA before public release:

1. Copy only the installer to a clean Windows machine.
2. Install OmniTune.
3. Launch from Start menu.
4. Verify no system Java is required.
5. Verify playback with no system VLC installed.
6. Search, play, seek, pause/resume.
7. Download a track, restart, and play it locally.
8. Save queue as playlist and verify persistence.
9. Switch theme and verify persistence.
10. Uninstall and verify binaries/shortcuts are removed.

## Upgrade confusion guard

Older development builds used different visible names and may coexist on a developer machine. When validating a public package, check Windows Apps & Features, Start menu shortcuts, and the Settings > About version inside the launched app. Users should see the current `omnitune.version`; otherwise they are still launching an older install.

The helper below inventories installed OmniTune entries and shortcuts:

```powershell
.\scripts\release\diagnose-installed-omnitune.ps1
```

Use `-Json` for machine-readable output during QA.
