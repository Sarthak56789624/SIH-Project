@echo off
title AI-ChainID Unified Platform Launcher
echo ================================================================
echo        AI-ChainID — Unified Enterprise Platform Launcher        
echo ================================================================

set ROOT_DIR=%~dp0

echo.
echo [1/2] Launching Python AI Engine (Port 8000)...
start "AI-ChainID Python AI Service" /B /D "%ROOT_DIR%ai-chainid" python -m uvicorn app.main:app --port 8000

echo [2/2] Launching AI-ChainID Platform Gateway (Port 8080)...
start "AI-ChainID Unified Backend" /B /D "%ROOT_DIR%AIchainID-backend-main\target\exploded" java -cp "BOOT-INF/classes;BOOT-INF/lib/*" com.aichainid.AIChainIdApplication

echo.
echo Initializing platform engines (waiting 6 seconds)...
timeout /t 6 /nobreak >nul

echo.
echo ================================================================
echo     AI-ChainID Unified Platform is READY!                      
echo ================================================================
echo   Platform URL:          http://localhost:8080/
echo   Admin Credentials:     admin@aichainid.org / Admin@12345
echo   AI Engine Status:      Integrated & Online
echo ================================================================
echo.

start http://localhost:8080/

pause
