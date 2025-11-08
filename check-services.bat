@echo off
chcp 65001 >nul
echo ========================================
echo 🔍 在线商城服务状态检查
echo ========================================
echo.

REM 获取当前时间
echo 📅 检查时间: %date% %time%
echo.

echo 🌐 检查服务端口状态...
echo.

REM 检查各个服务端口
echo 📡 网关服务 (8080):
netstat -an | findstr ":8080" >nul
if %errorlevel% equ 0 (
    echo ✅ 运行中
) else (
    echo ❌ 未运行
)

echo 🔐 认证服务 (8081):
netstat -an | findstr ":8081" >nul
if %errorlevel% equ 0 (
    echo ✅ 运行中
) else (
    echo ❌ 未运行
)

echo 👤 用户服务 (8082):
netstat -an | findstr ":8082" >nul
if %errorlevel% equ 0 (
    echo ✅ 运行中
) else (
    echo ❌ 未运行
)

echo 📦 商品服务 (8083):
netstat -an | findstr ":8083" >nul
if %errorlevel% equ 0 (
    echo ✅ 运行中
) else (
    echo ❌ 未运行
)

echo 📋 订单服务 (8084):
netstat -an | findstr ":8084" >nul
if %errorlevel% equ 0 (
    echo ✅ 运行中
) else (
    echo ❌ 未运行
)

echo 💳 支付服务 (8085):
netstat -an | findstr ":8085" >nul
if %errorlevel% equ 0 (
    echo ✅ 运行中
) else (
    echo ❌ 未运行
)

echo 👨‍💼 管理服务 (8086):
netstat -an | findstr ":8086" >nul
if %errorlevel% equ 0 (
    echo ✅ 运行中
) else (
    echo ❌ 未运行
)

echo 🏪 商家服务 (8087):
netstat -an | findstr ":8087" >nul
if %errorlevel% equ 0 (
    echo ✅ 运行中
) else (
    echo ❌ 未运行
)

echo 🛒 购物车服务 (8088):
netstat -an | findstr ":8088" >nul
if %errorlevel% equ 0 (
    echo ✅ 运行中
) else (
    echo ❌ 未运行
)

echo 🎨 前端服务 (3003):
netstat -an | findstr ":3003" >nul
if %errorlevel% equ 0 (
    echo ✅ 运行中
) else (
    echo ❌ 未运行
)

echo.
echo 🐳 检查基础设施服务...

echo 🗄️ MySQL (3306):
netstat -an | findstr ":3306" >nul
if %errorlevel% equ 0 (
    echo ✅ 运行中
) else (
    echo ❌ 未运行
)

echo 📊 Redis (6379):
netstat -an | findstr ":6379" >nul
if %errorlevel% equ 0 (
    echo ✅ 运行中
) else (
    echo ❌ 未运行
)

echo 🎯 Nacos (8848):
netstat -an | findstr ":8848" >nul
if %errorlevel% equ 0 (
    echo ✅ 运行中
) else (
    echo ❌ 未运行
)

echo.
echo ========================================
echo 📊 服务状态检查完成
echo ========================================
echo.
echo 💡 提示:
echo   - 如果服务未运行，请使用 start-all-services.bat 启动
echo   - 可以访问 http://localhost:8848/nacos 查看服务注册状态
echo   - 前端应用地址: http://localhost:3003
echo.
pause