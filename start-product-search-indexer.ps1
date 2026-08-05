$ErrorActionPreference = "Stop"

$serviceDir = Join-Path $PSScriptRoot "chatbot-service"
$pythonExe = Join-Path $serviceDir ".venv\Scripts\python.exe"

if (-not (Test-Path -LiteralPath $pythonExe -PathType Leaf)) {
    throw "Missing chatbot-service virtual environment at $pythonExe"
}

Set-Location $serviceDir
$env:PYTHONUTF8 = "1"
Write-Host "Product search indexing worker started." -ForegroundColor Green
& $pythonExe scripts\run_product_search_indexer.py
exit $LASTEXITCODE
