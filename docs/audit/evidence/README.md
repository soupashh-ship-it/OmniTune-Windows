# Audit Evidence Notes

Audit date: 2026-07-16

This directory is reserved for evidence created during the current verification pass.

Runtime app launch was verified:

- `.\gradlew.bat :composeApp:run` launched a Java process with main window title `OmniTune`.
- The process was responding.
- The launched process was closed after smoke verification.

Screenshot limitation:

- A screenshot capture was attempted for the Settings responsive case.
- The capture targeted unrelated foreground desktop content instead of the OmniTune window.
- The incorrect artifact was deleted immediately and is not retained here.
- Future visual QA should use a controlled desktop session or app-window-specific capture tool that verifies the window title/content before saving evidence.

No production source files were modified for this audit.
