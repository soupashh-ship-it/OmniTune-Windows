param([string]$OutPath = "D:\Omnitune Windoww\docs\qa\background-foundation-correction\current.png")

Add-Type -AssemblyName System.Drawing
Add-Type @"
using System;
using System.Drawing;
using System.Runtime.InteropServices;
public class WC {
    [DllImport("user32.dll")] public static extern bool GetWindowRect(IntPtr h, out RECT r);
    [DllImport("user32.dll")] public static extern bool PrintWindow(IntPtr h, IntPtr dc, uint f);
    [DllImport("user32.dll")] public static extern bool SetForegroundWindow(IntPtr h);
    [StructLayout(LayoutKind.Sequential)] public struct RECT { public int L,T,R,B; }
}
"@

$maxWait = 90
$interval = 3
$elapsed = 0

while ($elapsed -lt $maxWait) {
    $p = Get-Process java -ErrorAction SilentlyContinue |
         Where-Object { $_.MainWindowTitle -like "*OmniTune*" } |
         Select-Object -First 1
    if ($p -and $p.MainWindowHandle -ne 0) {
        $hw = [IntPtr]$p.MainWindowHandle
        [WC]::SetForegroundWindow($hw) | Out-Null
        Start-Sleep -Milliseconds 500
        $r = New-Object WC+RECT
        [WC]::GetWindowRect($hw, [ref]$r)
        $w = $r.R - $r.L
        $h = $r.B - $r.T
        if ($w -gt 100 -and $h -gt 100) {
            $bmp = New-Object System.Drawing.Bitmap($w, $h)
            $g   = [System.Drawing.Graphics]::FromImage($bmp)
            $dc  = $g.GetHdc()
            [WC]::PrintWindow($hw, $dc, 2) | Out-Null
            $g.ReleaseHdc($dc)
            $g.Dispose()
            $dir = Split-Path $OutPath
            if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
            $bmp.Save($OutPath)
            $bmp.Dispose()
            Write-Host "CAPTURED:$OutPath ($w x $h)"
            exit 0
        }
    }
    Start-Sleep -Seconds $interval
    $elapsed += $interval
}
Write-Host "TIMEOUT: OmniTune window not found after ${maxWait}s"
exit 1
