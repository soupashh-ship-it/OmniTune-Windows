
Add-Type @"
using System;
using System.Runtime.InteropServices;
public class Win32Focus {
    [DllImport("user32.dll")] public static extern bool SetForegroundWindow(IntPtr hWnd);
    [DllImport("user32.dll")] public static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);
    [DllImport("user32.dll")] public static extern bool BringWindowToTop(IntPtr hWnd);
}
"@
$processes = Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle -like "*OmniTune*" }
if ($processes) {
    $hwnd = [IntPtr]$processes[0].MainWindowHandle
    [Win32Focus]::ShowWindow($hwnd, 9)
    [Win32Focus]::SetForegroundWindow($hwnd)
    [Win32Focus]::BringWindowToTop($hwnd)
    Start-Sleep -Milliseconds 1500
    Write-Host "Focused PID=$($processes[0].Id) Handle=$hwnd"
} else {
    Write-Host "NOT_FOUND"
}
