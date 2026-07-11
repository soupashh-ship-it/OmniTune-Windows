
Add-Type @"
using System;
using System.Runtime.InteropServices;
public class WinAPI {
    [DllImport("user32.dll")]
    public static extern bool SetForegroundWindow(IntPtr hWnd);
    [DllImport("user32.dll")]
    public static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);
    [DllImport("user32.dll")]
    public static extern bool IsIconic(IntPtr hWnd);
}
"@

$procs = Get-Process java -ErrorAction SilentlyContinue
foreach ($p in $procs) {
    if ($p.MainWindowHandle -ne 0) {
        Write-Host "Found java window: $($p.Id) - $($p.MainWindowTitle)"
        [WinAPI]::ShowWindow($p.MainWindowHandle, 9)  # SW_RESTORE
        [WinAPI]::SetForegroundWindow($p.MainWindowHandle)
    }
}
