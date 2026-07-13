# OmniTune Windows installer release QA

| Test | Result | Evidence | Notes |
|---|---:|---|---|
| Compose EXE installer task | PASS | `.\gradlew.bat :composeApp:packageExe` | Produced jpackage EXE installer. |
| Compose MSI installer task | PASS | `.\gradlew.bat :composeApp:packageMsi` | Produced jpackage MSI installer. |
| Release wrapper script | PASS | `.\scripts\release\build-windows-release.ps1` | Ran compile, assemble, root tests, desktop tests, EXE/MSI packaging, hashes, manifest. |
| Primary EXE artifact | PASS | `build/release/windows/OmniTune-Setup-0.1.0-windows-x64.exe` | 133,158,400 bytes; SHA-256 `ff6b0a7642d103c1eec3809982e7f4540b110a728821f316c53234605cdc704b`. |
| MSI artifact | PASS | `build/release/windows/OmniTune-0.1.0-windows-x64.msi` | 132,586,321 bytes; SHA-256 `54cd7cdc6c85591b0ea7b6dcf2b17b85380730f144044bf89bf583e924a92a36`. |
| Release manifest | PASS | `build/release/windows/release-manifest.json` | Records version, x64 architecture, hashes, Java runtime bundled, VLC runtime bundled, unsigned status. |
| Private Java runtime image | PASS | `composeApp/build/compose/binaries/main/app/OmniTune/runtime` | Runtime image exists in app image. |
| Bundled VLC/libVLC files | PASS | `composeApp/build/compose/binaries/main/app/OmniTune/native/vlc/libvlc.dll` | Packaged app image contains libVLC and plugins. |
| Native lookup is packaged-first | PASS | `NativeRuntime.configureNativeAudioRuntime()` | Searches `native/vlc` before system fallback. |
| Packaged app-image launch | PASS | `composeApp/build/compose/binaries/main/app/OmniTune/OmniTune.exe` | Launched with isolated `%LOCALAPPDATA%`; process stayed alive; startup log selected packaged VLC runtime. |
| Third-party notices | PASS | `THIRD_PARTY_NOTICES.txt`, bundled under `licenses/` | VLC notices copied from local VLC runtime. |
| QA media not in app image | PASS | `rg runtime-download-artifacts` over app image | No QA download artifact found in app image. |
| Development project path not in app image | PASS | `rg "D:\\Omnitune Windoww"` over app image | No project path found in app image. |
| Release-ProGuard package task | FAIL | `:composeApp:packageReleaseExe` | Existing ProGuard unresolved optional dependency warnings; use `packageExe/packageMsi`. |
| Clean machine install | FAIL | Not executed in this environment | Current machine already contains an older `C:\Program Files\OmniTuneWindows` install, so it is not a clean target. |
| Installer lifecycle install | FAIL | Attempted silent MSI install | Existing old installation contaminated the test; clean VM/test user required. |
| Start menu launch | FAIL | Not executed in this environment | Requires installer installation on clean target. |
| No system Java required | FAIL | Not executed on clean machine | App image includes private runtime, but clean-machine proof remains. |
| No system VLC required | FAIL | Not executed on machine without VLC | App image includes VLC and packaged runtime was selected in app-image launch, but clean-machine proof remains. |
| Installed-build playback/search/download QA | FAIL | Not executed in installed build | Requires install/run QA. |
| Uninstall/reinstall/upgrade QA | FAIL | Not executed in this environment | Requires installed package lifecycle QA. |
