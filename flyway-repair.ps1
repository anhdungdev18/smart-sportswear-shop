# flyway-repair.ps1 - Fix Flyway checksum mismatch on Supabase cloud DB
# Run this once when you see: "Migration checksum mismatch for migration version X"

Write-Host ""
Write-Host "======================================" -ForegroundColor Yellow
Write-Host "  FLYWAY REPAIR - Supabase Cloud" -ForegroundColor Yellow
Write-Host "======================================" -ForegroundColor Yellow
Write-Host ""

# --- Java: use project-local JDK 21 if available ---
$jdk21 = Join-Path $PSScriptRoot ".tools\temurin21\jdk-21.0.11+10"
if (Test-Path (Join-Path $jdk21 "bin\java.exe")) {
    $env:JAVA_HOME = (Resolve-Path $jdk21).Path
    $env:Path = "$env:JAVA_HOME\bin;$env:Path"
}

# --- Load ONLY backend\.env (Supabase cloud) ---
$envFile = Join-Path $PSScriptRoot "backend\.env"
if (-not (Test-Path $envFile)) {
    Write-Host "[ERROR] File not found: $envFile" -ForegroundColor Red
    exit 1
}

Get-Content $envFile | ForEach-Object {
    if ($_ -match '^([^#=\s][^=]*)=(.*)$') {
        [System.Environment]::SetEnvironmentVariable(
            $Matches[1].Trim(),
            $Matches[2].Trim(),
            'Process'
        )
    }
}

Write-Host "[DB] $env:DB_HOST`:$env:DB_PORT/$env:DB_NAME" -ForegroundColor Cyan
Write-Host ""
Write-Host "[INFO] Running flyway:repair to sync checksums..." -ForegroundColor Green
Write-Host ""

Set-Location (Join-Path $PSScriptRoot "backend")
& .\mvnw.cmd flyway:repair `
    "-Dflyway.url=jdbc:postgresql://$env:DB_HOST`:$env:DB_PORT/$env:DB_NAME$env:DB_PARAMS" `
    "-Dflyway.user=$env:DB_USERNAME" `
    "-Dflyway.password=$env:DB_PASSWORD" `
    "-Dflyway.locations=classpath:db/migration"

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "[OK] Repair complete! Now run .\start-backend.ps1" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "[ERROR] Repair failed. Check the output above." -ForegroundColor Red
}
