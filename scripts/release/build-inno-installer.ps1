param(
    [string] $Version = "",
    [string] $InnoCompiler = "",
    [switch] $SkipTests,
    [switch] $Sign,
    [switch] $PrepareOnly
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Resolve-Path (Join-Path $scriptDir "..\..")
Set-Location $projectRoot

function Read-OmniVersion {
    $props = Get-Content (Join-Path $projectRoot "gradle.properties")
    foreach ($line in $props) {
        if ($line -match "^\s*omnitune\.version\s*=\s*(.+?)\s*$") {
            return $Matches[1]
        }
    }
    throw "gradle.properties does not define omnitune.version"
}

function Invoke-Gradle {
    param([string[]] $Arguments)
    & (Join-Path $projectRoot "gradlew.bat") @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle failed: $($Arguments -join ' ')"
    }
}

function Resolve-InnoCompiler {
    param([string] $ExplicitPath)

    if ($ExplicitPath.Trim()) {
        if (-not (Test-Path -LiteralPath $ExplicitPath)) {
            throw "Inno Setup compiler not found: $ExplicitPath"
        }
        return (Resolve-Path -LiteralPath $ExplicitPath).Path
    }

    $command = Get-Command "iscc.exe" -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }

    $candidates = @(
        "${env:ProgramFiles(x86)}\Inno Setup 6\ISCC.exe",
        "$env:ProgramFiles\Inno Setup 6\ISCC.exe"
    )
    foreach ($candidate in $candidates) {
        if (Test-Path -LiteralPath $candidate) {
            return $candidate
        }
    }

    throw "Inno Setup 6 compiler was not found. Install Inno Setup 6 or pass -InnoCompiler <path-to-ISCC.exe>."
}

function New-InstallerBrandingAssets {
    param(
        [string] $IconPath,
        [string] $OutputDir
    )

    Add-Type -AssemblyName System.Drawing
    Add-Type -AssemblyName System.Windows.Forms

    New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

    $wizardImage = Join-Path $OutputDir "wizard-image.bmp"
    $wizardSmallImage = Join-Path $OutputDir "wizard-small-image.bmp"

    $iconImage = [System.Drawing.Image]::FromFile($IconPath)
    try {
        $large = New-Object System.Drawing.Bitmap 164, 314
        $graphics = [System.Drawing.Graphics]::FromImage($large)
        try {
            $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
            $graphics.Clear([System.Drawing.Color]::FromArgb(3, 9, 23))
            $brush = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
                (New-Object System.Drawing.Rectangle 0, 0, 164, 314),
                [System.Drawing.Color]::FromArgb(6, 13, 30),
                [System.Drawing.Color]::FromArgb(31, 20, 113),
                70
            )
            $graphics.FillRectangle($brush, 0, 0, 164, 314)
            $brush.Dispose()
            $graphics.DrawImage($iconImage, 42, 88, 80, 80)
            $font = New-Object System.Drawing.Font "Segoe UI", 15, ([System.Drawing.FontStyle]::Bold)
            $textBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::White)
            $graphics.DrawString("OmniTune", $font, $textBrush, 30, 190)
            $font.Dispose()
            $textBrush.Dispose()
        } finally {
            $graphics.Dispose()
        }
        $large.Save($wizardImage, [System.Drawing.Imaging.ImageFormat]::Bmp)
        $large.Dispose()

        $small = New-Object System.Drawing.Bitmap 55, 55
        $graphics = [System.Drawing.Graphics]::FromImage($small)
        try {
            $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
            $graphics.Clear([System.Drawing.Color]::FromArgb(2, 7, 20))
            $graphics.DrawImage($iconImage, 8, 8, 39, 39)
        } finally {
            $graphics.Dispose()
        }
        $small.Save($wizardSmallImage, [System.Drawing.Imaging.ImageFormat]::Bmp)
        $small.Dispose()
    } finally {
        $iconImage.Dispose()
    }

    [pscustomobject]@{
        WizardImageFile = $wizardImage
        WizardSmallImageFile = $wizardSmallImage
    }
}

