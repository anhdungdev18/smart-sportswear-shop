# Start Spring Boot on Windows with backend/.env.local overriding backend/.env.
$jdk21 = Join-Path $PSScriptRoot "..\.tools\temurin21\jdk-21.0.11+10"
if (Test-Path (Join-Path $jdk21 "bin\java.exe")) {
    $env:JAVA_HOME = (Resolve-Path $jdk21).Path
    $env:Path = "$env:JAVA_HOME\bin;$env:Path"
}

$envFiles = @(
    (Join-Path $PSScriptRoot "backend\.env"),
    (Join-Path $PSScriptRoot "backend\.env.local")
)

foreach ($envFile in $envFiles) {
    if (Test-Path $envFile) {
        Get-Content $envFile | ForEach-Object {
            if ($_ -match '^([^#=][^=]*)=(.*)$') {
                [System.Environment]::SetEnvironmentVariable(
                    $Matches[1].Trim(),
                    $Matches[2].Trim(),
                    'Process'
                )
            }
        }
    }
}

Write-Host "DB: $env:DB_HOST`:$env:DB_PORT/$env:DB_NAME" -ForegroundColor Cyan
Write-Host "Redis: $env:REDIS_HOST`:$env:REDIS_PORT" -ForegroundColor Cyan
Write-Host "Java: $(& java -version 2>&1 | Select-Object -First 1)" -ForegroundColor Cyan
Write-Host "Starting backend at http://localhost:$env:SERVER_PORT ..." -ForegroundColor Green

Set-Location (Join-Path $PSScriptRoot "backend")
& .\mvnw.cmd spring-boot:run
