@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo 启动Docker基础设施服务
echo ========================================
echo.

cd /d "%~dp0"

echo [1/4] 检查Docker环境...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] Docker未安装或未运行
    echo 请先启动Docker Desktop
    pause
    exit /b 1
)
echo [√] Docker可用
echo.

echo [2/4] 停止现有容器...
docker-compose -f docker-compose-dev.yml down >nul 2>&1
timeout /t 3 /nobreak >nul
echo [√] 完成
echo.

echo [3/4] 启动基础设施容器...
echo 正在启动MySQL、Redis、Nacos...
echo.
docker-compose -f docker-compose-dev.yml up -d
if %errorlevel% neq 0 (
    echo.
    echo [错误] 容器启动失败
    pause
    exit /b 1
)
echo.
echo [√] 容器启动完成
echo.

echo [4/4] 等待服务就绪...
timeout /t 10 /nobreak >nul
echo.

echo 检查容器状态:
docker-compose -f docker-compose-dev.yml ps
echo.

echo ========================================
echo 服务访问地址
echo ========================================
echo MySQL:  localhost:3307 (root/123456)
echo Redis:  localhost:6379  
echo Nacos:  http://localhost:8848/nacos (nacos/nacos)
echo.
echo 提示: Nacos首次启动需要1-2分钟初始化
echo.
pause
