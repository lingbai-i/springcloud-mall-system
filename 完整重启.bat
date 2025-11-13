@echo off
setlocal enabledelayedexpansion

title SpringCloud商城 - 完整重启

echo.
echo ========================================
echo   SpringCloud商城 - 完整重启
echo ========================================
echo.

REM 步骤1: 停止所有服务
echo [1/4] 停止所有服务...
echo.

echo 停止Java进程...
taskkill /F /IM java.exe >nul 2>&1
if %errorlevel% equ 0 (
    echo   [OK] Java进程已停止
) else (
    echo   [INFO] 没有运行中的Java进程
)

echo 停止Node进程...
taskkill /F /IM node.exe >nul 2>&1
if %errorlevel% equ 0 (
    echo   [OK] Node进程已停止
) else (
    echo   [INFO] 没有运行中的Node进程
)

echo 停止Docker容器...
docker compose -f docker-compose-dev.yml down
if %errorlevel% equ 0 (
    echo   [OK] Docker容器已停止
) else (
    echo   [WARN] Docker容器停止失败
)

echo.
echo 等待5秒...
timeout /t 5 /nobreak >nul
echo.

REM 步骤2: 启动基础设施
echo [2/4] 启动基础设施 (MySQL, Redis, Nacos)...
echo.

docker compose -f docker-compose-dev.yml up -d
if %errorlevel% neq 0 (
    echo [ERROR] 基础设施启动失败！
    echo 请检查Docker Desktop是否运行
    pause
    exit /b 1
)

echo   [OK] 基础设施已启动
echo.
echo 等待30秒让服务就绪...
timeout /t 30 /nobreak >nul
echo.

REM 步骤3: 启动后端微服务
echo [3/4] 启动后端微服务...
echo.

echo 启动 Gateway (8080)...
start "Gateway-8080" cmd /c "cd /d backend\gateway-service && mvn spring-boot:run -Dspring-boot.run.profiles=simple"
timeout /t 8 /nobreak >nul

echo 启动 User Service (8082)...
start "User-8082" cmd /c "cd /d backend\user-service && mvn spring-boot:run"
timeout /t 3 /nobreak >nul

echo 启动 Product Service (8083)...
start "Product-8083" cmd /c "cd /d backend\product-service && mvn spring-boot:run"
timeout /t 3 /nobreak >nul

echo 启动 Cart Service (8088)...
start "Cart-8088" cmd /c "cd /d backend\cart-service && mvn spring-boot:run"
timeout /t 3 /nobreak >nul

echo 启动 Order Service (8084)...
start "Order-8084" cmd /c "cd /d backend\order-service && mvn spring-boot:run"
timeout /t 3 /nobreak >nul

echo 启动 Payment Service (8085)...
start "Payment-8085" cmd /c "cd /d backend\payment-service && mvn spring-boot:run"
timeout /t 3 /nobreak >nul

echo 启动 Merchant Service (8087)...
start "Merchant-8087" cmd /c "cd /d backend\merchant-service && mvn spring-boot:run"
timeout /t 3 /nobreak >nul

echo 启动 Admin Service (8086)...
start "Admin-8086" cmd /c "cd /d backend\admin-service && mvn spring-boot:run"
timeout /t 3 /nobreak >nul

echo 启动 SMS Service (8089)...
start "SMS-8089" cmd /c "cd /d backend\sms-service && mvn spring-boot:run"

echo.
echo   [OK] 所有微服务已启动
echo.

REM 步骤4: 启动前端
echo [4/4] 启动前端...
echo.

set /p start_fe="是否启动前端？[Y/N]: "
if /i "%start_fe%"=="Y" (
    echo 启动前端开发服务器...
    start "Frontend-5173" cmd /c "cd /d frontend && npm run dev"
    echo   [OK] 前端已启动
) else (
    echo   [SKIP] 跳过前端启动
)

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
echo 提示: 所有服务完全启动需要2-3分钟
echo       请等待所有窗口显示 "Started" 后再访问
echo.
pause

