# OmniTune Windows - GitHub Publication Baseline

Date: 2026-07-13

Local project directory: `D:\Omnitune Windoww`

## Git State

- Git initialized: yes
- Current branch before publication work: `ui/nocturne-prism-atmosphere-correction`
- Current HEAD before publication work: `da5123f fix(ui): exact 607x41 player control band - full-width timeline, dominant play button, correct transport hierarchy`
- Existing remotes: none
- Existing local branches: `master` plus multiple `ui/nocturne-prism-*` branches
- Worktree state: dirty before publication work
  - Modified desktop source and Gradle files were present.
  - Untracked desktop tests, download manager source, QA screenshots, QA reports, and scripts were present.
  - Ignored build/cache files were present under `.gradle/`, `.kotlin/`, module `build/` folders, and `local.properties`.

## Remote State

- Target repository checked: `soupashh-ship-it/OmniTune-Windows`
- Result before publication work: repository did not exist.
- `origin` before publication work: none.
- GitHub CLI: installed.
- Authenticated GitHub account: `soupashh-ship-it`.

## Project Structure Summary

The repository is a Gradle multi-project Kotlin desktop application:

- `composeApp`: Compose Multiplatform desktop application and Windows entry point.
- `innertube`: YouTube Music/InnerTube client and models.
- `kugou`, `lrclib`, `simpmusic`, `betterlyrics`: lyrics/search support modules.
- `lastfm`: Last.fm API support module.
- `kizzy`: Discord presence support code.
- `canvas`: canvas/visual companion module.
- `docs/qa`: existing QA evidence, reference screenshots, scripts, and visual audit notes.
- `docs/ui/nocturne-prism`: existing UI reconstruction documentation.

## Detected Technology Stack

- Kotlin: `2.1.20`
- Compose Multiplatform Gradle plugin: `1.8.0`
- Gradle wrapper: `8.12`
- JDK requirement from Gradle build: `jvmToolchain(21)`
- Launcher JVM observed locally: Eclipse Temurin 21.0.11
- Desktop entry point: `com.omnitune.app.MainKt`
- DI: Koin 4.1
- Networking: Ktor 3.1
- Image loading: Coil 3
- Audio engine: `VlcjAudioEngine` using vlcj 4.11/libVLC
- Persistence: Java Preferences plus file-backed download index under the OmniTune app-data directory

## Application and Packaging

- Compose desktop package name: `OmniTune`
- Compose desktop package version: `0.11.6`
- Package description in Gradle: `Open-source YouTube music player for Windows`
- Configured native package formats: MSI and EXE
- Windows icon: `composeApp/src/desktopMain/resources/icon.ico`
- VLC/libVLC path currently configured in `Main.kt` for standard Windows VLC installation paths:
  - `C:\Program Files\VideoLAN\VLC`
  - `C:\Program Files\VideoLAN\VLC\plugins`

## Gradle Modules

Detected modules:

- `:composeApp`
- `:innertube`
- `:kugou`
- `:lrclib`
- `:lastfm`
- `:simpmusic`
- `:betterlyrics`
- `:kizzy`
- `:canvas`

## Build and Test Tasks Discovered

Read-only Gradle discovery commands succeeded:

- `.\gradlew.bat --version`
- `.\gradlew.bat tasks --all`

Relevant tasks discovered:

- `build`
- `:composeApp:build`
- `:composeApp:desktopTest`
- `:composeApp:check`
- `:composeApp:desktopJar`
- `:composeApp:createDistributable`
- `:composeApp:createReleaseDistributable`
- `:composeApp:package`
- `:composeApp:packageDistributionForCurrentOS`
- `:composeApp:packageExe`
- `:composeApp:packageMsi`
- `:composeApp:packageReleaseExe`
- `:composeApp:packageReleaseMsi`
- `:composeApp:packageReleaseDistributionForCurrentOS`
- `downloadWix`

## Current QA Documentation and Screenshots

Existing QA material includes:

- `docs/qa/full-functionality-audit.md`
- `docs/qa/all-reference-screens-pixel-lock.md`
- final route screenshots for home, search, library, playlist detail, artist detail, album detail, now playing, queue/session, downloads, and settings
- responsive screenshots at 1366x768 and 1012x643 for selected screens
- QA JSON reports for downloads, offline playback, mini-player always-on-top, and queue save behavior
- numerous older visual reconstruction screenshots and logs

The strongest current hero candidate is:

- `docs/qa/home-final-runtime-proof-regression-1672x941.png`

## Sensitive File Audit Result

Searches were run for sensitive terms including:

- `password`
- `passwd`
- `token`
- `secret`
- `api_key`
- `apikey`
- `authorization`
- `bearer`
- `private key`
- `client_secret`
- `.github_pat`
- `ghp_`
- `github_pat_`

High-risk literal credential patterns were not found in publishable files. Matches were inspected and were source-code models or method parameters for auth/token handling, not committed secrets.

Files containing auth-related code terms include:

- `lastfm/src/main/kotlin/com/omnitune/lastfm/models/Authentication.kt`
- `lastfm/src/main/kotlin/com/omnitune/lastfm/LastFM.kt`
- `innertube/src/main/kotlin/com/omnitune/innertube/PlaybackAuthState.kt`
- `innertube/src/main/kotlin/com/omnitune/innertube/YouTube.kt`
- `innertube/src/main/kotlin/com/omnitune/innertube/InnerTube.kt`
- `kizzy/src/main/kotlin/com/omnitune/kizzy/rpc/KizzyRPC.kt`
- `kizzy/src/main/kotlin/com/omnitune/kizzy/gateway/DiscordWebSocket.kt`
- `kizzy/src/main/kotlin/com/omnitune/kizzy/gateway/entities/Identify.kt`
- `kizzy/src/main/kotlin/com/omnitune/kizzy/gateway/entities/Resume.kt`

Local machine files and generated artifacts that must not be published include:

- `local.properties`
- `.gradle/`
- `.kotlin/`
- module `build/` directories
- root `build/`
- generated logs
- downloaded runtime QA media under `docs/qa/runtime-download-artifacts/`

## Licensing Status

- No root `LICENSE` file was present during the baseline audit.
- Multiple source headers state GPL-3.0 licensing and Android-origin provenance.
- Publication work must preserve that existing GPL-3.0 indication and avoid claiming a different license.

## Major Publication Risks

- The worktree had substantial uncommitted source, test, screenshot, and QA evidence changes that need deliberate staging.
- The current branch was not `main`; the final public branch should be `main` while preserving existing history.
- No GitHub remote existed.
- The target GitHub repository did not exist.
- Packaging tasks exist, but MSI/EXE release readiness still requires validation on Windows and may require WiX/tooling downloads.
- Some runtime QA artifacts include absolute local paths and a downloaded audio file; those should not be committed as repository assets.
- The app currently depends on libVLC/VLC discovery at standard Windows install paths; this must be documented for users and contributors.
- A root license file was absent despite GPL-3.0 source headers.
