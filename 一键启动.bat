@echo off
setlocal enabledelayedexpansion

title SpringCloud商城 - 一键启动

echo.
echo ========================================
echo   SpringCloud商城 - 一键启动
echo ========================================
echo.

REM 设置项目路径
set "PROJECT_DIR=%~dp0"
set "BACKEND_DIR=%PROJECT_DIR%backend"
set "FRONTEND_DIR=%PROJECT_DIR%frontend"
set "LOGS_DIR=%PROJECT_DIR%logs"

REM 创建日志目录
if not exist "%LOGS_DIR%" mkdir "%LOGS_DIR%"

REM ============================================
REM 步骤1: 环境检查
REM ============================================
echo [1/4] 环境检查
echo ----------------------------------------

REM 检查 Docker
echo 检查 Docker...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [X] Docker 未安装
    echo.
    echo 请先安装 Docker Desktop: https://www.docker.com/products/docker-desktop
    goto :error
)
echo [OK] Docker 已安装

REM 检查 Docker 是否运行
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [X] Docker Desktop 未运行
    echo.
    echo 请启动 Docker Desktop 后重试
    goto :error
)
echo [OK] Docker Desktop 正在运行

REM 检查 Maven
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [!] Maven 未安装（后端服务将无法启动）
    set "MAVEN_OK=0"
) else (
    echo [OK] Maven 已安装
    set "MAVEN_OK=1"
)

REM 检查 Node.js
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [!] Node.js 未安装（前端将无法启动）
    set "NODE_OK=0"
) else (
    echo [OK] Node.js 已安装
    set "NODE_OK=1"
)

echo.

REM ============================================
REM 步骤2: 启动基础设施
REM ============================================
echo [2/4] 启动基础设施 (MySQL, Redis, Nacos)
echo ----------------------------------------

docker compose -f "%PROJECT_DIR%docker-compose-dev.yml" up -d
if %errorlevel% neq 0 (
    echo [X] 基础设施启动失败
    goto :error
)

echo [OK] 基础设施启动成功
echo.
echo 等待服务就绪 (30秒)...
timeout /t 30 /nobreak >nul
echo.

REM ============================================
REM 步骤3: 启动后端服务
REM ============================================
echo [3/4] 启动后端微服务
echo ----------------------------------------

if %MAVEN_OK%==0 (
    echo [跳过] Maven 不可用，无法启动后端服务
    goto :skip_backend
)

echo.
echo 选择启动方式:
echo   1. 自动启动所有服务（推荐）
echo   2. 手动使用 IDE 启动
echo   3. 跳过后端启动
echo.
set /p backend_choice="请选择 [1/2/3]: "

if "%backend_choice%"=="1" (
    echo.
    echo 正在启动微服务...
    
    start "Gateway" cmd /k "cd /d %BACKEND_DIR%\gateway-service && mvn spring-boot:run -Dspring-boot.run.profiles=simple"
    timeout /t 5 /nobreak >nul
    
    start "User" cmd /k "cd /d %BACKEND_DIR%\user-service && mvn spring-boot:run"
    timeout /t 3 /nobreak >nul
    
    start "Product" cmd /k "cd /d %BACKEND_DIR%\product-service && mvn spring-boot:run"
    timeout /t 3 /nobreak >nul
    
    start "Cart" cmd /k "cd /d %BACKEND_DIR%\cart-service && mvn spring-boot:run"
    timeout /t 3 /nobreak >nul
    
    start "Order" cmd /k "cd /d %BACKEND_DIR%\order-service && mvn spring-boot:run"
    timeout /t 3 /nobreak >nul
    
    start "Payment" cmd /k "cd /d %BACKEND_DIR%\payment-service && mvn spring-boot:run"
    timeout /t 3 /nobreak >nul
    
    start "Merchant" cmd /k "cd /d %BACKEND_DIR%\merchant-service && mvn spring-boot:run"
    timeout /t 3 /nobreak >nul
    
    start "Admin" cmd /k "cd /d %BACKEND_DIR%\admin-service && mvn spring-boot:run"
    timeout /t 3 /nobreak >nul
    
    start "SMS" cmd /k "cd /d %BACKEND_DIR%\sms-service && mvn spring-boot:run"
    
    echo [OK] 微服务已启动
) else if "%backend_choice%"=="2" (
    echo.
    echo 请使用 IntelliJ IDEA 或其他 IDE 启动以下服务:
    echo   - gateway-service (8080)
    echo   - user-service (8082)
    echo   - product-service (8083)
    echo   - cart-service (8088)
    echo   - order-service (8084)
    echo   - payment-service (8085)
    echo   - merchant-service (8087)
    echo   - admin-service (8086)
    echo   - sms-service (8089)
    pause
) else (
    echo [跳过] 后端服务启动
)

:skip_backend
echo.

REM ============================================
REM 步骤4: 启动前端
REM ============================================
echo [4/4] 启动前端
echo ----------------------------------------

if %NODE_OK%==0 (
    echo [跳过] Node.js 不可用，无法启动前端
    goto :finish
)

echo.
set /p start_frontend="是否启动前端？[Y/N]: "
if /i not "%start_frontend%"=="Y" (
    echo [跳过] 前端启动
    goto :finish
)

cd /d "%FRONTEND_DIR%"

if not exist "node_modules" (
    echo.
    echo 首次运行，正在安装依赖...
    call npm install
    if %errorlevel% neq 0 (
        echo [X] 依赖安装失败
        cd /d "%PROJECT_DIR%"
        goto :error
    )
)

echo.
echo 启动前端开发服务器...
start "Frontend" cmd /k "npm run dev"
cd /d "%PROJECT_DIR%"
echo [OK] 前端已启动

:finish
echo.
echo ========================================
echo   启动完成！
echo ========================================
echo.
echo 访问地址:
echo   前端:    http://localhost:5173
echo   网关:    http://localhost:8080
echo   Nacos:   http://localhost:8848/nacos
echo.
echo 登录信息:
echo   前端:    testlogin / nacos
echo   Nacos:   nacos / nacos
echo.
echo 管理命令:
echo   停止服务:  stop-dev-silent.bat
echo   查看日志:  pwsh -File tail-logs.ps1
echo   检查状态:  pwsh -File check-services-silent.ps1
echo.
echo 提示: 所有服务完全启动需要 2-3 分钟
echo.
pause
exit /b 0

:error
echo.
echo ========================================
echo   启动失败！
echo ========================================
echo.
echo 请根据上述错误信息解决问题后重试
echo.
pause
exit /b 1

