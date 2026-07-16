param(
    [Parameter(Mandatory = $true)]
    [string]$Path,

    [switch]$RequireValid
)

$ErrorActionPreference = "Stop"

if (!(Test-Path -LiteralPath $Path)) {
    throw "Installer path does not exist: $Path"
}

$signature = Get-AuthenticodeSignature -LiteralPath $Path
$signature | Format-List Status, StatusMessage, SignerCertificate, TimeStamperCertificate, Path

if ($RequireValid -and $signature.Status -ne "Valid") {
    throw "Installer is not signed with a valid Authenticode signature: $($signature.Status)"
}

