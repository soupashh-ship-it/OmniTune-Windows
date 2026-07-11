
$procs = Get-Process java -ErrorAction SilentlyContinue
if ($procs) {
    foreach ($p in $procs) {
        Write-Host "java PID=$($p.Id) CPU=$($p.CPU) Mem=$([Math]::Round($p.WorkingSet/1MB))MB Window='$($p.MainWindowTitle)'"
    }
} else {
    Write-Host "NO_JAVA"
}
