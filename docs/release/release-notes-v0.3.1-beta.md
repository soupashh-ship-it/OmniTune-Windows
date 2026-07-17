# OmniTune Windows 0.3.1 Beta

OmniTune Windows 0.3.1 Beta is a launch hotfix for the Windows installer package.

## Fixes

- Fixed a Windows native launcher failure that could show "Failed to launch JVM" after installing 0.3.0.
- Added the required packaged Java runtime modules for SQLite/JDBC-backed persistence.
- Added a safe JSON persistence fallback if SQLite cannot initialize on a user machine.
- Preserved the SQLite persistence, diagnostics export, notifications, MSIX packaging, and update improvements from 0.3.0.

## Notes

- If 0.3.0 is installed and does not start, install 0.3.1 over it.
- User data under `%LOCALAPPDATA%\OmniTuneData` is preserved.
- Public installer signing still requires a trusted code-signing certificate.
