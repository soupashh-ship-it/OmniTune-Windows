# OmniTune Windows Phase 1 Critical Correctness Remediation Report

Date: 2026-07-16  
Project root: `D:\Omnitune Windoww`  
Branch: `main`  
Starting HEAD: `951d421`

## Overall result

PARTIAL SUCCESS

The code-level Phase 1 fixes were implemented and verified:

- playlist custom covers are imported into managed app data before persistence;
- unsupported Settings toggles no longer appear as active fake controls;
- release/install verification tooling was added;
- installer signature verification tooling was added;
- signing/update strategy documentation was started;
- compile and targeted persistence tests pass.

One local machine issue remains intentionally not changed by code:

- the verifier found a legacy `OmniTuneWindows 1.0.0` installed entry. Removing it requires an uninstall action on the machine, so it was not performed automatically.

## 1. Playlist cover persistence

Problem:

- Playlist edit previously persisted the selected cover image as an arbitrary external absolute path.
- If the user moved/deleted the source file, the playlist cover could disappear.

Changed:

- Added `PlaylistCoverStore`.
- `SettingsRepository` now constructs playlist persistence with a managed cover importer.
- `PlaylistPersistence.updatePlaylistMetadata` imports external cover files into managed app data before storing `coverPath`.

Managed destination:

```text
%LOCALAPPDATA%\OmniTuneData\playlist-covers
```

Protection:

- Only real files are copied.
- Existing managed cover files are preserved.
- Supported extensions: `png`, `jpg`, `jpeg`, `webp`.
- Unsupported or missing paths are not transformed unexpectedly.

Test coverage:

- `SettingsRepositoryTest.playlistCreateEditAddRemoveReorderDeletePersist` now verifies:
  - cover image is copied into `playlist-covers`;
  - persisted path points inside managed app data;
  - copied bytes match source bytes;
  - playlist metadata and song order still persist.

## 2. Settings feature honesty

Problem:

- Several Settings controls looked active but only persisted preferences:
  - Normalize Volume
  - Spatial Audio
  - Gapless Playback
  - Auto download playlists
  - New Music notifications
  - Recommendations notifications
  - Concert Alerts
  - Product Updates notifications
  - Weekly Digest

Changed:

- Removed active toggles for unsupported runtime features.
- Replaced them with honest informational rows:
  - `Not available in this build`
  - notification card explains Windows notification delivery is not enabled.
- Product Updates now opens GitHub Releases as an explicit release-page action, not a fake notification toggle.

Still real and wired:

- Download Quality
- Shuffle default
- Default volume
- Repeat mode
- Theme selection
- Reduced motion
- Mini-player always on top
- Global shortcuts when OmniTune is focused
- Download folder opening
- Files-present count
- Check for Updates / release page navigation

Additional safety:

- `openUri` now only allows `http` and `https` schemes.

## 3. Installed-version verification

Added:

```text
scripts/verify-installed-omnitune.ps1
```

Purpose:

- Finds OmniTune/legacy OmniTuneWindows uninstall entries.
- Lists Desktop and Start Menu shortcuts.
- Compares installed versions against an expected version.
- Can fail release verification with `-FailOnMismatch`.

Verification command run:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\verify-installed-omnitune.ps1 -ExpectedVersion 0.2.0
```

Observed on this machine:

- `OmniTune` 0.2.0 is installed under the user profile.
- Legacy `OmniTuneWindows` 1.0.0 is still installed under `C:\Program Files\OmniTuneWindows\`.
- Desktop and Start Menu OmniTune shortcuts exist.

Action still required outside code:

- Uninstall or explicitly validate the legacy `OmniTuneWindows 1.0.0` install before release smoke sign-off.

## 4. Signing verification and strategy

Added:

```text
scripts/verify-installer-signature.ps1
docs/release/phase-1-release-hardening.md
```

Purpose:

- Verify Authenticode signature status for release artifacts.
- Support release gating through `-RequireValid`.
- Document signing strategy and update strategy.

Verification command run:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\verify-installer-signature.ps1 -Path .\gradlew.bat
```

Result:

- Script executed and reported the expected non-installer/unsigned status for `gradlew.bat`.

Remaining limitation:

- The repository does not contain a code-signing certificate.
- Actual release signing cannot be completed until a real certificate and secure signing process are provided.

## 5. Build and test verification

Commands run:

```powershell
.\gradlew.bat :composeApp:compileKotlinDesktop
.\gradlew.bat :composeApp:desktopTest --tests "com.omnitune.app.platform.SettingsRepositoryTest"
```

Results:

- Compile: PASS
- Targeted SettingsRepository tests: PASS

Full gate should be run after this report:

```powershell
.\gradlew.bat :composeApp:compileKotlinDesktop
.\gradlew.bat :composeApp:assemble
.\gradlew.bat test
.\gradlew.bat :composeApp:desktopTest
```

## Files changed

Production:

- `composeApp/src/desktopMain/kotlin/com/omnitune/app/platform/PlaylistCoverStore.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/platform/PlaylistPersistence.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/platform/SettingsRepository.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/SettingsView.kt`

Tests:

- `composeApp/src/desktopTest/kotlin/com/omnitune/app/platform/SettingsRepositoryTest.kt`

Release tooling:

- `scripts/verify-installed-omnitune.ps1`
- `scripts/verify-installer-signature.ps1`

Docs:

- `docs/release/phase-1-release-hardening.md`
- `docs/audit/phase-1-critical-correctness-remediation-report.md`

## Remaining items

1. Remove or validate the legacy `OmniTuneWindows 1.0.0` install on this machine.
2. Acquire and configure a real Windows code-signing certificate.
3. Implement a real native update-check flow later; current Settings behavior remains honest by opening GitHub Releases.
4. Run clean VM install proof and installed offline playback proof before treating a release as public-stable.
