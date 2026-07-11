
$p = Get-Process -Id 18772 -ErrorAction SilentlyContinue
if ($p) {
    Write-Host "PID=18772 CPU=$($p.CPU) Threads=$($p.Threads.Count) Handle=$($p.MainWindowHandle)"
} else {
    # Check all java
    Get-Process java -ErrorAction SilentlyContinue | ForEach-Object {
        Write-Host "PID=$($_.Id) CPU=$($_.CPU) Mem=$([Math]::Round($_.WorkingSet64/1MB))MB Handle=$($_.MainWindowHandle)"
    }
}
