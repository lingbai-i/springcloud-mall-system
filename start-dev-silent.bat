@echo off
setlocal enabledelayedexpansion

echo ========================================
echo 在线商城 - 智能后台启动模式
echo ========================================
echo.
echo 本脚本将：
echo   1. 启动 Docker 基础设施（MySQL、Redis、Nacos）
echo   2. 自动检测并启动所有微服务（无弹窗）
echo   3. 日志输出到 logs 目录
echo   4. 提供服务依赖关系管理和状态反馈
echo.

REM ============================================
REM 初始化环境
REM ============================================
set "SCRIPT_DIR=%~dp0"
set "BACKEND_DIR=%SCRIPT_DIR%backend"
set "LOGS_DIR=%SCRIPT_DIR%logs"
set "SERVICE_COUNT=0"
set "STARTED_COUNT=0"

REM 创建日志目录
if not exist "%LOGS_DIR%" mkdir "%LOGS_DIR%"

REM 定义服务启动顺序和配置（优先级从高到低）
REM 格式: 服务名:端口:配置文件:启动延迟(秒)
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

REM 排除的目录（非服务模块）
set "EXCLUDE_DIRS=common-bom common-core simple-test"

REM ============================================
REM 检查环境
REM ============================================
echo [步骤 1/5] 检查运行环境...
echo.

REM 检查 Docker
echo 检查 Docker 环境...
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
    pause
    exit /b 1
)
echo [√] Docker 环境检查通过
echo.

REM 检查 Docker Desktop 是否运行
echo 检查 Docker Desktop 运行状态...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo [错误] Docker Desktop 未运行
    echo.
    echo 解决方案:
    echo   1. 启动 Docker Desktop
    echo   2. 等待 Docker Desktop 完全启动（托盘图标显示绿色）
    echo   3. 重新运行此脚本
    echo.
    pause
    exit /b 1
)
echo [√] Docker Desktop 正在运行
echo.

REM 检查 Maven
echo 检查 Maven 环境...
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [警告] 未找到Maven，某些服务可能无法启动
) else (
    echo [√] Maven 环境检查通过
)
echo.

REM ============================================
REM 启动基础设施
REM ============================================
echo [步骤 2/5] 启动基础设施...
echo.
echo 启动 MySQL、Redis、Nacos...
echo 执行命令: docker compose -f docker-compose-dev.yml up -d
echo.
docker compose -f "%SCRIPT_DIR%docker-compose-dev.yml" up -d 2>&1
set "DOCKER_EXIT_CODE=%errorlevel%"
echo.
if %DOCKER_EXIT_CODE% neq 0 (
    echo [错误] 基础设施启动失败 (退出码: %DOCKER_EXIT_CODE%)
    echo.
    echo 可能的原因:
    echo   1. Docker Desktop 未运行
    echo   2. docker-compose-dev.yml 文件不存在或配置错误
    echo   3. 端口被占用（3307, 6379, 8848）
    echo   4. Docker 镜像下载失败
    echo.
    echo 请检查以上问题后重试
    echo.
    pause
    exit /b 1
)
echo [√] 基础设施启动成功
echo.

REM 等待基础设施就绪
echo 等待基础设施初始化完成 (20秒)...
timeout /t 20 /nobreak >nul
echo [√] 基础设施就绪
echo.

REM ============================================
REM 扫描并验证服务
REM ============================================
echo [步骤 3/5] 扫描可用服务...
echo.

REM 遍历 backend 目录，检测所有服务
for /d %%D in ("%BACKEND_DIR%\*") do (
    set "DIR_NAME=%%~nxD"
    set "IS_EXCLUDED=0"
    
    REM 检查是否在排除列表中
    for %%E in (%EXCLUDE_DIRS%) do (
        if /i "!DIR_NAME!"=="%%E" set "IS_EXCLUDED=1"
    )
    
    REM 检查是否存在 pom.xml（Maven 项目标识）
    if exist "%%D\pom.xml" (
        if !IS_EXCLUDED!==0 (
            set /a SERVICE_COUNT+=1
            echo   [!SERVICE_COUNT!] 发现服务: !DIR_NAME!
        )
    )
)

echo.
echo [√] 共发现 !SERVICE_COUNT! 个可启动服务
echo.

if !SERVICE_COUNT!==0 (
    echo.
    echo [错误] 未发现任何可启动的服务
    echo.
    echo 请检查:
    echo   1. backend 目录是否存在
    echo   2. 服务目录中是否包含 pom.xml 文件
    echo.
    pause
    exit /b 1
)

REM ============================================
REM 启动微服务
REM ============================================
echo [步骤 4/5] 启动微服务（按依赖顺序）...
echo.

