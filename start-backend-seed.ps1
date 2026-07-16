# Load .env vars and start Spring Boot backend WITH seed data (first run only)
$envFile = Join-Path $PSScriptRoot "backend\.env"
Get-Content $envFile | ForEach-Object {
    if ($_ -match '^([^#=][^=]*)=(.*)$') {
        [System.Environment]::SetEnvironmentVariable($Matches[1].Trim(), $Matches[2].Trim(), 'Process')
    }
}
$env:APP_SEED_ENABLED = "true"

Write-Host "DB: $env:DB_HOST`:$env:DB_PORT/$env:DB_NAME" -ForegroundColor Cyan
Write-Host "Starting backend WITH seed..." -ForegroundColor Yellow

Set-Location "$PSScriptRoot\backend"
.\mvnw.cmd spring-boot:run
