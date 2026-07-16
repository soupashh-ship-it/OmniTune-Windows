# docs/qa/capture_all.ps1

$routes = @("home", "search", "library", "playlist", "liked", "album", "artist", "downloads", "settings", "nowplaying")
$resolutions = @(
    @{ w = 1280; h = 800; name = "desktop" },
    @{ w = 800; h = 600; name = "compact" }
)

$exePath = "D:\Omnitune Windoww\composeApp\build\compose\binaries\main\app\OmniTune\OmniTune.exe"
$outDir = "D:\Omnitune Windoww\docs\qa\screenshots"

if (!(Test-Path $outDir)) {
    New-Item -ItemType Directory -Force -Path $outDir | Out-Null
}

Add-Type -AssemblyName System.Drawing
Add-Type @"
using System;
using System.Drawing;
using System.Runtime.InteropServices;
public class WinQA {
    [DllImport("user32.dll")] public static extern bool GetWindowRect(IntPtr hWnd, out RECT r);
    [DllImport("user32.dll")] public static extern bool PrintWindow(IntPtr hWnd, IntPtr hdc, uint f);
    [DllImport("user32.dll")] public static extern bool MoveWindow(IntPtr hWnd, int X, int Y, int nWidth, int nHeight, bool bRepaint);
    [StructLayout(LayoutKind.Sequential)] public struct RECT { public int Left,Top,Right,Bottom; }
}
"@

foreach ($route in $routes) {
    Write-Host "Capturing route: $route"
    
    $env:OMNITUNE_QA_ROUTE = $route
    if ($route -eq "search") {
        $env:OMNITUNE_QA_SEARCH_QUERY = "ed sheeran"
    } else {
        $env:OMNITUNE_QA_SEARCH_QUERY = ""
    }
    
    # Start the app
    Start-Process -FilePath $exePath
    
    # Wait for the window to appear
    $hwnd = [IntPtr]::Zero
    $procToKill = $null
    $retries = 0
    while ($hwnd -eq [IntPtr]::Zero -and $retries -lt 30) {
        Start-Sleep -Milliseconds 500
        $p = Get-Process | Where-Object { $_.MainWindowTitle -like "*OmniTune*" } | Select-Object -First 1
        if ($p -and $p.MainWindowHandle -ne [IntPtr]::Zero) {
            $hwnd = $p.MainWindowHandle
            $procToKill = $p
        }
        $retries++
    }
    
    if ($hwnd -ne [IntPtr]::Zero) {
        # Give it a moment to render the content
        Start-Sleep -Seconds 3
        
        foreach ($res in $resolutions) {
            $width = $res.w
            $height = $res.h
            $label = $res.name
            
            # Resize to responsive size
            [WinQA]::MoveWindow($hwnd, 10, 10, $width, $height, $true)
            Start-Sleep -Seconds 1
            
            # Capture
            $rect = New-Object WinQA+RECT
            [WinQA]::GetWindowRect($hwnd, [ref]$rect)
            $bmpW = $rect.Right - $rect.Left
            $bmpH = $rect.Bottom - $rect.Top
            
            if ($bmpW -gt 0 -and $bmpH -gt 0) {
                $bmp = New-Object System.Drawing.Bitmap($bmpW, $bmpH)
                $g = [System.Drawing.Graphics]::FromImage($bmp)
                $hdc = $g.GetHdc()
                [WinQA]::PrintWindow($hwnd, $hdc, 2) # PW_RENDERFULLCONTENT
                $g.ReleaseHdc($hdc)
                $g.Dispose()
                
                $filename = "$outDir\${route}_${label}_${width}x${height}.png"
                $bmp.Save($filename, [System.Drawing.Imaging.ImageFormat]::Png)
                $bmp.Dispose()
                Write-Host "  -> Saved $filename"
            } else {
                Write-Host "  -> Invalid window dimensions for $label"
            }
        }
    } else {
        Write-Host "  -> Window failed to appear"
    }
    
    # Kill the app
    if ($procToKill) {
        Stop-Process -Id $procToKill.Id -Force -EA SilentlyContinue
    }
    # Also kill any lingering java processes just in case
    Get-Process java -EA SilentlyContinue | Stop-Process -Force -EA SilentlyContinue
    Get-Process OmniTune -EA SilentlyContinue | Stop-Process -Force -EA SilentlyContinue
    Start-Sleep -Seconds 1
}

Write-Host "All captures complete!"
