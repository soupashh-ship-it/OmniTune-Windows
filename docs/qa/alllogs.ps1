
$gradleDir = "$env:USERPROFILE\.gradle\daemon\9.4.1"
Get-ChildItem $gradleDir -Filter "*.log" | ForEach-Object {
    Write-Host "$($_.Name) Size=$([Math]::Round($_.Length/1KB))KB Modified=$($_.LastWriteTime)"
}
