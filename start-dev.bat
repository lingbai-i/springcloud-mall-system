@echo off
echo ========================================
echo 在线商城 - 本地开发环境启动
echo ========================================
echo.
echo 本脚本会：
echo   1. 启动基础设施 (MySQL, Redis, Nacos)
echo   2. 启动核心微服务 (Gateway, User, Product, Cart)
echo   3. 启动前端开发服务器
echo.
echo 提示：如果只需要启动基础设施，请按 Ctrl+C 取消
echo        然后运行：docker compose -f docker-compose-dev.yml up -d
echo.
pause

REM 检查Docker环境
echo 检查Docker环境...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo [错误] 未找到Docker，请先安装Docker Desktop
    echo.
    echo 解决方案:
    echo   1. 安装 Docker Desktop: https://www.docker.com/products/docker-desktop
    echo   2. 确保 Docker Desktop 已启动
    echo   3. 重新运行此脚本
    echo.
    echo 💡 提示: 也可以运行 diagnose.bat 进行完整诊断
    echo.
    pause
    exit /b 1
)
echo Docker环境检查通过
echo.

REM 启动基础设施
echo 启动基础设施 (MySQL, Redis, Nacos)...
docker compose -f docker-compose-dev.yml up -d
if %errorlevel% neq 0 (
    echo.
    echo [错误] 基础设施启动失败
    echo.
    echo 可能的原因:
    echo   1. Docker Desktop 未运行
    echo   2. 端口被占用（3307, 6379, 8848）
    echo   3. docker-compose-dev.yml 配置错误
    echo.
    echo 💡 建议: 运行 diagnose.bat 进行详细诊断
    echo.
    pause
    exit /b 1
)
echo 基础设施启动成功
echo.

REM 等待服务就绪
echo 等待基础设施就绪 (20秒)...
timeout /t 20 /nobreak >nul
echo.

REM 询问是否启动后端服务
echo 是否启动后端微服务? (Y/N)
set /p start_backend="请选择 [Y/N]: "
if /i "%start_backend%" neq "Y" goto skip_backend

echo.
echo 启动后端微服务...
echo.

REM 启动网关服务
echo 启动网关服务 (端口: 8080)...
start "Gateway Service" cmd /k "cd /d %~dp0backend\gateway-service && mvn spring-boot:run -Dspring-boot.run.profiles=simple"
timeout /t 10 /nobreak >nul

REM 启动用户服务
echo 启动用户服务 (端口: 8082)...
start "User Service" cmd /k "cd /d %~dp0backend\user-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

REM 启动商品服务
echo 启动商品服务 (端口: 8083)...
start "Product Service" cmd /k "cd /d %~dp0backend\product-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

REM 启动购物车服务
echo 启动购物车服务 (端口: 8088)...
start "Cart Service" cmd /k "cd /d %~dp0backend\cart-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo.
echo 等待微服务启动完成 (30秒)...
timeout /t 30 /nobreak >nul

:skip_backend

REM 询问是否启动前端
echo.
echo 是否启动前端开发服务器? (Y/N)
set /p start_frontend="请选择 [Y/N]: "
if /i "%start_frontend%" neq "Y" goto finish

echo.
echo 启动前端服务...
cd /d %~dp0frontend

REM 检查是否需要安装依赖
if not exist "node_modules" (
    echo 检测到首次运行，正在安装依赖...
    call npm install
    if %errorlevel% neq 0 (
        echo.
        echo [错误] 前端依赖安装失败
        echo.
        echo 可能的原因:
        echo   1. Node.js 未安装或版本过低
        echo   2. 网络连接问题
        echo   3. npm 配置问题
        echo.
        echo 💡 建议: 配置 npm 国内镜像源
        echo   npm config set registry https://registry.npmmirror.com
        echo.
        cd /d %~dp0
        pause
        exit /b 1
    )
)

echo 启动前端开发服务器 (端口: 5173)...
start "Frontend Service" cmd /k "npm run dev"
cd /d %~dp0

:finish
echo.
echo ========================================
echo 本地开发环境启动完成！
echo ========================================
echo.
echo 服务访问地址:
echo   前端应用:     http://localhost:5173
echo   API网关:      http://localhost:8080
echo   Nacos控制台:  http://localhost:8848/nacos (nacos/nacos)
echo.
echo 基础设施:
echo   MySQL:        localhost:3307 (root/123456)
echo   Redis:        localhost:6379
echo.
echo 提示:
echo   - 微服务在独立窗口运行，关闭窗口即停止服务
echo   - 停止基础设施: docker-compose -f docker-compose-dev.yml down
echo   - 查看服务状态: pwsh -File check-services-silent.ps1
echo.
pause
exit /b 0
