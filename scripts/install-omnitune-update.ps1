param(
    [Parameter(Mandatory = $true)]
    [string] $InstallerPath,

    [string] $ExpectedVersion = "",

    [switch] $SkipUninstall,

    [switch] $Passive
)

$ErrorActionPreference = "Stop"

function Get-OmniTuneEntries {
    $roots = @(
        "HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall\*",
        "HKLM:\Software\Microsoft\Windows\CurrentVersion\Uninstall\*",
        "HKLM:\Software\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall\*"
    )

    foreach ($root in $roots) {
        Get-ItemProperty $root -ErrorAction SilentlyContinue |
            Where-Object { $_.DisplayName -eq "OmniTune" } |
            Select-Object DisplayName, DisplayVersion, InstallLocation, UninstallString, PSPath
    }
}

function Split-CommandLine {
    param([string] $Command)

    if ([string]::IsNullOrWhiteSpace($Command)) {
        throw "Command line is empty."
    }

    $trimmed = $Command.Trim()
    if ($trimmed.StartsWith('"')) {
        $endQuote = $trimmed.IndexOf('"', 1)
        if ($endQuote -lt 1) { throw "Malformed quoted command: $Command" }
        return [pscustomobject]@{
            FilePath = $trimmed.Substring(1, $endQuote - 1)
            Arguments = $trimmed.Substring($endQuote + 1).Trim()
        }
    }

    $firstSpace = $trimmed.IndexOf(" ")
    if ($firstSpace -lt 0) {
        return [pscustomobject]@{ FilePath = $trimmed; Arguments = "" }
    }

    [pscustomobject]@{
        FilePath = $trimmed.Substring(0, $firstSpace)
        Arguments = $trimmed.Substring($firstSpace + 1).Trim()
    }
}

function Invoke-ProcessChecked {
    param(
        [string] $FilePath,
        [string] $Arguments
    )

    Write-Host "Running: $FilePath $Arguments"
    $process = Start-Process -FilePath $FilePath -ArgumentList $Arguments -Wait -PassThru
    if ($process.ExitCode -ne 0) {
        throw "Process failed with exit code $($process.ExitCode): $FilePath $Arguments"
    }
}

$resolvedInstaller = Resolve-Path -LiteralPath $InstallerPath
$installerFile = Get-Item -LiteralPath $resolvedInstaller
$appDataDir = Join-Path $env:LOCALAPPDATA "OmniTuneData"

Write-Host "Installer: $($installerFile.FullName)"
Write-Host "User data root: $appDataDir"
Write-Host "Policy: this script does not delete OmniTune user data."

if (-not $SkipUninstall) {
    $entries = @(Get-OmniTuneEntries)
    foreach ($entry in $entries) {
        Write-Host "Removing existing OmniTune $($entry.DisplayVersion) before install. User data remains under $appDataDir."
        $command = Split-CommandLine -Command $entry.UninstallString
        $arguments = $command.Arguments
        if ($Passive -and $command.FilePath -match "msiexec(\.exe)?$" -and $arguments -notmatch "/quiet|/passive|/qn") {
            $arguments = "$arguments /passive"
        }
        Invoke-ProcessChecked -FilePath $command.FilePath -Arguments $arguments
    }
}

$installerArgs = ""
if ($Passive -and $installerFile.Extension -ieq ".msi") {
    $installerArgs = "/i `"$($installerFile.FullName)`" /passive"
    Invoke-ProcessChecked -FilePath "msiexec.exe" -Arguments $installerArgs
} else {
    Invoke-ProcessChecked -FilePath $installerFile.FullName -Arguments ""
}

if ($ExpectedVersion.Trim()) {
    & (Join-Path (Split-Path -Parent $MyInvocation.MyCommand.Path) "verify-installed-omnitune.ps1") -ExpectedVersion $ExpectedVersion -FailOnMismatch
    if (-not $?) {
        throw "Installed OmniTune verification failed for expected version $ExpectedVersion."
    }
}

Write-Host "OmniTune install/update completed. Verify Settings > About shows the expected version." -ForegroundColor Green
