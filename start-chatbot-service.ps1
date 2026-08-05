$ErrorActionPreference = "Stop"

$serviceDir = Join-Path $PSScriptRoot "chatbot-service"
$pythonExe = Join-Path $serviceDir ".venv\Scripts\python.exe"

if (-not (Test-Path -LiteralPath $pythonExe -PathType Leaf)) {
    throw "Missing chatbot-service virtual environment at $pythonExe"
}

Set-Location $serviceDir
$env:PYTHONUTF8 = "1"
Write-Host "Chatbot Service: http://localhost:8002" -ForegroundColor Green
& $pythonExe run.py
exit $LASTEXITCODE
