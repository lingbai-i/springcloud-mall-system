@echo off
chcp 65001 >nul
echo ========================================
echo 在线商城 - 后台启动模式
echo ========================================
echo.
echo 本脚本将：
echo   1. 启动 Docker 基础设施（MySQL、Redis、Nacos）
echo   2. 后台启动所有微服务（无弹窗）
echo   3. 日志输出到 logs 目录
echo.

REM 创建日志目录
if not exist "logs" mkdir logs

REM 检查Docker环境
echo 检查 Docker 环境...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Docker，请先安装Docker Desktop
    pause
    exit /b 1
)
echo Docker环境检查通过
echo.

REM 启动基础设施
echo 启动基础设施 (MySQL, Redis, Nacos)...
docker-compose -f docker-compose-dev.yml up -d
if %errorlevel% neq 0 (
    echo 错误: 基础设施启动失败
    pause
    exit /b 1
)
echo 基础设施启动成功
echo.

REM 等待服务就绪
echo 等待基础设施就绪 (20秒)...
timeout /t 20 /nobreak >nul
echo.

REM 启动微服务到后台
echo 启动微服务到后台...
echo.

REM 启动网关服务
echo 启动网关服务 (端口: 8080)...
cd /d "%~dp0backend\gateway-service"
start /B cmd /c "mvn spring-boot:run -Dspring-boot.run.profiles=simple > ..\..\logs\gateway.log 2>&1"
cd /d "%~dp0"
timeout /t 3 /nobreak >nul

REM 启动用户服务
echo 启动用户服务 (端口: 8082)...
cd /d "%~dp0backend\user-service"
start /B cmd /c "mvn spring-boot:run > ..\..\logs\user.log 2>&1"
cd /d "%~dp0"
timeout /t 3 /nobreak >nul

REM 启动商品服务
echo 启动商品服务 (端口: 8083)...
cd /d "%~dp0backend\product-service"
start /B cmd /c "mvn spring-boot:run > ..\..\logs\product.log 2>&1"
cd /d "%~dp0"
timeout /t 3 /nobreak >nul

REM 启动购物车服务
echo 启动购物车服务 (端口: 8088)...
cd /d "%~dp0backend\cart-service"
start /B cmd /c "mvn spring-boot:run > ..\..\logs\cart.log 2>&1"
cd /d "%~dp0"
timeout /t 3 /nobreak >nul

REM 启动订单服务
echo 启动订单服务 (端口: 8084)...
cd /d "%~dp0backend\order-service"
start /B cmd /c "mvn spring-boot:run > ..\..\logs\order.log 2>&1"
cd /d "%~dp0"
timeout /t 3 /nobreak >nul

REM 启动支付服务
echo 启动支付服务 (端口: 8085)...
cd /d "%~dp0backend\payment-service"
start /B cmd /c "mvn spring-boot:run > ..\..\logs\payment.log 2>&1"
cd /d "%~dp0"
timeout /t 3 /nobreak >nul

REM 启动管理服务
echo 启动管理服务 (端口: 8086)...
cd /d "%~dp0backend\admin-service"
start /B cmd /c "mvn spring-boot:run > ..\..\logs\admin.log 2>&1"
cd /d "%~dp0"
timeout /t 3 /nobreak >nul

REM 启动商家服务
echo 启动商家服务 (端口: 8087)...
cd /d "%~dp0backend\merchant-service"
start /B cmd /c "mvn spring-boot:run > ..\..\logs\merchant.log 2>&1"
cd /d "%~dp0"
timeout /t 3 /nobreak >nul

REM 启动短信服务
echo 启动短信服务 (端口: 8089)...
cd /d "%~dp0backend\sms-service"
start /B cmd /c "mvn spring-boot:run > ..\..\logs\sms.log 2>&1"
cd /d "%~dp0"
timeout /t 3 /nobreak >nul

REM 启动前端
echo 启动前端服务 (端口: 5173)...
cd /d "%~dp0frontend"
if not exist "node_modules" (
    echo 首次运行，正在安装依赖...
    call npm install >nul 2>&1
)
start /B cmd /c "npm run dev > ..\logs\frontend.log 2>&1"
cd /d "%~dp0"

echo.
echo ========================================
echo 所有服务已在后台启动！
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
echo 日志文件:
echo   所有日志保存在: logs\ 目录
echo   查看实时日志:   pwsh -File tail-logs.ps1
echo   检查服务状态:   pwsh -File check-services-silent.ps1
echo.
echo 提示:
echo   - 服务在后台运行，无CMD窗口
echo   - 查看日志: pwsh -File tail-logs.ps1 [服务名]
echo   - 重启服务: pwsh -File restart-service.ps1 [服务名]
echo   - 停止所有: stop-dev-silent.bat
echo.
echo 服务启动需要 1-2 分钟，请稍后访问
echo.
pause

