$ErrorActionPreference = "Stop"
$envFile = Join-Path $PSScriptRoot ".env"
if (-not (Test-Path $envFile)) { throw "Missing $envFile" }
Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -and -not $line.StartsWith("#")) {
        $parts = $line -split "=", 2
        if ($parts.Count -eq 2) { [Environment]::SetEnvironmentVariable($parts[0].Trim(), $parts[1].Trim(), "Process") }
    }
}
$port = if ($env:SERVER_PORT) { [int]$env:SERVER_PORT } else { 8081 }
try {
    $health = Invoke-RestMethod -Uri "http://localhost:$port/actuator/health" -TimeoutSec 3
    if ($health.status -eq "UP") { Write-Host "AI service is already healthy at http://localhost:$port" -ForegroundColor Green; exit 0 }
} catch {
    $listener = Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue
    if ($listener) { Write-Error "Port $port is occupied by PID $($listener[0].OwningProcess), but AI health is unavailable."; exit 1 }
}
Write-Host "Starting AI forecasting service at http://localhost:$port ..." -ForegroundColor Green
& "$PSScriptRoot\mvnw.cmd" spring-boot:run
exit $LASTEXITCODE