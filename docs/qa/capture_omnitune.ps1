
Add-Type -AssemblyName System.Drawing
Add-Type @"
using System;
using System.Drawing;
using System.Runtime.InteropServices;
public class WinCapture {
    [DllImport("user32.dll")] public static extern bool GetWindowRect(IntPtr hWnd, out RECT lpRect);
    [DllImport("user32.dll")] public static extern bool PrintWindow(IntPtr hWnd, IntPtr hdcBlt, uint nFlags);
    [StructLayout(LayoutKind.Sequential)]
    public struct RECT { public int Left, Top, Right, Bottom; }
}
"@
$proc = Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle -like "*OmniTune*" } | Select-Object -First 1
if ($proc) {
    $hwnd = [IntPtr]$proc.MainWindowHandle
    $rect = New-Object WinCapture+RECT
    [WinCapture]::GetWindowRect($hwnd, [ref]$rect)
    $bmpW = $rect.Right - $rect.Left
    $bmpH = $rect.Bottom - $rect.Top
    Write-Host "Window ${bmpW}x${bmpH}"
    $bmp = New-Object System.Drawing.Bitmap($bmpW, $bmpH)
    $graphics = [System.Drawing.Graphics]::FromImage($bmp)
    $hdc = $graphics.GetHdc()
    [WinCapture]::PrintWindow($hwnd, $hdc, 2)
    $graphics.ReleaseHdc($hdc)
    $graphics.Dispose()
    $path = "D:\Omnitune Windoww\docs\qa\bottom-player-exact-lock\final_player_exact.png"
    $bmp.Save($path)
    $bmp.Dispose()
    Write-Host "Saved: $path"
} else {
    Write-Host "OmniTune java process not found"
}
