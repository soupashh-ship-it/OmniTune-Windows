
Get-Process java,javaw -ErrorAction SilentlyContinue | Sort-Object WorkingSet64 -Descending | ForEach-Object {
    Write-Host "PID=$($_.Id) Name=$($_.ProcessName) CPU=$($_.CPU) Mem=$([Math]::Round($_.WorkingSet64/1MB))MB Handles=$($_.HandleCount) WinHandle=$($_.MainWindowHandle) Title='$($_.MainWindowTitle)' StartTime=$($_.StartTime)"
}
