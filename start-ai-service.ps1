$ErrorActionPreference = "Stop"

$envFile = Join-Path $PSScriptRoot "ai_forecasting_service\.env"
if (-not (Test-Path $envFile)) { throw "Missing $envFile" }

Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -and -not $line.StartsWith("#")) {
        $parts = $line -split "=", 2
        if ($parts.Count -eq 2) {
            [Environment]::SetEnvironmentVariable($parts[0].Trim(), $parts[1].Trim(), "Process")
        }
    }
}

$port = if ($env:SERVER_PORT) { [int]$env:SERVER_PORT } else { 8080 }
$healthUrl = "http://localhost:$port/actuator/health"
try {
    $health = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 3
    if ($health.status -eq "UP") {
        Write-Host "ai_forecasting_service is already running and healthy at http://localhost:$port" -ForegroundColor Green
        exit 0
    }
} catch {
    $listener = Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue
    if ($listener) {
        Write-Error "Port $port is occupied by PID $($listener[0].OwningProcess), but ai_forecasting_service health is unavailable."
        exit 1
    }
}

Write-Host "DB: $env:DB_HOST`:$env:DB_PORT/$env:DB_NAME" -ForegroundColor Cyan
Write-Host "Starting ai_forecasting_service at http://localhost:$port ..." -ForegroundColor Green
Set-Location (Join-Path $PSScriptRoot "ai_forecasting_service")
& .\mvnw.cmd spring-boot:run
exit $LASTEXITCODE
