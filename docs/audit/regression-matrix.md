# OmniTune Windows Regression Matrix

Date: 2026-07-15

This matrix tracks the audit remediation work. It is intentionally concise; detailed evidence belongs in the test output and issue ledger.

| Area | Baseline behavior | Audit phases touching area | Verification method | Current status |
| --- | --- | --- | --- | --- |
| Application startup | App launches from Gradle run | None directly | Manual launch required at final gate | Pending final verification |
| Home | Provider-backed Home renders or shows error state | UI null-safety cleanup | Compile; desktop tests; visual/manual final check | Compile PASS |
| Browse | Existing Browse route protected | None directly | Final navigation smoke | Pending final verification |
| Radio | Session identity/dedupe/continuation already present | Playback token guard can affect radio-start playback | Targeted radio policy test; final smoke | Targeted test PASS |
| Search | Search route and expanded section protected | UI null-safety cleanup | Compile; desktop tests; final smoke | Compile PASS |
| Library | Existing Library route protected | None directly | Final navigation smoke | Pending final verification |
| Playlist detail | Real playlist UI preserved | UI null-safety cleanup | Compile; desktop tests; final smoke | Compile PASS |
| Liked Songs | New Liked Songs route renders honest empty/current state | Download count/message correction | Compile; desktop tests; final visual check | Compile PASS |
| Album | Album route provider load protected | UI null-safety cleanup | Compile; final navigation smoke | Compile PASS |
| Artist | Artist route provider load protected | UI null-safety cleanup | Compile; final navigation smoke | Compile PASS |
| Downloads | Existing download manager protected | Managed-path hardening | `OmniDownloadManagerTest` | Targeted tests PASS |
| Settings | Existing Settings route protected | None in this audit phase | Final navigation smoke | Pending final verification |
| Queue | Existing queue semantics protected | Playback token guard adjacent | Desktop tests; final playback smoke | Targeted tests PASS |
| Playback play/pause/next/previous | Baseline PASS | Playback token guard | Compile; targeted tests; final smoke | Compile PASS |
| Shuffle/repeat | Baseline PASS | Playback token guard touches shuffled starts | Compile; final smoke | Compile PASS |
| Add to queue / Play Next | Baseline PASS | No direct semantic change | Desktop tests | Pending full run |
| Playlist mutations | Baseline PASS | No direct semantic change | SettingsRepository tests | Pending full run |
| Downloads delete/restore | Baseline PASS | Managed-path hardening | New targeted tests | PASS |
| Offline/local source selection | Baseline test coverage exists | Managed completed-file trust hardened | Download tests; final smoke where available | Targeted tests PASS |
| Search history | Baseline PASS | No direct semantic change | SettingsRepository tests | Pending full run |
| Settings persistence | Baseline PASS | No direct semantic change | SettingsRepository tests | Pending full run |
| Reduced motion | Baseline protected | No direct semantic change | Final UI smoke | Pending final verification |
| Mini-player | Baseline protected | No direct semantic change | Desktop tests/final smoke | Pending full run |
| Window resizing | Baseline protected | No visual/layout refactor intended | Manual final smoke | Pending final verification |
