@echo off
REM ========================================
REM SpringCloud Mall - Complete Startup Script
REM ========================================
setlocal enabledelayedexpansion

echo ========================================
echo SpringCloud Mall - Complete Startup
echo ========================================
echo.

REM Set paths
set "PROJECT_DIR=%~dp0"
set "BACKEND_DIR=%PROJECT_DIR%backend"
set "FRONTEND_DIR=%PROJECT_DIR%frontend"
set "LOGS_DIR=%PROJECT_DIR%logs"

REM Create logs directory
if not exist "%LOGS_DIR%" mkdir "%LOGS_DIR%"

REM ============================================
REM Step 1: Check Docker
REM ============================================
echo [Step 1/5] Checking Docker...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Docker not found!
    echo.
    echo Solution:
    echo   1. Install Docker Desktop: https://www.docker.com/products/docker-desktop
    echo   2. Start Docker Desktop
    echo   3. Run this script again
    echo.
    echo 💡 Tip: Run diagnose.bat for complete diagnosis
    echo.
    pause
    exit /b 1
)
echo [OK] Docker is available
echo.

REM ============================================
REM Step 2: Start Infrastructure
REM ============================================
echo [Step 2/5] Starting infrastructure (MySQL, Redis, Nacos)...
docker compose -f "%PROJECT_DIR%docker-compose.yml" up -d
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Failed to start infrastructure!
    echo.
    echo Possible causes:
    echo   1. Docker Desktop is not running
    echo   2. Ports are occupied (3307, 6379, 8848)
    echo   3. docker-compose.yml configuration error
    echo.
    echo 💡 Suggestion: Run diagnose.bat for detailed diagnosis
    echo.
    pause
    exit /b 1
)
echo [OK] Infrastructure started
echo.

echo Waiting for infrastructure to be ready (30 seconds)...
timeout /t 30 /nobreak >nul
echo.

REM ============================================
REM Step 3: Check Maven
REM ============================================
echo [Step 3/5] Checking Maven...
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARN] Maven not found!
    echo Please start backend services manually using IDE.
    set "MAVEN_OK=0"
) else (
    echo [OK] Maven is available
    set "MAVEN_OK=1"
)
echo.

REM ============================================
REM Step 4: Start Backend Services
REM ============================================
if "%MAVEN_OK%"=="1" (
    echo [Step 4/5] Starting backend services...
    echo.
    
    echo Starting Gateway Service (8080)...
    start "Gateway" cmd /k "cd /d %BACKEND_DIR%\gateway-service && mvn spring-boot:run -Dspring-boot.run.profiles=simple"
    timeout /t 5 /nobreak >nul
    
    echo Starting User Service (8082)...
    start "User" cmd /k "cd /d %BACKEND_DIR%\user-service && mvn spring-boot:run"
    timeout /t 3 /nobreak >nul
    
    echo Starting Product Service (8083)...
    start "Product" cmd /k "cd /d %BACKEND_DIR%\product-service && mvn spring-boot:run"
    timeout /t 3 /nobreak >nul
    
    echo Starting Cart Service (8088)...
    start "Cart" cmd /k "cd /d %BACKEND_DIR%\cart-service && mvn spring-boot:run"
    timeout /t 3 /nobreak >nul
    
    echo Starting Order Service (8084)...
    start "Order" cmd /k "cd /d %BACKEND_DIR%\order-service && mvn spring-boot:run"
    timeout /t 3 /nobreak >nul
    
    echo Starting Payment Service (8085)...
    start "Payment" cmd /k "cd /d %BACKEND_DIR%\payment-service && mvn spring-boot:run"
    timeout /t 3 /nobreak >nul
    
    echo Starting Merchant Service (8087)...
    start "Merchant" cmd /k "cd /d %BACKEND_DIR%\merchant-service && mvn spring-boot:run"
    timeout /t 3 /nobreak >nul
    
    echo Starting Admin Service (8086)...
    start "Admin" cmd /k "cd /d %BACKEND_DIR%\admin-service && mvn spring-boot:run"
    timeout /t 3 /nobreak >nul
    
    echo Starting SMS Service (8089)...
    start "SMS" cmd /k "cd /d %BACKEND_DIR%\sms-service && mvn spring-boot:run"
    
    echo.
    echo [OK] All backend services started
) else (
    echo [Step 4/5] Skipping backend services (Maven not available)
)
echo.

REM ============================================
REM Step 5: Start Frontend
REM ============================================
echo [Step 5/5] Starting frontend...
if exist "%FRONTEND_DIR%\package.json" (
    cd /d "%FRONTEND_DIR%"
    if not exist "node_modules" (
        echo Installing dependencies (first time)...
        call npm install
    )
    start "Frontend" cmd /k "npm run dev"
    cd /d "%PROJECT_DIR%"
    echo [OK] Frontend started
) else (
    echo [WARN] Frontend not found
)
echo.

REM ============================================
REM Startup Complete
REM ============================================
echo ========================================
echo Startup Complete!
echo ========================================
echo.
echo Access URLs:
echo   Frontend:  http://localhost:5173
echo   Gateway:   http://localhost:8080
echo   Nacos:     http://localhost:8848/nacos
echo.
echo Login:
echo   Frontend:  testlogin / nacos
echo   Nacos:     nacos / nacos
echo.
echo Database:
echo   MySQL:     localhost:3307 (root/123456)
echo   Redis:     localhost:6379
echo.
echo Management:
echo   Check status:  pwsh -File check-services-silent.ps1
echo   View logs:     logs\ directory
echo   Diagnose:      diagnose.bat
echo.
pause
exit /b 0

