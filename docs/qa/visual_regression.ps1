param(
    [string[]]$Routes = @("home", "search", "library", "playlist", "liked", "album", "artist", "queue", "downloads", "settings", "nowplaying"),
    [string[]]$Sizes = @("1672x941", "1366x768", "1012x643"),
    [string]$BaselineDir = "docs/qa/visual-baselines",
    [string]$CurrentDir = "docs/qa/screenshots/current",
    [string]$ReportPath = "docs/qa/screenshots/visual-regression-report.json",
    [double]$MeanDeltaThreshold = 2.5,
    [double]$ChangedPixelRatioThreshold = 0.015,
    [switch]$CompareOnly
)

$ErrorActionPreference = "Stop"
$project = "D:\Omnitune Windoww"
$captureScript = Join-Path $project "docs\qa\capture_route.ps1"
$baselineRoot = Join-Path $project $BaselineDir
$currentRoot = Join-Path $project $CurrentDir
$reportFile = Join-Path $project $ReportPath

Add-Type -AssemblyName System.Drawing

function Parse-Size([string]$size) {
    if ($size -notmatch '^(\d+)x(\d+)$') {
        throw "Invalid size '$size'. Expected WIDTHxHEIGHT."
    }
    return @{ Width = [int]$Matches[1]; Height = [int]$Matches[2] }
}

function Compare-Images([string]$baselinePath, [string]$currentPath) {
    $baseline = [System.Drawing.Bitmap]::new($baselinePath)
    $current = [System.Drawing.Bitmap]::new($currentPath)
    try {
        if ($baseline.Width -ne $current.Width -or $baseline.Height -ne $current.Height) {
            return @{
                result = "SIZE_MISMATCH"
                baselineSize = "$($baseline.Width)x$($baseline.Height)"
                currentSize = "$($current.Width)x$($current.Height)"
                meanDelta = $null
                changedPixelRatio = $null
            }
        }

        [long]$sumDelta = 0
        [long]$changed = 0
        [long]$pixels = [long]$baseline.Width * [long]$baseline.Height
        for ($y = 0; $y -lt $baseline.Height; $y++) {
            for ($x = 0; $x -lt $baseline.Width; $x++) {
                $a = $baseline.GetPixel($x, $y)
                $b = $current.GetPixel($x, $y)
                $delta = [math]::Abs($a.R - $b.R) + [math]::Abs($a.G - $b.G) + [math]::Abs($a.B - $b.B)
                $sumDelta += $delta
                if ($delta -gt 12) { $changed++ }
            }
        }

        $meanDelta = $sumDelta / ($pixels * 3.0)
        $changedRatio = $changed / [double]$pixels
        $passed = $meanDelta -le $MeanDeltaThreshold -and $changedRatio -le $ChangedPixelRatioThreshold
        return @{
            result = if ($passed) { "PASS" } else { "DIFF" }
            baselineSize = "$($baseline.Width)x$($baseline.Height)"
            currentSize = "$($current.Width)x$($current.Height)"
            meanDelta = [math]::Round($meanDelta, 4)
            changedPixelRatio = [math]::Round($changedRatio, 6)
        }
    } finally {
        $baseline.Dispose()
        $current.Dispose()
    }
}

New-Item -ItemType Directory -Force -Path $currentRoot | Out-Null
New-Item -ItemType Directory -Force -Path (Split-Path $reportFile) | Out-Null

$results = New-Object System.Collections.Generic.List[object]

foreach ($route in $Routes) {
    foreach ($size in $Sizes) {
        $parsed = Parse-Size $size
        $name = "$route`_$size.png"
        $currentRelative = Join-Path $CurrentDir $name
        $currentPath = Join-Path $project $currentRelative
        $baselinePath = Join-Path $baselineRoot $name

        if (-not $CompareOnly) {
            & $captureScript -Route $route -FileName $currentRelative -Width $parsed.Width -Height $parsed.Height
            if ($LASTEXITCODE -ne 0) {
                $results.Add([ordered]@{
                    route = $route
                    size = $size
                    result = "CAPTURE_FAILED"
                    baseline = $baselinePath
                    current = $currentPath
                })
                continue
            }
        }

        if (-not (Test-Path $currentPath)) {
            $results.Add([ordered]@{
                route = $route
                size = $size
                result = "CURRENT_MISSING"
                baseline = $baselinePath
                current = $currentPath
            })
            continue
        }

        if (-not (Test-Path $baselinePath)) {
            $results.Add([ordered]@{
                route = $route
                size = $size
                result = "BASELINE_MISSING"
                baseline = $baselinePath
                current = $currentPath
            })
            continue
        }

        $comparison = Compare-Images $baselinePath $currentPath
        $results.Add([ordered]@{
            route = $route
            size = $size
            result = $comparison.result
            baseline = $baselinePath
            current = $currentPath
            baselineSize = $comparison.baselineSize
            currentSize = $comparison.currentSize
            meanDelta = $comparison.meanDelta
            changedPixelRatio = $comparison.changedPixelRatio
        })
    }
}

$summary = [ordered]@{
    generatedAt = (Get-Date).ToString("o")
    routes = $Routes
    sizes = $Sizes
    baselineDir = $baselineRoot
    currentDir = $currentRoot
    thresholds = [ordered]@{
        meanDelta = $MeanDeltaThreshold
        changedPixelRatio = $ChangedPixelRatioThreshold
    }
    counts = [ordered]@{
        pass = @($results | Where-Object result -eq "PASS").Count
        diff = @($results | Where-Object result -eq "DIFF").Count
        missingBaseline = @($results | Where-Object result -eq "BASELINE_MISSING").Count
        failedCapture = @($results | Where-Object result -eq "CAPTURE_FAILED").Count
        missingCurrent = @($results | Where-Object result -eq "CURRENT_MISSING").Count
        sizeMismatch = @($results | Where-Object result -eq "SIZE_MISMATCH").Count
    }
    results = $results
}

$summary | ConvertTo-Json -Depth 8 | Set-Content -Path $reportFile -Encoding UTF8
Write-Host "Visual regression report: $reportFile"

if ($summary.counts.diff -gt 0 -or $summary.counts.failedCapture -gt 0 -or $summary.counts.missingCurrent -gt 0 -or $summary.counts.sizeMismatch -gt 0) {
    exit 1
}
