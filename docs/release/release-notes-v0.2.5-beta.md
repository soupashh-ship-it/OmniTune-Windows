# OmniTune Windows 0.2.5 Beta

This beta focuses on release trust, update safety, installer hardening, playlist data safety, Settings honesty, and playback/queue reliability.

## Added

- Added `PRIVACY.md` for SignPath Foundation review and public privacy disclosure.
- Added real Settings/About update checking against GitHub release metadata.
- Added managed playlist cover import so playlist artwork is copied into app data instead of depending on arbitrary external files.
- Added guarded legacy install cleanup tooling.
- Added install/update helper flow for same-version reinstall scenarios.
- Added user-data preservation verification script for install/update QA.
- Added custom Inno Setup installer definition and build script for a future branded installer path.
- Added release/signature/install verification documentation.

## Fixed

- Removed unsupported active Settings toggles and replaced them with honest unavailable-state rows.
- Restricted external URI opening from Settings to `http` and `https`.
- Improved shuffled queue previous/next behavior with deterministic shuffle history.
- Hardened playback request gating so stale async playback resolutions cannot override newer track selections.
- Reworked VLC release/shutdown handling to avoid an unbounded blocking release path during normal disposal.

## Verified

- Full Gradle gate passed:
  - `:composeApp:compileKotlinDesktop`
  - `:composeApp:assemble`
  - `test`
  - `:composeApp:desktopTest`
- Desktop tests: 103 passing, 0 failures.
- Innertube tests: 5 passing, 0 failures.
- Windows EXE/MSI release packaging verified locally.
- Release manifest confirms bundled Java runtime and bundled VLC runtime.

## Known limitations

- Public installers remain unsigned until a real signing certificate or SignPath Foundation signing approval is available.
- Custom Inno installer build path is implemented but requires Inno Setup 6 on the release machine.
- Clean VM install proof, physical offline installed-app proof, and long playback soak remain external QA tasks.

