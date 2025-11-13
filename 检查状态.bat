@echo off
setlocal enabledelayedexpansion

echo.
echo ========================================
echo   SpringCloud商城 - 服务状态检查
echo ========================================
echo.

echo [1] Docker基础设施状态
echo ----------------------------------------
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo.

echo [2] 后端微服务端口监听状态
echo ----------------------------------------
echo 检查端口: 8080-8089
echo.

set "PORTS=8080 8082 8083 8084 8085 8086 8087 8088 8089"
for %%P in (%PORTS%) do (
    netstat -ano | findstr ":%%P " | findstr "LISTENING" >nul 2>&1
    if !errorlevel! equ 0 (
        echo   [OK] 端口 %%P 正在监听
    ) else (
        echo   [X]  端口 %%P 未监听
    )
)
echo.

echo [3] 前端服务状态
echo ----------------------------------------
netstat -ano | findstr ":5173 " | findstr "LISTENING" >nul 2>&1
if %errorlevel% equ 0 (
    echo   [OK] 前端 (5173) 正在运行
) else (
    echo   [X]  前端 (5173) 未运行
)
echo.

echo [4] Java进程数量
echo ----------------------------------------
tasklist | findstr /I "java.exe" | find /C "java.exe"
echo.

echo [5] Node进程数量
echo ----------------------------------------
tasklist | findstr /I "node.exe" | find /C "node.exe"
echo.

echo ========================================
echo   检查完成
echo ========================================
echo.
pause

