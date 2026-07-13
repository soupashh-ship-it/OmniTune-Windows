# Troubleshooting

## VLC or libVLC Is Not Found

OmniTune Windows uses vlcj, which requires libVLC. The app checks:

- packaged `native/vlc`
- `VLC_HOME`
- `C:\Program Files\VideoLAN\VLC`

For local development, install VLC 3.x for Windows and verify that VLC exists at:

```text
C:\Program Files\VideoLAN\VLC
```

If VLC is installed somewhere else, set `VLC_HOME` to the directory containing `libvlc.dll`, `libvlccore.dll`, and `plugins/`.

## Playback Starts but Audio Does Not Play

Check:

- VLC is installed and matches the machine architecture.
- Windows has an available audio output device.
- The stream is available in your region.
- The track can be resolved by the provider.
- Volume is not muted in OmniTune, VLC/libVLC, or Windows.

## Provider Search or Streaming Fails

Search, streaming, discovery, lyrics, and downloads are provider-backed and require network access. Provider behavior can change outside this repository. Retry with another query and check whether `:innertube:test` still passes locally.

```powershell
.\gradlew.bat :innertube:test
```

## Unsigned Installer Warning

Local MSI/EXE builds are unsigned. Windows SmartScreen may warn on development packages. This is expected until release signing is configured.

## Packaging Fails

Run:

```powershell
.\gradlew.bat --version
.\scripts\release\build-windows-release.ps1
```

Packaging may need WiX-related tooling downloaded by Gradle/Compose Desktop. Make sure the machine has network access and a supported JDK 21 installation.

If release packaging fails, rerun the specific failing Gradle task from the release-script output with `--stacktrace`. The validated release path uses `:composeApp:packageReleaseExe` and `:composeApp:packageReleaseMsi`; release ProGuard minification is intentionally disabled for the desktop package.

## Stale Build Cache

If compilation behaves inconsistently, use Gradle's normal clean task:

```powershell
.\gradlew.bat clean
.\gradlew.bat :composeApp:desktopTest
```

Do not delete source, QA evidence, or local Git history while troubleshooting.

## Offline Playback

Completed downloads are played through verified local files when available. If offline playback does not work:

- Confirm the download task is completed.
- Confirm the local file exists and is non-empty.
- Confirm VLC can play local media files.
- Retry the download if the task is failed, cancelled, or paused.
