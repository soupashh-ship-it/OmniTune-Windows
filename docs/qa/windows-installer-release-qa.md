# OmniTune Windows installer release QA

| Test | Result | Evidence | Notes |
|---|---:|---|---|
| Compose release EXE installer task | PASS | `.\gradlew.bat :composeApp:packageReleaseExe` | Produced jpackage EXE installer. |
| Compose release MSI installer task | PASS | `.\gradlew.bat :composeApp:packageReleaseMsi` | Produced jpackage MSI installer. |
| Release wrapper script | PASS | `.\scripts\release\build-windows-release.ps1` | Ran compile, assemble, root tests, desktop tests, EXE/MSI packaging, hashes, manifest. |
| Primary EXE artifact | PASS | `build/release/windows/OmniTune-Setup-0.1.0-windows-x64.exe` | 133,166,592 bytes; SHA-256 `fbb445561b605f31738e167aba3770a7e8dffb3b74b08a5e40aac33460a0acb1`. |
| MSI artifact | PASS | `build/release/windows/OmniTune-0.1.0-windows-x64.msi` | 132,594,516 bytes; SHA-256 `b063e06d7be292bd5a8c26fdd39e502d6a9cedd6c105f77ab7b7f0f200aed00c`. |
| Release manifest | PASS | `build/release/windows/release-manifest.json` | Records version, x64 architecture, hashes, Java runtime bundled, VLC runtime bundled, unsigned status. |
| GitHub RC publication | PASS | `https://github.com/soupashh-ship-it/OmniTune-Windows/releases/tag/v0.1.0-rc.1` | Pre-release contains EXE, MSI, `SHA256SUMS.txt`, and `release-manifest.json`. |
| Private Java runtime image | PASS | `composeApp/build/compose/binaries/main/app/OmniTune/runtime` | Runtime image exists in app image. |
| Bundled VLC/libVLC files | PASS | `composeApp/build/compose/binaries/main/app/OmniTune/native/vlc/libvlc.dll` | Packaged app image contains libVLC and plugins. |
| Native lookup is packaged-first | PASS | `NativeRuntime.configureNativeAudioRuntime()` | Searches `native/vlc` before system fallback. |
| Packaged app-image launch | PASS | `composeApp/build/compose/binaries/main/app/OmniTune/OmniTune.exe` | Launched with isolated `%LOCALAPPDATA%`, `JAVA_HOME` cleared, restricted `PATH`, and `OMNITUNE_QA_REQUIRE_BUNDLED_VLC=true`; process stayed alive. |
| Bundled VLC isolation | PASS | `%LOCALAPPDATA%\OmniTune\logs\startup.log` in isolated launch | Selected `native/vlc`; logged exact `libvlc.dll`, `libvlccore.dll`, and `plugins` paths from the packaged app image. |
| Stale VLC plugin cache excluded | PASS | `composeApp/build/compose/binaries/main/app/OmniTune/native/vlc/plugins/plugins.dat` absent | Prevents build-machine plugin cache from being shipped. |
| Third-party notices | PASS | `THIRD_PARTY_NOTICES.txt`, bundled under `licenses/` | VLC notices copied from local VLC runtime. |
| QA media not in app image | PASS | `rg runtime-download-artifacts` over app image | No QA download artifact found in app image. |
| Development project path not in app image | PASS | `rg "D:\\Omnitune Windoww"` over app image | No project path found in app image. |
| Release-ProGuard package task | PASS | `composeApp/build.gradle.kts` | Release ProGuard minification is disabled by policy; `packageReleaseExe` and `packageReleaseMsi` pass. |
| Clean machine install | FAIL | Not executed in this environment | Current machine already contains an older `C:\Program Files\OmniTuneWindows` install, so it is not a clean target. |
| Installer lifecycle install | FAIL | Attempted silent MSI install | Existing old installation contaminated the test; clean VM/test user required. |
| Start menu launch | FAIL | Not executed in this environment | Requires installer installation on clean target. |
| No system Java required | FAIL | Not executed on clean machine | App image includes private runtime, but clean-machine proof remains. |
| No system VLC required | FAIL | Not executed on machine without VLC | App image includes VLC and packaged runtime was selected in app-image launch, but clean-machine proof remains. |
| Installed-build playback/search/download QA | FAIL | Not executed in installed build | Requires install/run QA. |
| Uninstall/reinstall/upgrade QA | FAIL | Not executed in this environment | Requires installed package lifecycle QA. |
