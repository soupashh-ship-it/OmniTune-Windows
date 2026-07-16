# OmniTune Windows Current Baseline Verification

Audit date: 2026-07-16  
Project root: `D:\Omnitune Windoww`  
Branch: `main`  
HEAD: `951d421`

## Repository state

Command:

```powershell
git status --short
git branch --show-current
git rev-parse --short HEAD
git remote -v
```

Observed:

- Branch: `main`
- HEAD: `951d421`
- Remote: `origin https://github.com/soupashh-ship-it/OmniTune-Windows.git`
- Worktree: clean except existing untracked `docs/qa/screenshots/`

## Baseline build and test gate

Command:

```powershell
.\gradlew.bat :composeApp:compileKotlinDesktop :composeApp:assemble test :composeApp:desktopTest
```

Observed result:

- Result: PASS
- Gradle output: `BUILD SUCCESSFUL`
- Duration: about 2 seconds because tasks were up-to-date
- No build failure observed

## Test result counts

Parsed XML test reports:

| Suite group | Suites | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|---:|
| `composeApp` desktop tests | 23 | 94 | 0 | 0 | 0 |
| `innertube` tests | 2 | 5 | 0 | 0 | 0 |
| Total observed | 25 | 99 | 0 | 0 | 0 |

## Runtime launch smoke

Command pattern:

```powershell
Start-Process .\gradlew.bat -ArgumentList ":composeApp:run"
```

Observed:

- A Java process with main window title `OmniTune` appeared.
- The process was responding.
- The launched process was closed after smoke verification.

Limitations:

- Automated screenshot capture was attempted but captured the wrong foreground application due Windows desktop z-order. The incorrect artifact was deleted immediately.
- Therefore no screenshot evidence from this audit is retained.
- Runtime visual claims in this audit are based on source inspection plus launch smoke, not retained screenshot proof.

## Previous fixes re-verified from source

Previously documented stale playback-resolution race:

- Current source contains `PlaybackRequestGate`.
- `PlaybackRequestGate` uses a generation token and current-song identity check.
- `PlayerViewModel` holds a `playbackRequestGate` and uses it for current request validation.
- Status: source-level mitigation present.

Previously documented unsafe download path handling:

- `OmniDownloadManager.delete` only deletes `localFilePath` when `isManagedDownloadFile` returns true.
- `completedDownloadFor` only returns completed local files when `verifiedLocalFile()` exists and passes managed-root validation.
- `isManagedDownloadFile` canonicalizes both root and candidate.
- Status: source-level mitigation present.

Previously documented provider-controlled filename issue:

- `safeFileName` replaces characters outside `[A-Za-z0-9._ -]` and caps length at 80.
- Final download names and partial download names use `safeFileName`.
- Status: source-level mitigation present.

Previously documented production `!!` route/provider risk:

- Search of production Kotlin sources found no `!!`.
- Only observed `!!` match was in `OmniDownloadManagerTest.kt`.
- Status: previously documented production force-null assertion debt appears fixed.

## Existing generated artifacts

Untracked generated screenshots exist:

- Directory: `docs/qa/screenshots/`
- Count: 20 files
- Size: about 5.7 MB

This audit did not delete or modify them.
