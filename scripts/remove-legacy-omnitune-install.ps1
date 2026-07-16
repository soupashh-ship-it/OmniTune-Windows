param(
    [switch] $Execute
)

$ErrorActionPreference = "Stop"

function Get-LegacyOmniTuneEntries {
    $roots = @(
        "HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall\*",
        "HKLM:\Software\Microsoft\Windows\CurrentVersion\Uninstall\*",
        "HKLM:\Software\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall\*"
    )

    foreach ($root in $roots) {
        Get-ItemProperty $root -ErrorAction SilentlyContinue |
            Where-Object {
                $_.DisplayName -match "^OmniTuneWindows$|OmniTuneWindows"
            } |
            Select-Object DisplayName, DisplayVersion, InstallLocation, UninstallString, PSPath
    }
}

function Split-UninstallCommand {
    param([string] $Command)

    if ([string]::IsNullOrWhiteSpace($Command)) {
        throw "Legacy entry has no uninstall command."
    }

    $trimmed = $Command.Trim()
    if ($trimmed.StartsWith('"')) {
        $endQuote = $trimmed.IndexOf('"', 1)
        if ($endQuote -lt 1) { throw "Malformed quoted uninstall command: $Command" }
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

$entries = @(Get-LegacyOmniTuneEntries)

if ($entries.Count -eq 0) {
    Write-Host "No legacy OmniTuneWindows install entries found." -ForegroundColor Green
    exit 0
}

Write-Host "Legacy OmniTuneWindows install entries:" -ForegroundColor Yellow
$entries | Format-Table DisplayName, DisplayVersion, InstallLocation, UninstallString -AutoSize

if (-not $Execute) {
    Write-Host ""
    Write-Host "Dry run only. Re-run with -Execute to launch the registered uninstallers." -ForegroundColor Cyan
    exit 2
}

foreach ($entry in $entries) {
    $command = Split-UninstallCommand -Command $entry.UninstallString
    Write-Host "Launching uninstaller for $($entry.DisplayName) $($entry.DisplayVersion): $($command.FilePath) $($command.Arguments)"
    $process = Start-Process -FilePath $command.FilePath -ArgumentList $command.Arguments -Wait -PassThru
    if ($process.ExitCode -ne 0) {
        throw "Uninstaller for $($entry.DisplayName) exited with code $($process.ExitCode)."
    }
}

Write-Host "Legacy uninstall commands completed. Re-run scripts\verify-installed-omnitune.ps1 to confirm." -ForegroundColor Green
