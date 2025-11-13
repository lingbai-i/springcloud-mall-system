@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   SpringCloud商城 - 快速启动脚本
echo ========================================
echo.

REM 设置项目路径
set "PROJECT_DIR=%~dp0"
set "BACKEND_DIR=%PROJECT_DIR%backend"
set "FRONTEND_DIR=%PROJECT_DIR%frontend"

echo [1/5] 启动 Docker 基础设施...
echo.
docker compose -f "%PROJECT_DIR%docker-compose-dev.yml" up -d
if %errorlevel% neq 0 (
    echo [错误] Docker 容器启动失败！
    echo 请确保 Docker Desktop 正在运行
    pause
    exit /b 1
)
echo [√] Docker 容器启动成功
echo.

echo [2/5] 等待基础设施就绪（30秒）...
timeout /t 30 /nobreak >nul
echo [√] 基础设施就绪
echo.

echo [3/5] 检查容器状态...
docker compose -f "%PROJECT_DIR%docker-compose-dev.yml" ps
echo.

echo [4/5] 启动后端微服务...
echo.
echo 请选择启动方式:
echo   1. 使用 Maven 命令启动（会打开多个窗口）
echo   2. 手动使用 IDE 启动（推荐）
echo   3. 跳过后端启动
echo.
set /p choice="请输入选择 (1/2/3): "

if "%choice%"=="1" (
    echo.
    echo 正在启动微服务...
    
    REM 启动网关服务
    start "Gateway Service" cmd /k "cd /d %BACKEND_DIR%\gateway-service && mvn spring-boot:run -Dspring-boot.run.profiles=simple"
    timeout /t 5 /nobreak >nul
    
    REM 启动用户服务
    start "User Service" cmd /k "cd /d %BACKEND_DIR%\user-service && mvn spring-boot:run"
    timeout /t 3 /nobreak >nul
    
    REM 启动商品服务
    start "Product Service" cmd /k "cd /d %BACKEND_DIR%\product-service && mvn spring-boot:run"
    timeout /t 3 /nobreak >nul
    
    REM 启动购物车服务
    start "Cart Service" cmd /k "cd /d %BACKEND_DIR%\cart-service && mvn spring-boot:run"
    timeout /t 3 /nobreak >nul
    
    REM 启动订单服务
    start "Order Service" cmd /k "cd /d %BACKEND_DIR%\order-service && mvn spring-boot:run"
    timeout /t 3 /nobreak >nul
    
    REM 启动支付服务
    start "Payment Service" cmd /k "cd /d %BACKEND_DIR%\payment-service && mvn spring-boot:run"
    timeout /t 3 /nobreak >nul
    
    REM 启动商家服务
    start "Merchant Service" cmd /k "cd /d %BACKEND_DIR%\merchant-service && mvn spring-boot:run"
    timeout /t 3 /nobreak >nul
    
    REM 启动管理服务
    start "Admin Service" cmd /k "cd /d %BACKEND_DIR%\admin-service && mvn spring-boot:run"
    timeout /t 3 /nobreak >nul
    
    REM 启动短信服务
    start "SMS Service" cmd /k "cd /d %BACKEND_DIR%\sms-service && mvn spring-boot:run"
    
    echo [√] 所有微服务已启动
) else if "%choice%"=="2" (
    echo.
    echo 请使用 IntelliJ IDEA 或其他 IDE 手动启动后端服务
    echo.
    echo 启动顺序:
    echo   1. gateway-service (8080)
    echo   2. user-service (8082)
    echo   3. product-service (8083)
    echo   4. cart-service (8088)
    echo   5. order-service (8084)
    echo   6. payment-service (8085)
    echo   7. merchant-service (8087)
    echo   8. admin-service (8086)
    echo   9. sms-service (8089)
    echo.
    pause
) else (
    echo [跳过] 后端服务启动
)

echo.
echo [5/5] 启动前端...
echo.
set /p start_frontend="是否启动前端？(Y/N): "
if /i "%start_frontend%"=="Y" (
    if exist "%FRONTEND_DIR%\node_modules" (
        start "Frontend" cmd /k "cd /d %FRONTEND_DIR% && npm run dev"
        echo [√] 前端服务已启动
    ) else (
        echo [提示] 首次运行需要安装依赖
        start "Frontend" cmd /k "cd /d %FRONTEND_DIR% && npm install && npm run dev"
        echo [√] 前端服务正在安装依赖并启动...
    )
) else (
    echo [跳过] 前端启动
)

echo.
echo ========================================
echo   启动完成！
echo ========================================
echo.
echo 🌐 访问地址:
echo   前端应用:     http://localhost:5173
echo   API网关:      http://localhost:8080
echo   Nacos控制台:  http://localhost:8848/nacos
echo.
echo 🔐 登录信息:
echo   前端账号:     testlogin / nacos
echo   Nacos账号:    nacos / nacos
echo.
echo 📝 提示:
echo   - 所有服务完全启动需要 2-3 分钟
echo   - 可以访问 Nacos 控制台查看服务注册情况
echo   - 查看启动指南: 启动指南-完整版.md
echo.
echo ========================================
pause

