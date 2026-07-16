# OmniTune Windows MSIX Packaging

OmniTune's primary public installer remains the branded Inno Setup installer. MSIX is now available as an explicit Windows SDK packaging path for future Microsoft Store / sideload evaluation.

Build an MSIX package:

```powershell
.\scripts\release\build-msix-package.ps1
```

Build without signing:

```powershell
.\scripts\release\build-msix-package.ps1 -SkipSigning
```

Output:

```text
build/release/windows/msix/OmniTune-<version>-windows-x64.msix
build/release/windows/msix/OmniTune-<version>-windows-x64.msix.sha256
```

## Prerequisites

- Windows SDK with `makeappx.exe`.
- `signtool.exe` for signing.
- A real code-signing certificate for installable public packages.

Environment variables used for signing:

```powershell
$env:OMNITUNE_MSIX_PUBLISHER = "CN=Your Publisher Name"
$env:OMNITUNE_SIGN_CERT_PATH = "C:\secure\OmniTune-CodeSigning.pfx"
$env:OMNITUNE_SIGN_CERT_PASSWORD = "<secret>"
$env:OMNITUNE_TIMESTAMP_URL = "http://timestamp.digicert.com"
```

## Important limitation

Unsigned MSIX packages can be created for validation, but normal users cannot install them cleanly. Public MSIX distribution requires a trusted signing certificate and final identity decisions for Store submission.
