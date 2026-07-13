$maxWait = 90
$interval = 3
$elapsed = 0
while ($elapsed -lt $maxWait) {
    $p = Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle -like "*OmniTune*" } | Select-Object -First 1
    if ($p -and $p.MainWindowHandle -ne 0) {
        Write-Host "FOUND"
        exit 0
    }
    Start-Sleep -Seconds $interval
    $elapsed += $interval
}
exit 1
