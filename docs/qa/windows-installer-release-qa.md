# OmniTune Windows installer release QA

| Test | Result | Evidence | Notes |
|---|---:|---|---|
| Compose EXE installer task | PASS | `.\gradlew.bat :composeApp:packageExe` | Produced jpackage EXE installer. |
| Compose MSI installer task | PASS | `.\gradlew.bat :composeApp:packageMsi` | Produced jpackage MSI installer. |
| Private Java runtime image | PASS | `composeApp/build/compose/binaries/main/app/OmniTune/runtime` | Runtime image exists in app image. |
| Bundled VLC/libVLC files | PASS | `composeApp/build/compose/binaries/main/app/OmniTune/native/vlc/libvlc.dll` | Packaged app image contains libVLC and plugins. |
| Native lookup is packaged-first | PASS | `NativeRuntime.configureNativeAudioRuntime()` | Searches `native/vlc` before system fallback. |
| Third-party notices | PASS | `THIRD_PARTY_NOTICES.txt`, bundled under `licenses/` | VLC notices copied from local VLC runtime. |
| QA media not in app image | PASS | `rg runtime-download-artifacts` over app image | No QA download artifact found in app image. |
| Development project path not in app image | PASS | `rg "D:\\Omnitune Windoww"` over app image | No project path found in app image. |
| Release-ProGuard package task | FAIL | `:composeApp:packageReleaseExe` | Existing ProGuard unresolved optional dependency warnings; use `packageExe/packageMsi`. |
| Clean machine install | FAIL | Not executed in this environment | Requires clean Windows VM/test account. |
| Start menu launch | FAIL | Not executed in this environment | Requires installer installation. |
| No system Java required | FAIL | Not executed on clean machine | App image includes private runtime, but clean-machine proof remains. |
| No system VLC required | FAIL | Not executed on machine without VLC | App image includes VLC, but clean-machine proof remains. |
| Installed-build playback/search/download QA | FAIL | Not executed in installed build | Requires install/run QA. |
| Uninstall/reinstall/upgrade QA | FAIL | Not executed in this environment | Requires installed package lifecycle QA. |
