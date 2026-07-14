param(
    [string] $Version = "",
    [switch] $SkipTests,
    [switch] $Sign
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

function Copy-Artifact {
    param(
        [string] $Source,
        [string] $Destination
    )
    if (-not (Test-Path $Source)) {
        throw "Expected artifact missing: $Source"
    }
    Copy-Item -LiteralPath $Source -Destination $Destination -Force
}

function Write-HashFiles {
    param([System.IO.FileInfo[]] $Files)
    $sumLines = @()
    foreach ($file in $Files) {
        $hash = Get-FileHash -Algorithm SHA256 -LiteralPath $file.FullName
        "$($hash.Hash.ToLowerInvariant())  $($file.Name)" | Set-Content -Encoding ascii -LiteralPath "$($file.FullName).sha256"
        $sumLines += "$($hash.Hash.ToLowerInvariant())  $($file.Name)"
    }
    $sumLines | Set-Content -Encoding ascii -LiteralPath (Join-Path $releaseDir "SHA256SUMS.txt")
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
$vlcHome = if ($env:VLC_HOME) { $env:VLC_HOME } else { "C:\Program Files\VideoLAN\VLC" }

if (-not (Test-Path (Join-Path $vlcHome "libvlc.dll"))) {
    throw "VLC/libVLC runtime not found at '$vlcHome'. Install VLC x64 or set VLC_HOME before building the Windows release."
}
if (-not (Test-Path (Join-Path $vlcHome "plugins"))) {
    throw "VLC plugins directory not found at '$vlcHome\plugins'."
}

if (-not $SkipTests) {
    Invoke-Gradle @(":composeApp:compileKotlinDesktop", ":composeApp:assemble", "test", ":composeApp:desktopTest")
}

Invoke-Gradle @(":composeApp:createDistributable", ":composeApp:packageReleaseExe", ":composeApp:packageReleaseMsi")

$releaseDir = Join-Path $projectRoot "build\release\windows"
New-Item -ItemType Directory -Force -Path $releaseDir | Out-Null

$sourceExe = Join-Path $projectRoot "composeApp\build\compose\binaries\main-release\exe\OmniTune-$releaseVersion.exe"
$sourceMsi = Join-Path $projectRoot "composeApp\build\compose\binaries\main-release\msi\OmniTune-$releaseVersion.msi"
$targetExe = Join-Path $releaseDir "OmniTune-Setup-$releaseVersion-windows-$architecture.exe"
$targetMsi = Join-Path $releaseDir "OmniTune-$releaseVersion-windows-$architecture.msi"

Copy-Artifact -Source $sourceExe -Destination $targetExe
Copy-Artifact -Source $sourceMsi -Destination $targetMsi

if ($Sign) {
    Invoke-CodeSign -Path $targetExe
    Invoke-CodeSign -Path $targetMsi
}

$artifactFiles = @()
$artifactFiles += Get-Item -LiteralPath $targetExe
$artifactFiles += Get-Item -LiteralPath $targetMsi
Write-HashFiles -Files $artifactFiles

$exeHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $targetExe).Hash.ToLowerInvariant()
$msiHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $targetMsi).Hash.ToLowerInvariant()
$releaseAppImage = Join-Path $projectRoot "composeApp\build\compose\binaries\main-release\app\OmniTune"
$mainAppImage = Join-Path $projectRoot "composeApp\build\compose\binaries\main\app\OmniTune"
$appImage = if (Test-Path $releaseAppImage) { $releaseAppImage } else { $mainAppImage }
$manifest = [ordered]@{
    app = "OmniTune"
    version = $releaseVersion
    platform = "windows"
    architecture = $architecture
    buildTimestamp = (Get-Date).ToUniversalTime().ToString("o")
    javaRuntimeBundled = (Test-Path (Join-Path $appImage "runtime\release"))
    nativeAudioRuntimeBundled = (Test-Path (Join-Path $appImage "native\vlc\libvlc.dll"))
    installer = [ordered]@{
        file = (Split-Path -Leaf $targetExe)
        sha256 = $exeHash
        sizeBytes = (Get-Item -LiteralPath $targetExe).Length
    }
    msi = [ordered]@{
        file = (Split-Path -Leaf $targetMsi)
        sha256 = $msiHash
        sizeBytes = (Get-Item -LiteralPath $targetMsi).Length
    }
    bundledVlc = [ordered]@{
        source = $vlcHome
        libvlc = (Test-Path (Join-Path $appImage "native\vlc\libvlc.dll"))
        plugins = (Test-Path (Join-Path $appImage "native\vlc\plugins"))
    }
    signed = [bool]$Sign
}

$manifest | ConvertTo-Json -Depth 8 | Set-Content -Encoding utf8 -LiteralPath (Join-Path $releaseDir "release-manifest.json")

Write-Host "Windows release artifacts written to $releaseDir"
