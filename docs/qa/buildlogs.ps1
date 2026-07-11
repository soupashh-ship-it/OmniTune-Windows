
$buildDir = "D:\Omnitune Windoww\build"
if (Test-Path $buildDir) {
    Get-ChildItem $buildDir -Recurse -Filter "*.log" | Sort-Object LastWriteTime -Descending | Select-Object -First 5 | ForEach-Object {
        Write-Host "BUILD LOG: $($_.FullName) $([Math]::Round($_.Length/1KB))KB $($_.LastWriteTime)"
    }
}
