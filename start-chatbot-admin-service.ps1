$ErrorActionPreference = "Stop"

$serviceDir = Join-Path $PSScriptRoot "chatbot-admin-service"
$venvPython = Join-Path $serviceDir ".venv\Scripts\python.exe"

Set-Location $serviceDir

if (-not (Test-Path $venvPython)) {
    Write-Host "Creating chatbot-admin-service virtual environment..." -ForegroundColor Yellow
    python -m venv .venv
}

Write-Host "Installing chatbot-admin-service dependencies..." -ForegroundColor Yellow
& $venvPython -m pip install -r requirements.txt
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Chatbot Admin Service: http://localhost:8003" -ForegroundColor Green
& $venvPython run.py
exit $LASTEXITCODE
