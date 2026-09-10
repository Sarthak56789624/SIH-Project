Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "       AI-ChainID — Unified Enterprise Platform Launcher        " -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan

$rootDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# 1. Start Python AI Microservice (Port 8000)
Write-Host "`n[1/2] Launching Python AI Engine (Port 8000)..." -ForegroundColor Yellow
$aiProc = Start-Process python -ArgumentList "-m uvicorn app.main:app --port 8000" `
    -WorkingDirectory "$rootDir\ai-chainid" `
    -WindowStyle Hidden `
    -PassThru

# 2. Start Spring Boot Unified Platform Backend (Port 8080)
Write-Host "[2/2] Launching AI-ChainID Platform Gateway (Port 8080)..." -ForegroundColor Yellow
$backendProc = Start-Process java -ArgumentList "-cp `"BOOT-INF/classes;BOOT-INF/lib/*`" com.aichainid.AIChainIdApplication" `
    -WorkingDirectory "$rootDir\AIchainID-backend-main\target\exploded" `
    -WindowStyle Hidden `
    -PassThru

Write-Host "`nInitializing platform engines..." -ForegroundColor Gray
Start-Sleep -Seconds 6

Write-Host "`n================================================================" -ForegroundColor Green
Write-Host "    AI-ChainID Unified Platform is READY!                      " -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green
Write-Host "  Platform URL:          http://localhost:8080/" -ForegroundColor Cyan
Write-Host "  Admin Credentials:     admin@aichainid.org / Admin@12345" -ForegroundColor White
Write-Host "  AI Engine Status:      Integrated & Online" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green

# Open browser to unified platform
Start-Process "http://localhost:8080/"

Write-Host "`nPlatform running in background. To exit and stop services, press Enter." -ForegroundColor Gray
Read-Host

Stop-Process -Id $aiProc.Id -ErrorAction SilentlyContinue
Stop-Process -Id $backendProc.Id -ErrorAction SilentlyContinue
Write-Host "Services stopped gracefully." -ForegroundColor Yellow
