# OmniTune Windows 0.3.0 Beta

OmniTune Windows 0.3.0 Beta focuses on productization, reliability, diagnostics, packaging, and data durability.

## Changes and fixes

- Added SQLite-backed local persistence for core user data, with migration from existing JSON data.
- Preserved JSON-backed data as a compatibility backup path during migration.
- Added local crash-report capture and opt-in diagnostics export.
- Added a prefilled GitHub issue report flow for user-submitted diagnostics.
- Added Windows tray notification support for supported local app events.
- Added an MSIX packaging path for future Microsoft Store or enterprise distribution work.
- Added signing prerequisite checks for release builds that use a real code-signing certificate.
- Added visual-regression QA documentation and helper script.
- Improved release and packaging documentation for EXE, MSI, Inno, MSIX, signing, and update validation.
- Expanded regression coverage around persistence migration, downloads metadata, and diagnostics.

## Notes

- Public installer signing still requires a real trusted code-signing certificate.
- Hosted crash analytics are not enabled; diagnostics remain opt-in and user-controlled.
- MSIX packaging is available, but Store publishing still requires Microsoft Partner Center setup and signing identity.
