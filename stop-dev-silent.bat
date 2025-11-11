@echo off
chcp 65001 >nul
echo ========================================
echo 停止在线商城所有服务
echo ========================================
echo.

REM 获取当前时间
echo 停止时间: %date% %time%
echo.

echo 正在停止后台服务...
echo.

REM 停止所有Java进程（微服务）
echo 停止所有微服务...
taskkill /F /IM java.exe >nul 2>&1
if %errorlevel% equ 0 (
    echo 微服务已停止
) else (
    echo 没有运行的微服务进程
)

REM 停止Node.js进程（前端）
echo 停止前端服务...
taskkill /F /IM node.exe >nul 2>&1
if %errorlevel% equ 0 (
    echo 前端服务已停止
) else (
    echo 没有运行的前端进程
)

REM 停止Maven进程
echo 停止Maven进程...
taskkill /F /IM mvn.cmd >nul 2>&1
taskkill /F /IM mvn >nul 2>&1

REM 停止Docker容器（基础设施）
echo 停止Docker基础设施...
docker-compose -f docker-compose-dev.yml down
if %errorlevel% equ 0 (
    echo Docker容器已停止
) else (
    echo Docker容器停止失败或未运行
)

echo.
echo ========================================
echo 所有服务已停止
echo ========================================
echo.

REM 询问是否清理日志
echo 是否清理日志文件? (y/N)
set /p clean_logs="请选择 [y/N]: "

if /i "%clean_logs%"=="y" (
    echo.
    echo 清理日志文件...
    if exist "logs\*.log" (
        del /Q logs\*.log
        echo 日志文件已清理
    ) else (
        echo 没有日志文件需要清理
    )
) else (
    echo 保留日志文件
)

echo.
echo 提示:
echo   - 所有微服务和前端服务已停止
echo   - 基础设施服务 (MySQL, Redis, Nacos) 已停止
echo   - 如需重新启动，请运行 start-dev-silent.bat
echo   - 查看日志文件: logs\ 目录
echo.
pause

