param(
    [string]$MakeAppx = "",
    [string]$SignTool = "",
    [string]$Publisher = $env:OMNITUNE_MSIX_PUBLISHER,
    [string]$CertificatePath = $env:OMNITUNE_SIGN_CERT_PATH,
    [string]$CertificatePassword = $env:OMNITUNE_SIGN_CERT_PASSWORD,
    [string]$TimestampUrl = $env:OMNITUNE_TIMESTAMP_URL,
    [switch]$SkipBuild,
    [switch]$SkipSigning
)

$ErrorActionPreference = "Stop"
$project = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$gradleProps = Get-Content (Join-Path $project "gradle.properties")
$version = ($gradleProps | Where-Object { $_ -like "omnitune.version=*" } | Select-Object -First 1).Split("=", 2)[1].Trim()
$msixVersion = if ($version -match '^\d+\.\d+\.\d+$') { "$version.0" } else { throw "MSIX requires semantic numeric omnitune.version, got '$version'." }

function Find-Tool([string]$name) {
    $fromPath = Get-Command $name -ErrorAction SilentlyContinue
    if ($fromPath) { return $fromPath.Source }
    $kitRoot = "C:\Program Files (x86)\Windows Kits\10\bin"
    $found = Get-ChildItem $kitRoot -Recurse -Filter $name -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName -match "\\x64\\$name$" } |
        Sort-Object FullName -Descending |
        Select-Object -First 1
    if ($found) { return $found.FullName }
    throw "$name not found. Install the Windows SDK or pass the tool path explicitly."
}

$makeAppxPath = if ($MakeAppx) { $MakeAppx } else { Find-Tool "makeappx.exe" }
$signToolPath = if ($SignTool) { $SignTool } else { Find-Tool "signtool.exe" }

if ([string]::IsNullOrWhiteSpace($Publisher)) {
    $Publisher = "CN=OmniTune"
}

if (-not $SkipBuild) {
    & (Join-Path $project "gradlew.bat") ":composeApp:createDistributable"
    if ($LASTEXITCODE -ne 0) { throw "createDistributable failed." }
}

$appImage = Join-Path $project "composeApp\build\compose\binaries\main\app\OmniTune"
if (!(Test-Path (Join-Path $appImage "OmniTune.exe"))) {
    throw "OmniTune app image was not found at $appImage. Run createDistributable first."
}

$outRoot = Join-Path $project "build\release\windows\msix"
$stage = Join-Path $outRoot "staging"
$assets = Join-Path $stage "Assets"
$output = Join-Path $outRoot "OmniTune-$version-windows-x64.msix"

Remove-Item $stage -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $stage, $assets | Out-Null
Copy-Item (Join-Path $appImage "*") $stage -Recurse -Force

Add-Type -AssemblyName System.Drawing
function Write-Asset([string]$name, [int]$width, [int]$height) {
    $source = Join-Path $project "composeApp\src\desktopMain\resources\omnitune-icon.png"
    $image = [System.Drawing.Image]::FromFile($source)
    try {
        $bitmap = New-Object System.Drawing.Bitmap($width, $height)
        $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.DrawImage($image, 0, 0, $width, $height)
        $graphics.Dispose()
        $bitmap.Save((Join-Path $assets $name), [System.Drawing.Imaging.ImageFormat]::Png)
        $bitmap.Dispose()
    } finally {
        $image.Dispose()
    }
}

Write-Asset "Square44x44Logo.png" 44 44
Write-Asset "Square150x150Logo.png" 150 150
Write-Asset "StoreLogo.png" 50 50

$manifest = @"
<?xml version="1.0" encoding="utf-8"?>
<Package
  xmlns="http://schemas.microsoft.com/appx/manifest/foundation/windows10"
  xmlns:uap="http://schemas.microsoft.com/appx/manifest/uap/windows10"
  xmlns:rescap="http://schemas.microsoft.com/appx/manifest/foundation/windows10/restrictedcapabilities"
  IgnorableNamespaces="uap rescap">
  <Identity
    Name="SoupashhShipIt.OmniTuneWindows"
    Publisher="$Publisher"
    Version="$msixVersion"
    ProcessorArchitecture="x64" />
  <Properties>
    <DisplayName>OmniTune Windows</DisplayName>
    <PublisherDisplayName>OmniTune</PublisherDisplayName>
    <Logo>Assets\StoreLogo.png</Logo>
  </Properties>
  <Dependencies>
    <TargetDeviceFamily Name="Windows.Desktop" MinVersion="10.0.17763.0" MaxVersionTested="10.0.26100.0" />
  </Dependencies>
  <Resources>
    <Resource Language="en-us" />
  </Resources>
  <Applications>
    <Application Id="OmniTune" Executable="OmniTune.exe" EntryPoint="Windows.FullTrustApplication">
      <uap:VisualElements
        DisplayName="OmniTune"
        Description="Open-source music player for Windows"
        BackgroundColor="#030917"
        Square150x150Logo="Assets\Square150x150Logo.png"
        Square44x44Logo="Assets\Square44x44Logo.png" />
    </Application>
  </Applications>
  <Capabilities>
    <rescap:Capability Name="runFullTrust" />
  </Capabilities>
</Package>
"@

$manifest | Set-Content -Path (Join-Path $stage "AppxManifest.xml") -Encoding UTF8
New-Item -ItemType Directory -Force -Path $outRoot | Out-Null
Remove-Item $output -Force -ErrorAction SilentlyContinue

& $makeAppxPath pack /d $stage /p $output /o
if ($LASTEXITCODE -ne 0) { throw "makeappx failed." }

if (-not $SkipSigning) {
    if ([string]::IsNullOrWhiteSpace($CertificatePath)) {
        Write-Warning "MSIX created unsigned because OMNITUNE_SIGN_CERT_PATH is not set. Unsigned MSIX packages cannot be installed by normal users."
    } else {
        $args = @("sign", "/fd", "SHA256", "/f", $CertificatePath)
        if (-not [string]::IsNullOrWhiteSpace($CertificatePassword)) { $args += @("/p", $CertificatePassword) }
        if (-not [string]::IsNullOrWhiteSpace($TimestampUrl)) { $args += @("/tr", $TimestampUrl, "/td", "SHA256") }
        $args += $output
        & $signToolPath @args
        if ($LASTEXITCODE -ne 0) { throw "signtool failed." }
    }
}

$hash = (Get-FileHash $output -Algorithm SHA256).Hash.ToLowerInvariant()
"$hash  $(Split-Path $output -Leaf)" | Set-Content -Path "$output.sha256" -Encoding ASCII

Write-Host "MSIX: $output"
