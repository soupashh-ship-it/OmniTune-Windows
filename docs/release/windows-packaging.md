# OmniTune Windows packaging

## Packaging architecture

OmniTune Windows uses the Compose Desktop native distribution pipeline, backed by `jpackage`, to create normal Windows installers.

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

Current public artifact naming:

```text
OmniTune-Setup-<version>-windows-x64.exe
OmniTune-<version>-windows-x64.msi
```

## Version source

The canonical app/package version is:

```properties
omnitune.version=0.1.0
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
%LOCALAPPDATA%\OmniTune
```

This includes settings, search/history/playlist persistence, download metadata, downloads, cache, and logs. A legacy `~\.omnitune` directory is copied once if the new app-data directory does not already exist.

## Logs

Startup/native-runtime diagnostics are written to:

```text
%LOCALAPPDATA%\OmniTune\logs\startup.log
```

## Signing

No signing certificate is configured in the repository. Produced installers are unsigned unless an external signing step is added by the release owner. Unsigned installers may trigger Windows SmartScreen reputation warnings.

## Release packaging policy

The production Windows installer path is the Compose Desktop release native-distribution pipeline:

```powershell
.\gradlew.bat :composeApp:packageReleaseExe
.\gradlew.bat :composeApp:packageReleaseMsi
```

Release ProGuard minification is intentionally disabled in `composeApp/build.gradle.kts`. Earlier release packaging failed because desktop transitive dependencies expose optional Android, JSSE, JNA, and native-provider references that ProGuard attempted to resolve even though those paths are not part of the Windows desktop runtime. The release build remains a normal jpackage native distribution with a private runtime and bundled VLC files.

The VLC `plugins/plugins.dat` cache file is excluded from packaged output so VLC can build/use plugin discovery appropriate to the installed path instead of a stale cache generated on the build machine.

The Windows installer is configured as a GUI application with:

- no console window,
- per-user install preference,
- Start menu shortcut,
- desktop shortcut,
- directory chooser,
- OmniTune icon.

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
