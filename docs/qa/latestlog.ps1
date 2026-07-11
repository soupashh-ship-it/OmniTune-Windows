
$gradleDir = "$env:USERPROFILE\.gradle\daemon\9.4.1"
# Find the most recently modified log
$logs = Get-ChildItem $gradleDir -Filter "*.log" | Sort-Object LastWriteTime -Descending
$latest = $logs | Select-Object -First 1
Write-Host "Latest log: $($latest.Name) modified $($latest.LastWriteTime)"
# Get last 30 lines
Get-Content $latest.FullName -Tail 30
