
$gradleDir = "$env:USERPROFILE\.gradle\daemon"
if (Test-Path $gradleDir) {
    Get-ChildItem $gradleDir -Recurse -Filter "*.log" | Sort-Object LastWriteTime -Descending | Select-Object -First 3 | ForEach-Object {
        Write-Host "LOG: $($_.FullName) Size=$([Math]::Round($_.Length/1KB))KB Modified=$($_.LastWriteTime)"
        Get-Content $_.FullName -Tail 10 | ForEach-Object { Write-Host "  $_" }
    }
} else {
    Write-Host "No gradle daemon dir"
}
