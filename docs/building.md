# Building

## Prerequisites

- Windows 10 or Windows 11, x64
- JDK 21
- Git
- VLC 3.x installed locally for playback/runtime QA, or `VLC_HOME` pointing at a redistributable VLC/libVLC runtime

The Gradle build declares `jvmToolchain(21)`. The wrapper currently uses Gradle `8.12`.

## Verify Tooling

```powershell
.\gradlew.bat --version
```

Expected project-level versions:

- Gradle wrapper: `8.12`
- Kotlin plugin: `2.1.20`
- Compose Multiplatform plugin: `1.8.0`
- JDK toolchain: `21`

## Discover Tasks

```powershell
.\gradlew.bat tasks --all
```

Important desktop tasks include:

- `:composeApp:desktopTest`
- `:composeApp:run`
- `:composeApp:desktopJar`
- `:composeApp:createDistributable`
- `:composeApp:packageDistributionForCurrentOS`
- `:composeApp:packageExe`
- `:composeApp:packageMsi`
- `:composeApp:packageReleaseDistributionForCurrentOS`
- `:composeApp:packageReleaseExe`
- `:composeApp:packageReleaseMsi`

## Compile and Test

Stable local verification:

```powershell
.\gradlew.bat :composeApp:desktopTest
.\gradlew.bat build -x :innertube:test
```

The `:innertube:test` task performs a live provider search. Run it manually when you explicitly want network/provider coverage:

```powershell
.\gradlew.bat :innertube:test
```

## Run the App

```powershell
.\gradlew.bat :composeApp:run
```

Playback requires VLC/libVLC discovery. The current native runtime helper checks:

- packaged `native/vlc`
- `VLC_HOME`
- `C:\Program Files\VideoLAN\VLC`

## Package for Windows

```powershell
.\scripts\release\build-windows-release.ps1
```

The release wrapper runs desktop compile/assemble/tests, builds release EXE/MSI installers, generates checksums, and writes a release manifest. Final public artifacts are copied to:

```text
build/release/windows/
```

Do not commit generated installers. Attach release artifacts to GitHub Releases.

Release packaging copies `THIRD_PARTY_NOTICES.txt` and VLC runtime files into the app image when VLC is available. Set `VLC_HOME` if VLC is not installed at the default Windows path.

The validated release tasks are:

```powershell
.\gradlew.bat :composeApp:packageReleaseExe
.\gradlew.bat :composeApp:packageReleaseMsi
```

Release ProGuard minification is intentionally disabled for the desktop package because optional transitive dependencies expose unresolved non-runtime references.
