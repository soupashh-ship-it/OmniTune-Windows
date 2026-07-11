
$wshell = New-Object -com WScript.Shell
$wshell.AppActivate('OmniTune')
Start-Sleep -Milliseconds 800
$wshell.SendKeys(' ')
Start-Sleep -Milliseconds 500
Write-Host "Spacebar sent to OmniTune"
