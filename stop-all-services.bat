@echo off
chcp 65001 >nul
echo ========================================
echo 🛑 停止在线商城所有服务
echo ========================================
echo.

REM 获取当前时间
echo 📅 停止时间: %date% %time%
echo.

echo 🔄 正在停止所有Java进程...
taskkill /f /im java.exe >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Java进程已停止
) else (
    echo ℹ️ 没有运行的Java进程
)

echo 🔄 正在停止Node.js进程...
taskkill /f /im node.exe >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Node.js进程已停止
) else (
    echo ℹ️ 没有运行的Node.js进程
)

echo 🔄 正在停止Maven进程...
taskkill /f /im mvn.cmd >nul 2>&1
taskkill /f /im mvn >nul 2>&1

echo 🐳 正在停止Docker容器...
docker-compose down
if %errorlevel% equ 0 (
    echo ✅ Docker容器已停止
) else (
    echo ℹ️ Docker容器停止失败或未运行
)

echo.
echo ========================================
echo ✅ 所有服务已停止
echo ========================================
echo.
echo 💡 提示:
echo   - 所有微服务和前端服务已停止
echo   - 基础设施服务(MySQL, Redis, Nacos)已停止
echo   - 如需重新启动，请运行 start-all-services.bat
echo.
pause