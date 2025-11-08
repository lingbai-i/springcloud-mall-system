@echo off
chcp 65001 >nul
echo ========================================
echo 🚀 在线商城微服务启动脚本
echo ========================================
echo.

REM 获取当前时间：2025-10-21 23:01:58
echo 📅 启动时间: %date% %time%
echo.

REM 检查Java环境
echo 🔍 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: 未找到Java环境，请先安装JDK 8或更高版本
    pause
    exit /b 1
)
echo ✅ Java环境检查通过
echo.

REM 检查Maven环境
echo 🔍 检查Maven环境...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: 未找到Maven环境，请先安装Maven
    pause
    exit /b 1
)
echo ✅ Maven环境检查通过
echo.

REM 检查Node.js环境
echo 🔍 检查Node.js环境...
node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: 未找到Node.js环境，请先安装Node.js
    pause
    exit /b 1
)
echo ✅ Node.js环境检查通过
echo.

REM 启动基础设施服务
echo 🐳 启动基础设施服务 (MySQL, Redis, Nacos)...
docker-compose up -d
if %errorlevel% neq 0 (
    echo ❌ 错误: Docker服务启动失败，请检查Docker是否正常运行
    pause
    exit /b 1
)
echo ✅ 基础设施服务启动成功
echo.

REM 等待基础设施服务就绪
echo ⏳ 等待基础设施服务就绪 (30秒)...
timeout /t 30 /nobreak >nul
echo.

REM 编译后端项目
echo 🔨 编译后端微服务...
cd backend
mvn clean compile -DskipTests
if %errorlevel% neq 0 (
    echo ❌ 错误: 后端项目编译失败
    cd ..
    pause
    exit /b 1
)
echo ✅ 后端项目编译成功
cd ..
echo.

REM 启动微服务 (按依赖顺序)
echo 🚀 启动微服务...

REM 1. 启动网关服务
echo 📡 启动网关服务 (端口: 8080)...
start "Gateway Service" cmd /c "cd backend\gateway-service && mvn spring-boot:run"
timeout /t 10 /nobreak >nul

REM 2. 启动认证服务
echo 🔐 启动认证服务 (端口: 8081)...
start "Auth Service" cmd /c "cd backend\auth-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

REM 3. 启动用户服务
echo 👤 启动用户服务 (端口: 8082)...
start "User Service" cmd /c "cd backend\user-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

REM 4. 启动商品服务
echo 📦 启动商品服务 (端口: 8083)...
start "Product Service" cmd /c "cd backend\product-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

REM 5. 启动购物车服务
echo 🛒 启动购物车服务 (端口: 8088)...
start "Cart Service" cmd /c "cd backend\cart-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

REM 6. 启动订单服务
echo 📋 启动订单服务 (端口: 8084)...
start "Order Service" cmd /c "cd backend\order-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

REM 7. 启动支付服务
echo 💳 启动支付服务 (端口: 8085)...
start "Payment Service" cmd /c "cd backend\payment-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

REM 8. 启动商家服务
echo 🏪 启动商家服务 (端口: 8087)...
start "Merchant Service" cmd /c "cd backend\merchant-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

REM 9. 启动管理服务
echo 👨‍💼 启动管理服务 (端口: 8086)...
start "Admin Service" cmd /c "cd backend\admin-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo.
echo ⏳ 等待所有微服务启动完成 (60秒)...
timeout /t 60 /nobreak >nul
echo.

REM 安装前端依赖并启动
echo 🎨 启动前端服务...
cd frontend
echo 📦 安装前端依赖...
npm install
if %errorlevel% neq 0 (
    echo ❌ 错误: 前端依赖安装失败
    cd ..
    pause
    exit /b 1
)

echo 🚀 启动前端开发服务器 (端口: 3003)...
start "Frontend Service" cmd /c "npm run dev"
cd ..
echo.

echo ========================================
echo ✅ 所有服务启动完成！
echo ========================================
echo.
echo 🌐 服务访问地址:
echo   前端应用:     http://localhost:3003
echo   API网关:      http://localhost:8080
echo   Nacos控制台:  http://localhost:8848/nacos
echo   用户名/密码:  nacos/nacos
echo.
echo 📊 微服务端口分配:
echo   Gateway:      8080
echo   Auth:         8081  
echo   User:         8082
echo   Product:      8083
echo   Order:        8084
echo   Payment:      8085
echo   Admin:        8086
echo   Merchant:     8087
echo   Cart:         8088
echo.
echo 💡 提示: 
echo   - 首次启动可能需要较长时间下载依赖
echo   - 如果服务启动失败，请检查端口是否被占用
echo   - 可以通过 Nacos 控制台查看服务注册状态
echo.
echo 按任意键退出...
pause >nul