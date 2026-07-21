# Script ki?m tra tru?c bu?i Demo (Pre-flight check)
$ErrorActionPreference = "Stop"

Write-Host "B?t d?u ki?m tra h? th?ng AI Replenishment..." -ForegroundColor Cyan

function Test-Endpoint {
    param([string]$name, [string]$url)
    try {
        $response = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 5
        if ($response.status -eq "UP" -or $response) {
            Write-Host "[PASS] $name dang ch?y ($url)" -ForegroundColor Green
        } else {
            Write-Host "[FAIL] $name ph?n h?i nhung tr?ng thái không dúng" -ForegroundColor Red
        }
    } catch {
        Write-Host "[FAIL] $name không ph?n h?i ($url)" -ForegroundColor Red
    }
}

Test-Endpoint "Core Backend Health" "http://localhost:8082/actuator/health"
Test-Endpoint "AI Backend Health" "http://localhost:8081/actuator/health"

try {
    $adminResponse = Invoke-WebRequest -Uri "http://localhost:3001" -Method Get -TimeoutSec 5 -UseBasicParsing
    Write-Host "[PASS] Admin Frontend dang ch?y" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Admin Frontend không ph?n h?i" -ForegroundColor Red
}

Write-Host "`nÐ? ki?m tra d? li?u database (Product snapshot > 0, Policy > 0...), vui lòng ch?y các l?nh SQL sau trên Supabase SQL Editor:" -ForegroundColor Yellow
Write-Host "SELECT count(*) as product_snapshot FROM ai_product_variant_snapshot;"
Write-Host "SELECT count(*) as sales_snapshot FROM ai_sales_daily_snapshot;"
Write-Host "SELECT count(*) as policies FROM inventory_policies;"
Write-Host "SELECT count(*) as forecast_runs FROM forecast_runs;"
Write-Host "SELECT count(*) as recommendations FROM replenishment_recommendations;"
Write-Host "SELECT count(*) as duplicate_pending FROM replenishment_recommendations WHERE status = 'PENDING' GROUP BY variant_id HAVING count(*) > 1;"
Write-Host "SELECT count(*) as negative_forecast FROM forecast_runs WHERE forecast_quantity < 0;"

Write-Host "`nPre-flight check hoàn t?t!" -ForegroundColor Cyan

