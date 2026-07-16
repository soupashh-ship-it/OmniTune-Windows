param(
    [string]$SignTool = $env:OMNITUNE_SIGNTOOL,
    [string]$CertificatePath = $env:OMNITUNE_SIGN_CERT_PATH,
    [string]$CertificatePassword = $env:OMNITUNE_SIGN_CERT_PASSWORD,
    [string]$TimestampUrl = $env:OMNITUNE_TIMESTAMP_URL
)

$ErrorActionPreference = "Stop"

function Find-SignTool {
    param([string]$ExplicitPath)

    if ($ExplicitPath) {
        if (!(Test-Path -LiteralPath $ExplicitPath)) {
            throw "OMNITUNE_SIGNTOOL is set but does not exist: $ExplicitPath"
        }
        return (Resolve-Path -LiteralPath $ExplicitPath).Path
    }

    $command = Get-Command "signtool.exe" -ErrorAction SilentlyContinue
    if ($command) { return $command.Source }

    $kitRoot = "C:\Program Files (x86)\Windows Kits\10\bin"
    $found = Get-ChildItem $kitRoot -Recurse -Filter "signtool.exe" -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName -match "\\x64\\signtool.exe$" } |
        Sort-Object FullName -Descending |
        Select-Object -First 1
    if ($found) { return $found.FullName }

    throw "signtool.exe was not found. Install the Windows SDK or set OMNITUNE_SIGNTOOL."
}

$resolvedSignTool = Find-SignTool $SignTool

if ([string]::IsNullOrWhiteSpace($CertificatePath)) {
    throw "OMNITUNE_SIGN_CERT_PATH is not set. A real .pfx code-signing certificate is required for public signing."
}

if (!(Test-Path -LiteralPath $CertificatePath)) {
    throw "Signing certificate not found: $CertificatePath"
}

try {
    $cert = New-Object System.Security.Cryptography.X509Certificates.X509Certificate2
    if ([string]::IsNullOrEmpty($CertificatePassword)) {
        $cert.Import($CertificatePath)
    } else {
        $cert.Import($CertificatePath, $CertificatePassword, [System.Security.Cryptography.X509Certificates.X509KeyStorageFlags]::EphemeralKeySet)
    }
    if (-not $cert.HasPrivateKey) {
        throw "Certificate does not contain a private key."
    }
    if ($cert.NotAfter -lt (Get-Date)) {
        throw "Certificate is expired: $($cert.NotAfter)"
    }
} catch {
    throw "Could not load signing certificate: $($_.Exception.Message)"
}

[pscustomobject]@{
    Result = "PASS"
    SignTool = $resolvedSignTool
    Certificate = $CertificatePath
    CertificateSubject = $cert.Subject
    CertificateExpires = $cert.NotAfter
    TimestampUrl = if ($TimestampUrl) { $TimestampUrl } else { "http://timestamp.digicert.com" }
} | Format-List