function Invoke-CodeSign {
    param([string] $Path)

    $signtool = if ($env:OMNITUNE_SIGNTOOL) { $env:OMNITUNE_SIGNTOOL } else { "signtool.exe" }
    $certPath = $env:OMNITUNE_SIGN_CERT_PATH
    $certPassword = $env:OMNITUNE_SIGN_CERT_PASSWORD
    $timestampUrl = if ($env:OMNITUNE_TIMESTAMP_URL) { $env:OMNITUNE_TIMESTAMP_URL } else { "http://timestamp.digicert.com" }

    if (-not $certPath) {
        throw "Signing requested, but OMNITUNE_SIGN_CERT_PATH is not set."
    }
    if (-not (Test-Path -LiteralPath $certPath)) {
        throw "Signing certificate not found: $certPath"
    }

    $args = @("sign", "/fd", "SHA256", "/td", "SHA256", "/tr", $timestampUrl, "/f", $certPath)
    if ($certPassword) {
        $args += @("/p", $certPassword)
    }
    $args += $Path

    & $signtool @args
    if ($LASTEXITCODE -ne 0) {
        throw "Code signing failed for $Path"
    }
}

$releaseVersion = if ($Version.Trim()) { $Version.Trim() } else { Read-OmniVersion }
$architecture = if ([Environment]::Is64BitOperatingSystem) { "x64" } else { "x86" }
$iconPath = Join-Path $projectRoot "composeApp\src\desktopMain\resources\omnitune-icon.png"
$setupIconPath = Join-Path $projectRoot "composeApp\src\desktopMain\resources\icon.ico"
$brandingDir = Join-Path $projectRoot "build\installer-branding"
$sourceDir = Join-Path $projectRoot "composeApp\build\compose\binaries\main\app\OmniTune"
$outputDir = Join-Path $projectRoot "build\release\windows\inno"
$issPath = Join-Path $projectRoot "installer\inno\OmniTune.iss"

if (-not $SkipTests) {
    Invoke-Gradle @(":composeApp:compileKotlinDesktop", ":composeApp:assemble", "test", ":composeApp:desktopTest")
}

Invoke-Gradle @(":composeApp:createDistributable")

if (-not (Test-Path -LiteralPath (Join-Path $sourceDir "OmniTune.exe"))) {
    throw "Expected app image missing OmniTune.exe: $sourceDir"
}

$branding = New-InstallerBrandingAssets -IconPath $iconPath -OutputDir $brandingDir
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

if ($PrepareOnly) {
    Write-Host "Prepared app image and installer branding assets."
    Write-Host "App image: $sourceDir"
    Write-Host "Wizard image: $($branding.WizardImageFile)"
    Write-Host "Wizard small image: $($branding.WizardSmallImageFile)"
    exit 0
}

$iscc = Resolve-InnoCompiler -ExplicitPath $InnoCompiler
$innoArgs = @(
    "/DAppVersion=$releaseVersion",
    "/DArchitecture=$architecture",
    "/DSourceDir=$sourceDir",
    "/DOutputDir=$outputDir",
    "/DSetupIconFile=$setupIconPath",
    "/DWizardImageFile=$($branding.WizardImageFile)",
    "/DWizardSmallImageFile=$($branding.WizardSmallImageFile)",
    $issPath
)

& $iscc @innoArgs
if ($LASTEXITCODE -ne 0) {
    throw "Inno Setup compiler failed."
}

$installer = Join-Path $outputDir "OmniTune-Setup-$releaseVersion-windows-$architecture-custom.exe"
if (-not (Test-Path -LiteralPath $installer)) {
    throw "Expected custom installer missing: $installer"
}

if ($Sign) {
    Invoke-CodeSign -Path $installer
}

$hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $installer).Hash.ToLowerInvariant()
"$hash  $(Split-Path -Leaf $installer)" | Set-Content -Encoding ascii -LiteralPath "$installer.sha256"

$manifest = [ordered]@{
    app = "OmniTune"
    version = $releaseVersion
    platform = "windows"
    architecture = $architecture
    installerTechnology = "Inno Setup"
    buildTimestamp = (Get-Date).ToUniversalTime().ToString("o")
    file = (Split-Path -Leaf $installer)
    sha256 = $hash
    sizeBytes = (Get-Item -LiteralPath $installer).Length
    signed = [bool]$Sign
    branding = [ordered]@{
        setupIcon = $setupIconPath
        wizardImage = $branding.WizardImageFile
        wizardSmallImage = $branding.WizardSmallImageFile
    }
    behavior = [ordered]@{
        appId = "7A8B9C0D-1E2F-3A4B-5C6D-7E8F9A0B1C2D"
        sameVersionReinstall = "Supported by installing over the same AppId/app directory."
        userDataPreserved = "%LOCALAPPDATA%\\OmniTuneData is outside the install directory and is not deleted by this installer."
    }
}

$manifest | ConvertTo-Json -Depth 8 | Set-Content -Encoding utf8 -LiteralPath (Join-Path $outputDir "custom-installer-manifest.json")

Write-Host "Custom OmniTune installer written to $installer"