REM 按照配置顺序启动服务
for %%S in (%SERVICES_CONFIG%) do (
    set "CONFIG=%%S"
    
    REM 解析配置: 服务名:端口:配置文件:延迟
    for /f "tokens=1,2,3,4 delims=:" %%A in ("!CONFIG!") do (
        set "SERVICE_NAME=%%A"
        set "SERVICE_PORT=%%B"
        set "SERVICE_PROFILE=%%C"
        set "SERVICE_DELAY=%%D"
        
        REM 检查服务目录是否存在
        if exist "%BACKEND_DIR%\!SERVICE_NAME!\pom.xml" (
            set /a STARTED_COUNT+=1
            
            REM 构建启动命令
            set "MVN_CMD=mvn spring-boot:run"
            if not "!SERVICE_PROFILE!"=="" (
                set "MVN_CMD=!MVN_CMD! -Dspring-boot.run.profiles=!SERVICE_PROFILE!"
            )
            
            REM 显示启动信息
            echo [!STARTED_COUNT!/!SERVICE_COUNT!] 启动 !SERVICE_NAME! (端口: !SERVICE_PORT!)
            
            REM 启动服务到后台
            cd /d "%BACKEND_DIR%\!SERVICE_NAME!"
            start /B cmd /c "!MVN_CMD! > "%LOGS_DIR%\!SERVICE_NAME!.log" 2>&1"
            cd /d "%SCRIPT_DIR%"
            
            REM 等待服务初始化
            if "!SERVICE_DELAY!"=="" set "SERVICE_DELAY=3"
            timeout /t !SERVICE_DELAY! /nobreak >nul
        ) else (
            echo [跳过] !SERVICE_NAME! (目录不存在)
        )
    )
)

echo.
echo [√] 已启动 !STARTED_COUNT! 个微服务
echo.

REM ============================================
REM 启动前端
REM ============================================
echo [步骤 5/5] 启动前端服务...
echo.

if exist "%SCRIPT_DIR%frontend\package.json" (
    cd /d "%SCRIPT_DIR%frontend"
    
    REM 检查是否需要安装依赖
    if not exist "node_modules" (
        echo 首次运行，正在安装前端依赖...
        call npm install >nul 2>&1
        if %errorlevel% neq 0 (
            echo [警告] 前端依赖安装失败，请手动执行: cd frontend ^& npm install
        ) else (
            echo [√] 前端依赖安装完成
        )
    )
    
    echo 启动前端开发服务器 (端口: 5173)...
    start /B cmd /c "npm run dev > "%LOGS_DIR%\frontend.log" 2>&1"
    cd /d "%SCRIPT_DIR%"
    echo [√] 前端服务已启动
) else (
    echo [警告] 未找到前端项目
)

echo.
echo ========================================
echo       所有服务已在后台启动！
echo ========================================
echo.
echo 📊 启动统计:
echo   微服务数量:    !STARTED_COUNT!
echo   基础设施:      3 (MySQL, Redis, Nacos)
echo   前端应用:      1
echo.
echo 🌐 服务访问地址:
echo   前端应用:       http://localhost:5173
echo   API网关:        http://localhost:8080
echo   Nacos控制台:    http://localhost:8848/nacos
echo   登录凭证:       nacos / nacos
echo.
echo 💾 基础设施连接:
echo   MySQL:          localhost:3307 (root/123456)
echo   Redis:          localhost:6379
echo.
echo 📋 日志管理:
echo   日志目录:       %LOGS_DIR%
echo   查看实时日志:   pwsh -File tail-logs.ps1 [服务名]
echo   检查服务状态:   pwsh -File check-services-silent.ps1
echo.
echo 🔧 服务管理命令:
echo   重启单个服务:   pwsh -File restart-service.ps1 [服务名]
echo   停止所有服务:   stop-dev-silent.bat
echo.
echo 📝 已启动的服务列表:
for %%S in (%SERVICES_CONFIG%) do (
    for /f "tokens=1,2 delims=:" %%A in ("%%S") do (
        if exist "%BACKEND_DIR%\%%A\pom.xml" (
            echo   - %%A (端口: %%B)
        )
    )
)
echo.
echo ⏱️  提示:
echo   - 服务在后台静默运行（无CMD窗口）
echo   - 所有服务完全启动需要 1-2 分钟
echo   - 如遇问题，请查看对应服务的日志文件
echo.
echo ========================================
echo 启动完成！按任意键退出...
echo ========================================
pause >nul
exit /b 0

REM ============================================
REM 错误退出处理
REM ============================================
:error_exit
echo.
echo ========================================
echo 启动失败！
echo ========================================
echo.
echo 请根据上述错误信息解决问题后重试
echo.
echo 💡 故障排查建议:
echo   1. 运行诊断工具: diagnose.bat
echo   2. 使用调试模式: start-dev-debug.bat
echo   3. 查看文档: TROUBLESHOOTING.md
echo.
echo 按任意键退出...
pause >nul
exit /b 1

