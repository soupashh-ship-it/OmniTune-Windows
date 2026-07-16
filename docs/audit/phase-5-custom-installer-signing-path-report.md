# Phase 5 Custom Installer and Signing Path Report

Project: `D:\Omnitune Windoww`

Date: 2026-07-16

## Objective

Move beyond stock jpackage installer limitations by adding a custom installer path that can support:

- stronger OmniTune branding inside the installer;
- same-version reinstall/overwrite behavior;
- user-data preservation;
- future Authenticode signing.

## Implemented

### Custom Inno Setup installer definition

Added:

- `installer/inno/OmniTune.iss`

Behavior:

- app name: `OmniTune`;
- app id: stable `7A8B9C0D-1E2F-3A4B-5C6D-7E8F9A0B1C2D`;
- install dir: `%LOCALAPPDATA%\OmniTune`;
- Start menu shortcut;
- optional desktop shortcut;
- setup icon: existing OmniTune icon;
- branded wizard image support;
- installs over the same app id/app directory;
- does not delete `%LOCALAPPDATA%\OmniTuneData`.

### Custom installer build script

Added:

- `scripts/release/build-inno-installer.ps1`

Behavior:

- builds/prepares the Compose app image;
- generates OmniTune-branded wizard BMP assets from the current icon;
- compiles the Inno installer when Inno Setup 6 is installed;
- signs the installer when `-Sign` and signing environment variables are provided;
- writes a custom installer manifest and SHA-256 hash.

Prepare-only validation was run:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\release\build-inno-installer.ps1 -SkipTests -PrepareOnly
```

Result:

- PASS
- app image prepared;
- wizard image generated;
- wizard small image generated.

Full custom installer build was attempted:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\release\build-inno-installer.ps1 -SkipTests
```

Result:

- BLOCKED because Inno Setup 6 compiler is not installed locally.

Verified local tool state:

- `winget`: available
- `iscc.exe`: not found
- `signtool.exe`: not found
- WiX/NSIS tools: not found

## Required user/system actions

Install Inno Setup:

```powershell
winget install --id JRSoftware.InnoSetup -e
```

Install Windows SDK signing tools:

```powershell
winget install --id Microsoft.WindowsSDK.10.0.26100 -e
```

Provide a real code-signing certificate outside the repository, then set:

```powershell
$env:OMNITUNE_SIGNTOOL = "C:\Program Files (x86)\Windows Kits\10\bin\<sdk-version>\x64\signtool.exe"
$env:OMNITUNE_SIGN_CERT_PATH = "C:\secure\OmniTune-CodeSigning.pfx"
$env:OMNITUNE_SIGN_CERT_PASSWORD = "<from secret manager>"
$env:OMNITUNE_TIMESTAMP_URL = "http://timestamp.digicert.com"
```

Build signed custom installer:

```powershell
.\scripts\release\build-inno-installer.ps1 -Sign
```

## Final state

The custom installer path is implemented and prepare-validated. Actual custom installer compilation and signing are blocked only by missing local external tools/certificate.

