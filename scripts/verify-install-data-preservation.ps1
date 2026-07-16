param(
    [ValidateSet("Before", "After")]
    [string] $Mode = "Before",

    [string] $SnapshotPath = "",

    [switch] $HashDownloads
)

$ErrorActionPreference = "Stop"

if (-not $SnapshotPath.Trim()) {
    $SnapshotPath = Join-Path (Get-Location) "build\release\windows\user-data-preservation-snapshot.json"
}

$appDataDir = Join-Path $env:LOCALAPPDATA "OmniTuneData"
$markerPath = Join-Path $appDataDir "install-preservation-marker.txt"

function Get-RelativeFileSnapshot {
    param([string] $Root)

    if (-not (Test-Path -LiteralPath $Root)) {
        return @()
    }

    Get-ChildItem -LiteralPath $Root -Recurse -File -ErrorAction SilentlyContinue |
        Where-Object {
            $_.FullName -notmatch "\\logs\\" -and
            $_.FullName -notmatch "\\cache\\" -and
            $_.FullName -notmatch "\\native\\"
        } |
        ForEach-Object {
            $relative = $_.FullName.Substring($Root.Length).TrimStart([char[]]@([char]92, [char]47))
            $item = [ordered]@{
                path = $relative
                length = $_.Length
            }
            if ($HashDownloads -or $relative -notmatch "^downloads[\\/]") {
                $item.sha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $_.FullName).Hash.ToLowerInvariant()
            }
            [pscustomobject]$item
        } |
        Sort-Object path
}

function Write-Snapshot {
    New-Item -ItemType Directory -Force -Path $appDataDir | Out-Null
    Set-Content -Encoding utf8 -LiteralPath $markerPath -Value "OmniTune install preservation marker created $(Get-Date -Format o)"
    $snapshot = [ordered]@{
        generatedAt = (Get-Date).ToUniversalTime().ToString("o")
        appDataDir = $appDataDir
        markerPath = $markerPath
        hashDownloads = [bool]$HashDownloads
        files = @(Get-RelativeFileSnapshot -Root $appDataDir)
    }
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $SnapshotPath) | Out-Null
    $snapshot | ConvertTo-Json -Depth 8 | Set-Content -Encoding utf8 -LiteralPath $SnapshotPath
    Write-Host "Wrote user-data preservation snapshot: $SnapshotPath" -ForegroundColor Green
}

function Test-Snapshot {
    if (-not (Test-Path -LiteralPath $SnapshotPath)) {
        throw "Snapshot not found: $SnapshotPath"
    }
    if (-not (Test-Path -LiteralPath $markerPath)) {
        throw "Preservation marker missing after install/update: $markerPath"
    }

    $before = Get-Content -LiteralPath $SnapshotPath -Raw | ConvertFrom-Json
    $after = @(Get-RelativeFileSnapshot -Root $appDataDir)
    $afterByPath = @{}
    foreach ($file in $after) {
        $afterByPath[$file.path] = $file
    }

    $missing = @()
    $changed = @()
    foreach ($file in $before.files) {
        if (-not $afterByPath.ContainsKey($file.path)) {
            $missing += $file.path
            continue
        }
        $current = $afterByPath[$file.path]
        if ($null -ne $file.sha256 -and $null -ne $current.sha256 -and $file.sha256 -ne $current.sha256) {
            $changed += $file.path
        }
    }

    if ($missing.Count -gt 0 -or $changed.Count -gt 0) {
        [pscustomobject]@{
            missing = $missing
            changed = $changed
        } | ConvertTo-Json -Depth 5
        throw "User-data preservation verification failed."
    }

    Write-Host "User-data preservation verified. Marker and snapshotted files survived." -ForegroundColor Green
}

if ($Mode -eq "Before") {
    Write-Snapshot
} else {
    Test-Snapshot
}
