param([string]$OutPath = "D:\Omnitune Windoww\docs\qa\background-foundation-correction\current.png")

Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing
Add-Type @"
using System;
using System.Drawing;
using System.Runtime.InteropServices;
public class ScreenCap {
    [DllImport("user32.dll")] public static extern bool GetWindowRect(IntPtr h, out RECT r);
    [DllImport("user32.dll")] public static extern bool SetForegroundWindow(IntPtr h);
    [DllImport("user32.dll")] public static extern bool ShowWindow(IntPtr h, int cmd);
    [DllImport("user32.dll")] public static extern IntPtr GetForegroundWindow();
    [StructLayout(LayoutKind.Sequential)] public struct RECT { public int L, T, R, B; }
}
"@

$p = Get-Process java -ErrorAction SilentlyContinue |
     Where-Object { $_.MainWindowTitle -like "*OmniTune*" } |
     Select-Object -First 1

if (-not $p -or $p.MainWindowHandle -eq 0) {
    Write-Host "NOT_FOUND"
    exit 1
}

$hw = [IntPtr]$p.MainWindowHandle
[ScreenCap]::ShowWindow($hw, 9) | Out-Null   # SW_RESTORE
[ScreenCap]::SetForegroundWindow($hw) | Out-Null
Start-Sleep -Milliseconds 800

$r = New-Object ScreenCap+RECT
[ScreenCap]::GetWindowRect($hw, [ref]$r)
$x = $r.L; $y = $r.T
$w = $r.R - $r.L; $h = $r.B - $r.T

Write-Host "Window: $x,$y ${w}x${h} Title=$($p.MainWindowTitle)"

if ($w -lt 100 -or $h -lt 100) { Write-Host "TOO_SMALL"; exit 1 }

$dir = Split-Path $OutPath
if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }

# BitBlt screen grab (captures actual pixels as displayed)
$bmp = New-Object System.Drawing.Bitmap($w, $h)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.CopyFromScreen($x, $y, 0, 0, [System.Drawing.Size]::new($w, $h), [System.Drawing.CopyPixelOperation]::SourceCopy)
$g.Dispose()
$bmp.Save($OutPath)
$bmp.Dispose()
Write-Host "SAVED:$OutPath"
