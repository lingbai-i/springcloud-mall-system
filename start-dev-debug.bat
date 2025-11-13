@echo off
setlocal enabledelayedexpansion

echo ========================================
echo 在线商城 - 调试启动模式
echo ========================================
echo.
echo 此脚本将逐步显示详细信息，帮助诊断启动问题
echo.

REM ============================================
REM 初始化环境
REM ============================================
set "SCRIPT_DIR=%~dp0"
set "BACKEND_DIR=%SCRIPT_DIR%backend"
set "LOGS_DIR=%SCRIPT_DIR%logs"

echo [调试] 脚本目录: %SCRIPT_DIR%
echo [调试] 后端目录: %BACKEND_DIR%
echo [调试] 日志目录: %LOGS_DIR%
echo.

REM 创建日志目录
if not exist "%LOGS_DIR%" (
    echo [调试] 创建日志目录...
    mkdir "%LOGS_DIR%"
)

REM ============================================
REM 检查环境
REM ============================================
echo [步骤 1/6] 环境检查
echo ========================================
echo.

REM 检查 Docker
echo 检查 Docker...
docker --version
if %errorlevel% neq 0 (
    echo [错误] Docker 未安装或未在 PATH 中
    goto :error_exit
) else (
    echo [√] Docker 可用
)
echo.

REM 检查 Docker Desktop 是否运行
echo 检查 Docker Desktop 运行状态...
docker ps >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] Docker Desktop 未运行
    echo 请先启动 Docker Desktop，然后重试
    goto :error_exit
) else (
    echo [√] Docker Desktop 正在运行
)
echo.

REM 检查 Maven
echo 检查 Maven...
mvn --version
if %errorlevel% neq 0 (
    echo [警告] Maven 未安装，微服务将无法启动
    set "MAVEN_AVAILABLE=0"
) else (
    echo [√] Maven 可用
    set "MAVEN_AVAILABLE=1"
)
echo.

REM 检查 docker-compose 文件
echo 检查 docker-compose-dev.yml...
if not exist "%SCRIPT_DIR%docker-compose-dev.yml" (
    echo [错误] 找不到 docker-compose-dev.yml 文件
    goto :error_exit
) else (
    echo [√] docker-compose-dev.yml 文件存在
)
echo.

REM 检查 backend 目录
echo 检查 backend 目录...
if not exist "%BACKEND_DIR%" (
    echo [错误] 找不到 backend 目录
    goto :error_exit
) else (
    echo [√] backend 目录存在
)
echo.

pause
echo.

REM ============================================
REM 扫描服务
REM ============================================
echo [步骤 2/6] 扫描可用服务
echo ========================================
echo.

set "SERVICE_COUNT=0"
set "EXCLUDE_DIRS=common-bom common-core simple-test"

echo 扫描 backend 目录...
echo.

for /d %%D in ("%BACKEND_DIR%\*") do (
    set "DIR_NAME=%%~nxD"
    set "IS_EXCLUDED=0"
    
    REM 检查是否在排除列表中
    for %%E in (%EXCLUDE_DIRS%) do (
        if /i "!DIR_NAME!"=="%%E" set "IS_EXCLUDED=1"
    )
    
    REM 检查是否存在 pom.xml
    if exist "%%D\pom.xml" (
        if !IS_EXCLUDED!==0 (
            set /a SERVICE_COUNT+=1
            echo   [!SERVICE_COUNT!] 发现服务: !DIR_NAME!
        ) else (
            echo   [跳过] !DIR_NAME! (已排除)
        )
    ) else (
        echo   [跳过] !DIR_NAME! (无 pom.xml)
    )
)

echo.
echo 共发现 %SERVICE_COUNT% 个可启动服务
echo.

if %SERVICE_COUNT%==0 (
    echo [错误] 未发现任何可启动的服务
    goto :error_exit
)

pause
echo.

REM ============================================
REM 询问是否继续
REM ============================================
echo [步骤 3/6] 确认启动
echo ========================================
echo.
echo 将启动以下内容:
echo   - 基础设施: MySQL, Redis, Nacos
echo   - 微服务: %SERVICE_COUNT% 个
echo   - 前端: Vue3 应用
echo.
choice /C YN /M "是否继续启动"
if %errorlevel%==2 (
    echo 用户取消启动
    goto :normal_exit
)
echo.

REM ============================================
REM 启动基础设施
REM ============================================
echo [步骤 4/6] 启动基础设施
echo ========================================
echo.

echo 执行命令: docker-compose -f "%SCRIPT_DIR%docker-compose-dev.yml" up -d
echo.
docker compose -f "%SCRIPT_DIR%docker-compose-dev.yml" up -d
if %errorlevel% neq 0 (
    echo.
    echo [错误] 基础设施启动失败
    echo.
    echo 请检查:
    echo   1. Docker Desktop 是否正在运行
    echo   2. 端口是否被占用 (3307, 6379, 8848)
    echo   3. docker-compose-dev.yml 配置是否正确
    echo.
    goto :error_exit
)

echo.
echo [√] 基础设施启动成功
echo.
echo 等待基础设施初始化 (20秒)...
timeout /t 20 /nobreak
echo.

