param(
    [int]$width,
    [int]$height,
    [string]$filename
)
Add-Type @"
  using System;
  using System.Runtime.InteropServices;
  public class Win32 {
    [DllImport("user32.dll")]
    [return: MarshalAs(UnmanagedType.Bool)]
    public static extern bool MoveWindow(IntPtr hWnd, int X, int Y, int nWidth, int nHeight, bool bRepaint);
    [DllImport("user32.dll")]
    [return: MarshalAs(UnmanagedType.Bool)]
    public static extern bool SetForegroundWindow(IntPtr hWnd);
  }
"@
$process = Get-Process java | Where-Object {$_.MainWindowTitle -like '*OmniTune*'} | Select-Object -First 1
if ($process) {
    [Win32]::SetForegroundWindow($process.MainWindowHandle)
    Start-Sleep -Seconds 1
    [Win32]::MoveWindow($process.MainWindowHandle, 0, 0, $width, $height, $true)
    Start-Sleep -Seconds 2
    Add-Type -AssemblyName System.Windows.Forms
    Add-Type -AssemblyName System.Drawing
    $bounds = [System.Drawing.Rectangle]::FromLTRB(0, 0, $width, $height)
    $bmp = New-Object System.Drawing.Bitmap $width, $height
    $graphics = [System.Drawing.Graphics]::FromImage($bmp)
    $graphics.CopyFromScreen($bounds.Location, [System.Drawing.Point]::Empty, $bounds.Size)
    $bmp.Save("D:\Omnitune Windoww\docs\qa\$filename", [System.Drawing.Imaging.ImageFormat]::Png)
    $graphics.Dispose()
    $bmp.Dispose()
    Write-Host "Captured $width x $height to $filename"
} else {
    Write-Host "Window not found."
}
