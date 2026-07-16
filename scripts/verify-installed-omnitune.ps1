param(
    [string]$ExpectedVersion = "",
    [switch]$FailOnMismatch
)

$ErrorActionPreference = "Stop"

function Get-OmniTuneUninstallEntries {
    $roots = @(
        "HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall\*",
        "HKLM:\Software\Microsoft\Windows\CurrentVersion\Uninstall\*",
        "HKLM:\Software\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall\*"
    )

    foreach ($root in $roots) {
        Get-ItemProperty $root -ErrorAction SilentlyContinue |
            Where-Object {
                $_.DisplayName -match "OmniTune|OmniTuneWindows"
            } |
            Select-Object DisplayName, DisplayVersion, InstallLocation, Publisher, UninstallString, PSPath
    }
}

function Get-OmniTuneShortcuts {
    $shortcutRoots = @(
        [Environment]::GetFolderPath("Desktop"),
        [Environment]::GetFolderPath("StartMenu"),
        [Environment]::GetFolderPath("CommonDesktopDirectory"),
        [Environment]::GetFolderPath("CommonStartMenu")
    ) | Where-Object { $_ -and (Test-Path $_) } | Select-Object -Unique

    $shell = New-Object -ComObject WScript.Shell
    foreach ($root in $shortcutRoots) {
        Get-ChildItem -LiteralPath $root -Filter "*.lnk" -Recurse -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -match "OmniTune|OmniTuneWindows" } |
            ForEach-Object {
                $shortcut = $shell.CreateShortcut($_.FullName)
                [pscustomobject]@{
                    Name = $_.Name
                    Path = $_.FullName
                    TargetPath = $shortcut.TargetPath
                    Arguments = $shortcut.Arguments
                    WorkingDirectory = $shortcut.WorkingDirectory
                }
            }
    }
}

$entries = @(Get-OmniTuneUninstallEntries)
$shortcuts = @(Get-OmniTuneShortcuts)

Write-Host "OmniTune installed entries:" -ForegroundColor Cyan
if ($entries.Count -eq 0) {
    Write-Host "  none found"
} else {
    $entries | Format-Table -AutoSize
}

Write-Host ""
Write-Host "OmniTune shortcuts:" -ForegroundColor Cyan
if ($shortcuts.Count -eq 0) {
    Write-Host "  none found"
} else {
    $shortcuts | Format-Table -AutoSize
}

$mismatches = @()
if ($ExpectedVersion.Trim()) {
    $mismatches = @(
        $entries | Where-Object {
            $_.DisplayVersion -and $_.DisplayVersion -ne $ExpectedVersion
        }
    )

    if ($entries.Count -eq 0) {
        Write-Warning "No OmniTune uninstall entries were found to compare against expected version $ExpectedVersion."
    } elseif ($mismatches.Count -gt 0) {
        Write-Warning "One or more installed OmniTune entries do not match expected version $ExpectedVersion."
        $mismatches | Format-Table -AutoSize
    } else {
        Write-Host "Installed OmniTune entries match expected version $ExpectedVersion." -ForegroundColor Green
    }
}

$legacyEntries = @($entries | Where-Object { $_.DisplayName -match "OmniTuneWindows" })
if ($legacyEntries.Count -gt 0) {
    Write-Warning "Legacy OmniTuneWindows entries are present. Remove or validate them before release smoke sign-off."
}

if ($FailOnMismatch -and (($ExpectedVersion.Trim() -and $mismatches.Count -gt 0) -or $legacyEntries.Count -gt 0)) {
    exit 1
}

