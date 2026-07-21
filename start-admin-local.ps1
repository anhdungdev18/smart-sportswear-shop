# Run the admin app with the project-local Node.js 24 distribution.
$nodeDir = Join-Path $PSScriptRoot ".tools\node-v24.14.1-win-x64"
$nodeExe = Join-Path $nodeDir "node.exe"

if (-not (Test-Path $nodeExe)) {
    throw "Project-local Node.js 24 was not found at $nodeDir"
}

$env:Path = "$nodeDir;$env:Path"
$appDir = Join-Path $PSScriptRoot "frontend\admin"
Set-Location $appDir

Write-Host "Node: $(& node --version)" -ForegroundColor Cyan
Write-Host "Admin: http://localhost:3001" -ForegroundColor Green

if (-not (Test-Path (Join-Path $appDir "node_modules\next\package.json"))) {
    Write-Host "Installing admin dependencies..." -ForegroundColor Yellow
    & npm.cmd ci
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

& npm.cmd run dev
