# OmniTune Windows local-feasible remaining work

Date: 2026-07-14

Scope: local closure pass. Clean VM/no-Java/no-VLC, physical no-network installed playback, and multi-hour soak are intentionally deferred by the user and are not counted as failures in this pass.

| Gap | Current evidence | Real status | Priority | Required proof |
|---|---|---|---|---|
| Clean Windows VM / no Java / no VLC | Not attempted by instruction | DEFERRED BY USER | P0 later | Clean VM install and launch. |
| Physical no-network installed-app offline playback | Not attempted by instruction | DEFERRED BY USER | P0 later | Installed build plays downloaded file with OS network unavailable. |
| Multi-hour playback soak | Not attempted by instruction | DEFERRED BY USER | P1 later | Several-hour playback with memory/thread/native-handle observation. |
| Actual Windows code signing | Signing hook exists; no certificate available | EXTERNAL BLOCKER | P0 for public release | Real certificate and signed EXE/MSI verification. |
| Legacy install-collision migration | Marker-based allowlisted migration now tested | PROVEN | P0 | `PlatformContextMigrationTest` pass. |
| Atomic JSON writes | `AtomicFileStore` writes temp + fsync + backup + replace | PROVEN | P0 | `AtomicFileStoreTest` pass. |
| Corrupt primary + valid backup recovery | Settings/download backups used after corrupt primary | PROVEN | P0 | Settings/download desktop tests pass. |
| Permission denied UX | Atomic write-denial simulation added; Queue Save as Playlist no longer reports false success | PARTIAL PASS | P1 | Complete UI-level failure matrix for history/session/download destination paths. |
| No disk space UX | Simulated no-space before atomic replace preserves existing primary and backup | PARTIAL PASS | P1 | Physical disk-full validation and media-download ENOSPC UX remain unproven. |
| Full keyboard traversal | Source shortcuts verified; full Tab/Shift+Tab traversal not automated | PARTIAL | P1 | Manual or automated focus traversal matrix. |
| External screen-reader validation | Semantics improved; no NVDA/Narrator pass performed | PARTIAL | P1 | Real screen-reader pass. |
| Semantic interaction audit | Empty handlers removed and misleading actions corrected; full runtime semantic matrix remains broad | PARTIAL | P1 | Runtime pass through all visible controls. |
| Visual pixel-lock | All eight captures/diffs exist; measured differences remain | PARTIAL | P1 | Fresh capture/diff iteration until stable landmarks converge. |
| Responsive/theme visual regression | Existing captures and theme screenshots exist; not rerun after this code-only pass | PARTIAL | P2 | Fresh screenshots at 1672×941, 1366×768, 1012×643 across changed screens/themes. |
