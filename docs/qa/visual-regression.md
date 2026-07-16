# Visual Regression Workflow

OmniTune now has a repeatable screenshot-regression runner:

```powershell
.\docs\qa\visual_regression.ps1
```

By default it captures:

- Home
- Search
- Library
- Playlist
- Liked Songs
- Album
- Artist
- Queue
- Downloads
- Settings
- Now Playing

at:

- `1672x941`
- `1366x768`
- `1012x643`

Current captures are written to:

```text
docs/qa/screenshots/current/
```

That directory is ignored by git.

## Baselines

Approved visual baselines should be placed in:

```text
docs/qa/visual-baselines/
```

Use the filename format:

```text
<route>_<width>x<height>.png
```

Example:

```text
settings_1366x768.png
```

## Report

The runner writes:

```text
docs/qa/screenshots/visual-regression-report.json
```

Each route/size is classified as:

- `PASS`
- `DIFF`
- `BASELINE_MISSING`
- `CURRENT_MISSING`
- `CAPTURE_FAILED`
- `SIZE_MISMATCH`

Missing baselines are not treated as failure. Diffs, missing current captures, capture failures, and size mismatches fail the script.

## CI usage

This should become an explicit visual QA job after curated baselines are approved. It should not block normal unit-test runs until the baselines are stable.
