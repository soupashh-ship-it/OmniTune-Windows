
Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing
$bounds = [System.Windows.Forms.Screen]::PrimaryScreen.Bounds
$bitmap = New-Object System.Drawing.Bitmap($bounds.Width, $bounds.Height)
$g = [System.Drawing.Graphics]::FromImage($bitmap)
$g.CopyFromScreen($bounds.X, $bounds.Y, 0, 0, $bounds.Size)
$bitmap.Save('D:\Omnitune Windoww\docs\qa\screenshot-phase7.png')
$g.Dispose()
$bitmap.Dispose()
Write-Host "OK: $($bounds.Width)x$($bounds.Height)"
