param(
  [string]$Route,
  [string]$FileName,
  [string]$Theme = "",
  [string]$PlaylistState = "",
  [int]$WaitSeconds = 35,
  [int]$Width = 1672,
  [int]$Height = 941,
  [int]$PostWindowSeconds = 8
)
$project = 'D:\Omnitune Windoww'
Add-Type -AssemblyName System.Drawing
Add-Type @"
using System;
using System.Drawing;
using System.Runtime.InteropServices;
public class RouteCaptureWin32 {
  [DllImport("user32.dll")] public static extern bool MoveWindow(IntPtr hWnd, int X, int Y, int nWidth, int nHeight, bool bRepaint);
  [DllImport("user32.dll")] public static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);
  [DllImport("user32.dll")] public static extern bool SetForegroundWindow(IntPtr hWnd);
  [DllImport("user32.dll")] public static extern bool GetWindowRect(IntPtr h, out RECT r);
  [DllImport("user32.dll")] public static extern bool PrintWindow(IntPtr h, IntPtr dc, uint f);
  [StructLayout(LayoutKind.Sequential)] public struct RECT { public int L,T,R,B; }
}
"@
Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle -like '*OmniTune*' } | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 1
$log = Join-Path $project "docs\qa\qa-route-$Route.log"
$err = Join-Path $project "docs\qa\qa-route-$Route.err.log"
$psi = [System.Diagnostics.ProcessStartInfo]::new()
$psi.FileName = Join-Path $project 'gradlew.bat'
$psi.Arguments = ':composeApp:run'
$psi.WorkingDirectory = $project
$psi.UseShellExecute = $false
$psi.RedirectStandardOutput = $true
$psi.RedirectStandardError = $true
$psi.CreateNoWindow = $true
$psi.Environment['OMNITUNE_QA_ROUTE'] = $Route
if ($Theme -ne "") { $psi.Environment['OMNITUNE_QA_THEME'] = $Theme }
if ($PlaylistState -ne "") { $psi.Environment['OMNITUNE_QA_PLAYLIST_STATE'] = $PlaylistState }
$p = [System.Diagnostics.Process]::new()
$p.StartInfo = $psi
[void]$p.Start()
$stdoutTask = $p.StandardOutput.ReadToEndAsync()
$stderrTask = $p.StandardError.ReadToEndAsync()
$deadline = (Get-Date).AddSeconds($WaitSeconds)
$window = $null
while ((Get-Date) -lt $deadline) {
  $window = Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle -like '*OmniTune*' } | Select-Object -First 1
  if ($window -and $window.MainWindowHandle -ne 0) { break }
  Start-Sleep -Seconds 1
}
if (-not $window) { Write-Host "NO_WINDOW:$Route"; exit 2 }
Start-Sleep -Seconds $PostWindowSeconds
$hw = [IntPtr]$window.MainWindowHandle
[RouteCaptureWin32]::ShowWindow($hw, 9) | Out-Null
[RouteCaptureWin32]::MoveWindow($hw, 0, 0, $Width, $Height, $true) | Out-Null
[RouteCaptureWin32]::SetForegroundWindow($hw) | Out-Null
Start-Sleep -Seconds 2
$r = New-Object RouteCaptureWin32+RECT
[RouteCaptureWin32]::GetWindowRect($hw, [ref]$r) | Out-Null
$w = $r.R - $r.L
$h = $r.B - $r.T
if ($w -lt 100 -or $h -lt 100) { Write-Host "TOO_SMALL:$Route"; exit 3 }
$outPath = Join-Path $project "docs\qa\$FileName"
$dir = Split-Path $outPath
if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
$bmp = New-Object System.Drawing.Bitmap($w, $h)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$dc = $g.GetHdc()
[RouteCaptureWin32]::PrintWindow($hw, $dc, 2) | Out-Null
$g.ReleaseHdc($dc)
$g.Dispose()
$bmp.Save($outPath)
$bmp.Dispose()
Write-Host "CAPTURED:$outPath ($w x $h)"
Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle -like '*OmniTune*' } | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 1
try { $stdoutTask.Result | Set-Content -Path $log -Encoding UTF8 } catch {}
try { $stderrTask.Result | Set-Content -Path $err -Encoding UTF8 } catch {}
Write-Host "DONE:${Route}:${FileName}"
