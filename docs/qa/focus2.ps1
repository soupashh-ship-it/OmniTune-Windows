
Add-Type @"
using System;
using System.Runtime.InteropServices;
public class WinAPI2 {
    [DllImport("user32.dll")]
    public static extern bool SetForegroundWindow(IntPtr hWnd);
    [DllImport("user32.dll")]
    public static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);
    [DllImport("user32.dll")]
    public static extern IntPtr GetForegroundWindow();
}
"@

$procs = Get-Process java -ErrorAction SilentlyContinue
Write-Host "Found $($procs.Count) java processes"
foreach ($p in $procs) {
    Write-Host "PID=$($p.Id) Handle=$($p.MainWindowHandle) Title='$($p.MainWindowTitle)' Mem=$([Math]::Round($p.WorkingSet64/1MB))MB"
    if ($p.MainWindowHandle -ne 0) {
        [WinAPI2]::ShowWindow($p.MainWindowHandle, 9)
        [WinAPI2]::SetForegroundWindow($p.MainWindowHandle)
        Write-Host "Focused PID=$($p.Id)"
    }
}
