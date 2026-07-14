# OmniTune Windows installer release QA

| Test | Result | Evidence | Notes |
|---|---:|---|---|
| Compose release EXE installer task | PASS | `.\gradlew.bat :composeApp:packageReleaseExe` | Produced jpackage EXE installer. |
| Compose release MSI installer task | PASS | `.\gradlew.bat :composeApp:packageReleaseMsi` | Produced jpackage MSI installer. |
| Release wrapper script | PASS | `.\scripts\release\build-windows-release.ps1` | Ran compile, assemble, root tests, desktop tests, EXE/MSI packaging, hashes, manifest. |
| GitHub Release workflow | PASS | `https://github.com/soupashh-ship-it/OmniTune-Windows/actions/runs/29241303516` | Corrected workflow ran from `main` and completed successfully, producing workflow artifacts. |
| Primary EXE artifact | PASS | `build/release/windows/OmniTune-Setup-0.1.3-windows-x64.exe` | 196,457,984 bytes; SHA-256 `7f64ef6585b41d367532085a4b21d0e90c181230fe612bfc94866ab66dab3dc9`. |
| MSI artifact | PASS | `build/release/windows/OmniTune-0.1.3-windows-x64.msi` | 195,885,909 bytes; SHA-256 `9c5572c49b1de7eeb42560b3512787223e8920328108cc9cb1a14cf044907d4d`. |
| Release manifest | PASS | `build/release/windows/release-manifest.json` | Records version, x64 architecture, hashes, Java runtime bundled, VLC runtime bundled, unsigned status. |
| Code signing hook | PASS | `scripts/release/build-windows-release.ps1 -Sign` | Opt-in signing supports `OMNITUNE_SIGNTOOL`, `OMNITUNE_SIGN_CERT_PATH`, `OMNITUNE_SIGN_CERT_PASSWORD`, and timestamp URL. No certificate is committed. |
| Code signing actual signature | FAIL | No certificate available | Installers remain unsigned; SmartScreen warnings may occur. |
| GitHub RC publication | PASS | `https://github.com/soupashh-ship-it/OmniTune-Windows/releases/tag/v0.1.1-rc.1` | Pre-release contains EXE, MSI, `SHA256SUMS.txt`, and `release-manifest.json`. This release supersedes 0.1.0 RC2 to avoid Windows Installer same-version error 1638. |
| GitHub 0.1.2 RC publication | PASS | `https://github.com/soupashh-ship-it/OmniTune-Windows/releases/tag/v0.1.2-rc.1` | Pre-release contains EXE, MSI, checksum files, `SHA256SUMS.txt`, and `release-manifest.json`. |
| GitHub 0.1.3 RC publication | PASS | `https://github.com/soupashh-ship-it/OmniTune-Windows/releases/tag/v0.1.3-rc.1` | 0.1.3 supersedes 0.1.2 after install QA found missing installed native VLC files. |
| Same-version installer failure diagnosis | PASS | Windows Event Log `MsiInstaller` status `1638` for OmniTune 0.1.0 | RC2 appeared not to open because OmniTune 0.1.0 was already installed; 0.1.1 uses a higher installer version for upgrade/install retry. |
| Manual search hotfix validation | PASS | `:innertube:test --tests com.omnitune.innertube.InnertubeSearchTest`; `:composeApp:desktopTest` | Provider-backed song search returns results; desktop tests pass after preview-key search submission fix. |
| Private Java runtime image | PASS | `composeApp/build/compose/binaries/main/app/OmniTune/runtime` | Runtime image exists in app image. |
| Bundled VLC/libVLC files | PASS | `composeApp/build/compose/binaries/main/app/OmniTune/native/vlc/libvlc.dll` | Packaged app image contains libVLC and plugins. |
| Native lookup is packaged-first with embedded fallback | PASS | `NativeRuntime.configureNativeAudioRuntime()` | Searches packaged `native/vlc` first, then extracts embedded VLC resources to `%LOCALAPPDATA%\OmniTuneData\native\vlc-runtime` before system fallback. |
| Packaged app-image launch | PASS | `composeApp/build/compose/binaries/main/app/OmniTune/OmniTune.exe` | 0.1.2 app image launched with `OMNITUNE_QA_REQUIRE_BUNDLED_VLC=true`; process stayed alive after 12 seconds. |
| Bundled VLC isolation | PASS | `%LOCALAPPDATA%\OmniTune\logs\startup.log` in isolated launch | Selected `native/vlc`; logged exact `libvlc.dll`, `libvlccore.dll`, and `plugins` paths from the packaged app image. |
| Installed embedded VLC isolation | PASS | `%LOCALAPPDATA%\OmniTuneData\logs\startup.log` | Installed 0.1.3 launched with `OMNITUNE_QA_REQUIRE_BUNDLED_VLC=true`; selected `embedded-resource`, extracted `libvlc.dll`, `libvlccore.dll`, and `plugins`. |
| Installed provider search smoke | PASS | `docs/qa/search-runtime-qa.json` | Installed 0.1.3 returned 70 provider-backed results for `Blinding Lights` with no error. |
| Stale VLC plugin cache excluded | PASS | `composeApp/build/compose/binaries/main/app/OmniTune/native/vlc/plugins/plugins.dat` absent | Prevents build-machine plugin cache from being shipped. |
| Third-party notices | PASS | `THIRD_PARTY_NOTICES.txt`, bundled under `licenses/` | VLC notices copied from local VLC runtime. |
| QA media not in app image | PASS | `rg runtime-download-artifacts` over app image | No QA download artifact found in app image. |
| Development project path not in app image | PASS | `rg "D:\\Omnitune Windoww"` over app image | No project path found in app image. |
| Release-ProGuard package task | PASS | `composeApp/build.gradle.kts` | Release ProGuard minification is disabled by policy; `packageReleaseExe` and `packageReleaseMsi` pass. |
| Current-machine installer lifecycle install | PASS | HKCU uninstall entry: OmniTune `0.1.3`, install path `C:\Users\soupa\AppData\Local\OmniTune\` | Current machine is not clean because older `OmniTuneWindows` also exists under Program Files, but the OmniTune 0.1.3 per-user install itself succeeded. |
| Start menu shortcut | PASS | `C:\Users\soupa\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\OmniTune\OmniTune.lnk` | Shortcut exists after install. |
| Uninstall data preservation | PASS | Marker file under `%LOCALAPPDATA%\OmniTuneData` survived uninstall | Uninstall removed app binaries/entry and preserved user data by policy. |
| Reinstall data rediscovery | PASS | Same marker file remained after reinstalling 0.1.3 | Reinstall recreated app install while preserving `%LOCALAPPDATA%\OmniTuneData`. |
| Upgrade 0.1.1 -> 0.1.3 current-machine test | PARTIAL | `docs/qa/upgrade-0.1.1-to-0.1.3-qa.json`; `PlatformContextMigrationTest` | MSI upgrade succeeded and embedded VLC extraction worked. The synthetic JSON-only migration caveat was fixed with marker-based allowlisted migration and tested locally, but a clean full older-RC VM matrix remains unproven. |
| Clean machine install | FAIL | Not executed in this environment | User explicitly deferred clean Windows VM/no Java/no VLC test. |
| No system Java required | FAIL | Not executed on clean machine | App image includes private runtime, but clean-machine proof remains deferred. |
| No system VLC required | FAIL | Not executed on machine without VLC | Packaged/embedded VLC path is proven on current machine; no-system-VLC clean proof remains deferred. |
| Installed-build playback/search/download QA | PARTIAL | Installed provider search smoke PASS; embedded VLC launch PASS | Full installed-build playback/download walkthrough was not rerun in this pass. |