REM ============================================
REM 检查容器状态
REM ============================================
echo 检查容器状态:
docker ps --filter "name=mall-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo.

pause
echo.

REM ============================================
REM 启动微服务
REM ============================================
echo [步骤 5/6] 启动微服务
echo ========================================
echo.

if %MAVEN_AVAILABLE%==0 (
    echo [跳过] Maven 不可用，无法启动微服务
    goto :skip_services
)

set "STARTED_COUNT=0"
set "SERVICES_CONFIG="
set "SERVICES_CONFIG=!SERVICES_CONFIG!gateway-service:8080:simple:5;"
set "SERVICES_CONFIG=!SERVICES_CONFIG!auth-service:8081::3;"
set "SERVICES_CONFIG=!SERVICES_CONFIG!user-service:8082::3;"
set "SERVICES_CONFIG=!SERVICES_CONFIG!product-service:8083::3;"
set "SERVICES_CONFIG=!SERVICES_CONFIG!order-service:8084::3;"
set "SERVICES_CONFIG=!SERVICES_CONFIG!payment-service:8085::3;"
set "SERVICES_CONFIG=!SERVICES_CONFIG!admin-service:8086::3;"
set "SERVICES_CONFIG=!SERVICES_CONFIG!merchant-service:8087::3;"
set "SERVICES_CONFIG=!SERVICES_CONFIG!cart-service:8088::3;"
set "SERVICES_CONFIG=!SERVICES_CONFIG!sms-service:8089::3;"

for %%S in (%SERVICES_CONFIG%) do (
    set "CONFIG=%%S"
    
    for /f "tokens=1,2,3,4 delims=:" %%A in ("!CONFIG!") do (
        set "SERVICE_NAME=%%A"
        set "SERVICE_PORT=%%B"
        set "SERVICE_PROFILE=%%C"
        set "SERVICE_DELAY=%%D"
        
        if exist "%BACKEND_DIR%\!SERVICE_NAME!\pom.xml" (
            set /a STARTED_COUNT+=1
            
            echo [!STARTED_COUNT!/%SERVICE_COUNT%] 启动 !SERVICE_NAME! (端口: !SERVICE_PORT!)
            
            REM 构建启动命令
            set "MVN_CMD=mvn spring-boot:run"
            if not "!SERVICE_PROFILE!"=="" (
                set "MVN_CMD=!MVN_CMD! -Dspring-boot.run.profiles=!SERVICE_PROFILE!"
            )
            
            echo   命令: !MVN_CMD!
            echo   日志: logs\!SERVICE_NAME!.log
            
            REM 启动服务
            cd /d "%BACKEND_DIR%\!SERVICE_NAME!"
            start /B cmd /c "!MVN_CMD! > "%LOGS_DIR%\!SERVICE_NAME!.log" 2>&1"
            cd /d "%SCRIPT_DIR%"
            
            REM 等待初始化
            if "!SERVICE_DELAY!"=="" set "SERVICE_DELAY=3"
            echo   等待 !SERVICE_DELAY! 秒...
            timeout /t !SERVICE_DELAY! /nobreak >nul
            echo.
        ) else (
            echo [跳过] !SERVICE_NAME! (目录不存在)
            echo.
        )
    )
)

echo [√] 已启动 %STARTED_COUNT% 个微服务
echo.

:skip_services

pause
echo.

REM ============================================
REM 启动前端
REM ============================================
echo [步骤 6/6] 启动前端
echo ========================================
echo.

if exist "%SCRIPT_DIR%frontend\package.json" (
    cd /d "%SCRIPT_DIR%frontend"
    
    if not exist "node_modules" (
        echo 首次运行，需要安装依赖...
        choice /C YN /M "是否现在安装前端依赖 (可能需要几分钟)"
        if %errorlevel%==1 (
            call npm install
        ) else (
            echo [跳过] 前端依赖安装
            cd /d "%SCRIPT_DIR%"
            goto :skip_frontend
        )
    )
    
    echo 启动前端开发服务器...
    start /B cmd /c "npm run dev > "%LOGS_DIR%\frontend.log" 2>&1"
    cd /d "%SCRIPT_DIR%"
    echo [√] 前端服务已启动
) else (
    echo [警告] 未找到前端项目
)

:skip_frontend
echo.

REM ============================================
REM 启动完成
REM ============================================
echo ========================================
echo 启动完成！
echo ========================================
echo.
echo 📊 启动统计:
echo   基础设施: 3 (MySQL, Redis, Nacos)
echo   微服务: %STARTED_COUNT%
echo   前端: 1
echo.
echo 🌐 访问地址:
echo   前端:    http://localhost:5173
echo   网关:    http://localhost:8080
echo   Nacos:   http://localhost:8848/nacos
echo.
echo 📝 管理命令:
echo   查看日志: pwsh -File tail-logs.ps1
echo   检查状态: pwsh -File check-services-silent.ps1
echo.
goto :normal_exit

REM ============================================
REM 退出处理
REM ============================================
:error_exit
echo.
echo ========================================
echo 启动失败！
echo ========================================
echo.
echo 请根据上述错误信息解决问题后重试
echo.
pause
exit /b 1

:normal_exit
echo.
echo 按任意键退出...
pause >nul
exit /b 0
