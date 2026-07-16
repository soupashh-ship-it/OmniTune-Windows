param(
    [switch] $Json
)

$ErrorActionPreference = "Stop"

function Read-UninstallEntries {
    $roots = @(
        "HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall",
        "HKLM:\Software\Microsoft\Windows\CurrentVersion\Uninstall",
        "HKLM:\Software\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall"
    )

    foreach ($root in $roots) {
        if (-not (Test-Path $root)) { continue }
        Get-ChildItem $root | ForEach-Object {
            $props = Get-ItemProperty -LiteralPath $_.PsPath
            $name = [string]$props.DisplayName
            if ($name -match "OmniTune") {
                [pscustomobject]@{
                    scope = if ($root -like "HKCU:*") { "CurrentUser" } else { "LocalMachine" }
                    registryKey = $_.Name
                    displayName = $name
                    displayVersion = [string]$props.DisplayVersion
                    installLocation = [string]$props.InstallLocation
                    uninstallString = [string]$props.UninstallString
                    publisher = [string]$props.Publisher
                }
            }
        }
    }
}

function Read-ShortcutEntries {
    $paths = @(
        [Environment]::GetFolderPath("Desktop"),
        [Environment]::GetFolderPath("CommonDesktopDirectory"),
        [Environment]::GetFolderPath("StartMenu"),
        [Environment]::GetFolderPath("CommonStartMenu")
    ) | Where-Object { $_ -and (Test-Path -LiteralPath $_) } | Select-Object -Unique

    $shell = New-Object -ComObject WScript.Shell
    foreach ($path in $paths) {
        Get-ChildItem -LiteralPath $path -Filter "*.lnk" -Recurse -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -match "OmniTune" } |
            ForEach-Object {
                $shortcut = $shell.CreateShortcut($_.FullName)
                [pscustomobject]@{
                    path = $_.FullName
                    targetPath = $shortcut.TargetPath
                    arguments = $shortcut.Arguments
                    workingDirectory = $shortcut.WorkingDirectory
                }
            }
    }
}

$report = [ordered]@{
    generatedAt = (Get-Date).ToUniversalTime().ToString("o")
    uninstallEntries = @(Read-UninstallEntries)
    shortcuts = @(Read-ShortcutEntries)
    expectedCurrentVersion = (Get-Content (Join-Path (Split-Path -Parent $PSScriptRoot) "..\gradle.properties") |
        Where-Object { $_ -match "^\s*omnitune\.version\s*=" } |
        Select-Object -First 1) -replace "^\s*omnitune\.version\s*=\s*", ""
    note = "If an installed entry or shortcut points to an older version/path, uninstall the older OmniTune/OmniTuneWindows package and reinstall the current release artifact."
}

if ($Json) {
    $report | ConvertTo-Json -Depth 8
} else {
    "OmniTune install diagnosis"
    "Expected current version: $($report.expectedCurrentVersion)"
    ""
    "Installed entries:"
    if ($report.uninstallEntries.Count -eq 0) {
        "  None found"
    } else {
        $report.uninstallEntries | Format-Table displayName, displayVersion, scope, installLocation -AutoSize | Out-String
    }
    "Shortcuts:"
    if ($report.shortcuts.Count -eq 0) {
        "  None found"
    } else {
        $report.shortcuts | Format-Table path, targetPath -AutoSize | Out-String
    }
    $report.note
}
