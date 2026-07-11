
Add-Type @"
using System;
using System.Runtime.InteropServices;
public class MouseClick {
    [DllImport("user32.dll")] public static extern bool SetCursorPos(int X, int Y);
    [DllImport("user32.dll")] public static extern void mouse_event(uint dwFlags, uint dx, uint dy, uint cButtons, uint dwExtraInfo);
    public const uint MOUSEEVENTF_LEFTDOWN = 0x02;
    public const uint MOUSEEVENTF_LEFTUP = 0x04;
    public static void Click(int x, int y) {
        SetCursorPos(x, y);
        mouse_event(MOUSEEVENTF_LEFTDOWN, 0, 0, 0, 0);
        System.Threading.Thread.Sleep(80);
        mouse_event(MOUSEEVENTF_LEFTUP, 0, 0, 0, 0);
    }
}
"@
Add-Type @"
using System;
using System.Runtime.InteropServices;
public class Win32Focus2 {
    [DllImport("user32.dll")] public static extern bool SetForegroundWindow(IntPtr hWnd);
    [DllImport("user32.dll")] public static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);
    [DllImport("user32.dll")] public static extern bool GetWindowRect(IntPtr hWnd, out RECT2 lpRect);
    [StructLayout(LayoutKind.Sequential)]
    public struct RECT2 { public int Left, Top, Right, Bottom; }
}
"@

$proc = Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle -like "*OmniTune*" } | Select-Object -First 1
if ($proc) {
    $hwnd = [IntPtr]$proc.MainWindowHandle
    [Win32Focus2]::ShowWindow($hwnd, 9)
    [Win32Focus2]::SetForegroundWindow($hwnd)
    Start-Sleep -Milliseconds 800
    
    $rect = New-Object Win32Focus2+RECT2
    [Win32Focus2]::GetWindowRect($hwnd, [ref]$rect)
    
    $winX = $rect.Left
    $winY = $rect.Top
    $winW = $rect.Right - $rect.Left
    $winH = $rect.Bottom - $rect.Top
    
    Write-Host "Window at ${winX},${winY} size ${winW}x${winH}"
    
    # Click "Songs" in sidebar (approximately x=135, y=300 relative to window)
    $clickX = $winX + 135
    $clickY = $winY + 300
    Write-Host "Clicking Songs at ${clickX},${clickY}"
    [MouseClick]::Click($clickX, $clickY)
    Start-Sleep -Milliseconds 1000
    
    # Double-click first song in list (approximately x=700, y=350 relative to window)  
    $songX = $winX + 700
    $songY = $winY + 350
    Write-Host "Double-clicking first song at ${songX},${songY}"
    [MouseClick]::Click($songX, $songY)
    Start-Sleep -Milliseconds 100
    [MouseClick]::Click($songX, $songY)
    Start-Sleep -Milliseconds 2000
    
    Write-Host "Done"
} else {
    Write-Host "OmniTune not found"
}
