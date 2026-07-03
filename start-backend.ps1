# Load .env vars and start Spring Boot backend
$envFile = Join-Path $PSScriptRoot "backend\.env"
Get-Content $envFile | ForEach-Object {
    if ($_ -match '^([^#=][^=]*)=(.*)$') {
        [System.Environment]::SetEnvironmentVariable($Matches[1].Trim(), $Matches[2].Trim(), 'Process')
    }
}

Write-Host "DB: $env:DB_HOST`:$env:DB_PORT/$env:DB_NAME" -ForegroundColor Cyan
Write-Host "Starting backend..." -ForegroundColor Green

Set-Location "$PSScriptRoot\backend"
.\mvnw.cmd spring-boot:run
