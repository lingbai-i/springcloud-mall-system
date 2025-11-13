# 启动所有微服务和前端
# 设置UTF-8编码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "启动所有微服务和前端应用" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Join-Path $scriptDir "backend"
$frontendDir = Join-Path $scriptDir "frontend"
$logsDir = Join-Path $scriptDir "logs"

# 确保日志目录存在
if (-not (Test-Path $logsDir)) {
    New-Item -ItemType Directory -Path $logsDir | Out-Null
}

# 服务配置（按启动顺序）
$services = @(
    @{ Name = "gateway-service"; Port = 8080; Profile = "simple"; Delay = 8 },
    @{ Name = "auth-service"; Port = 8081; Profile = ""; Delay = 5 },
    @{ Name = "user-service"; Port = 8082; Profile = ""; Delay = 5 },
    @{ Name = "product-service"; Port = 8083; Profile = ""; Delay = 5 },
    @{ Name = "order-service"; Port = 8084; Profile = ""; Delay = 5 },
    @{ Name = "payment-service"; Port = 8085; Profile = ""; Delay = 5 },
    @{ Name = "admin-service"; Port = 8086; Profile = ""; Delay = 5 },
    @{ Name = "merchant-service"; Port = 8087; Profile = ""; Delay = 5 },
    @{ Name = "cart-service"; Port = 8088; Profile = ""; Delay = 5 },
    @{ Name = "sms-service"; Port = 8089; Profile = ""; Delay = 3 }
)

Write-Host "[步骤 1/3] 检查基础设施状态..." -ForegroundColor Yellow
Write-Host ""

# 检查Docker容器
$dockerContainers = docker ps --filter "name=mall-" --format "{{.Names}}" 2>$null
if ($dockerContainers) {
    Write-Host "  ✓ 检测到运行中的Docker容器:" -ForegroundColor Green
    $dockerContainers | ForEach-Object { Write-Host "    - $_" -ForegroundColor Gray }
} else {
    Write-Host "  ✗ 未检测到Docker容器运行" -ForegroundColor Red
    Write-Host "    请先运行 start-docker.bat 启动基础设施" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "[步骤 2/3] 启动后端微服务..." -ForegroundColor Yellow
Write-Host ""

$startedCount = 0
$totalServices = $services.Count

foreach ($service in $services) {
    $servicePath = Join-Path $backendDir $service.Name
    $pomFile = Join-Path $servicePath "pom.xml"
    
    if (-not (Test-Path $pomFile)) {
        Write-Host "  [跳过] $($service.Name) - 目录不存在" -ForegroundColor Yellow
        continue
    }
    
    $startedCount++
    $logFile = Join-Path $logsDir "$($service.Name).log"
    
    Write-Host "  [$startedCount/$totalServices] 启动 $($service.Name) (端口: $($service.Port))..." -ForegroundColor Cyan
    
    # 构建Maven命令
    $mvnArgs = "spring-boot:run"
    if ($service.Profile) {
        $mvnArgs += " `"-Dspring-boot.run.profiles=$($service.Profile)`""
    }
    
    # 使用cmd在后台启动服务
    $startCmd = "cd /d `"$servicePath`" && mvn $mvnArgs > `"$logFile`" 2>&1"
    
    Start-Process -FilePath "cmd.exe" -ArgumentList "/c", $startCmd -WindowStyle Hidden -PassThru | Out-Null
    
    Write-Host "    日志文件: $logFile" -ForegroundColor Gray
    
    # 等待服务初始化
    if ($startedCount -eq 1) {
        # 第一个服务（Gateway）等待更长时间
        Write-Host "    等待网关服务初始化..." -ForegroundColor Gray
    }
    Start-Sleep -Seconds $service.Delay
}

Write-Host ""
Write-Host "  ✓ 已启动 $startedCount 个微服务" -ForegroundColor Green

Write-Host ""
Write-Host "[步骤 3/3] 启动前端服务..." -ForegroundColor Yellow
Write-Host ""

if (Test-Path (Join-Path $frontendDir "package.json")) {
    # 检查node_modules
    if (-not (Test-Path (Join-Path $frontendDir "node_modules"))) {
        Write-Host "  首次运行，正在安装前端依赖..." -ForegroundColor Gray
        Set-Location $frontendDir
        npm install 2>&1 | Out-Null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✓ 依赖安装完成" -ForegroundColor Green
        } else {
            Write-Host "  ! 依赖安装可能失败，继续启动..." -ForegroundColor Yellow
        }
    }
    
    Write-Host "  启动前端开发服务器 (端口: 5173)..." -ForegroundColor Cyan
    
    $frontendLog = Join-Path $logsDir "frontend.log"
    
    $startCmd = "cd /d `"$frontendDir`" && npm run dev > `"$frontendLog`" 2>&1"
    
    Start-Process -FilePath "cmd.exe" -ArgumentList "/c", $startCmd -WindowStyle Hidden -PassThru | Out-Null
    
    Write-Host "  ✓ 前端服务已启动" -ForegroundColor Green
    Write-Host "    日志文件: $frontendLog" -ForegroundColor Gray
} else {
    Write-Host "  ! 未找到前端项目" -ForegroundColor Yellow
}

Set-Location $scriptDir

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "所有服务已在后台启动！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

Write-Host "📊 启动统计:" -ForegroundColor White
Write-Host "  基础设施:      4 (MySQL, Redis, Nacos, MinIO)" -ForegroundColor Gray
Write-Host "  微服务数量:    $startedCount" -ForegroundColor Gray
Write-Host "  前端应用:      1" -ForegroundColor Gray
Write-Host ""

Write-Host "🌐 服务访问地址:" -ForegroundColor White
Write-Host "  前端应用:       http://localhost:5173" -ForegroundColor Gray
Write-Host "  API网关:        http://localhost:8080" -ForegroundColor Gray
Write-Host "  Nacos控制台:    http://localhost:8848/nacos (nacos/nacos)" -ForegroundColor Gray
Write-Host ""

Write-Host "💾 基础设施连接:" -ForegroundColor White
Write-Host "  MySQL:          localhost:3307 (root/123456)" -ForegroundColor Gray
Write-Host "  Redis:          localhost:6379" -ForegroundColor Gray
Write-Host ""

Write-Host "📋 日志管理:" -ForegroundColor White
Write-Host "  日志目录:       $logsDir" -ForegroundColor Gray
Write-Host "  查看日志:       Get-Content logs\<服务名>.log -Tail 50 -Wait" -ForegroundColor Gray
Write-Host ""

Write-Host "🔧 管理命令:" -ForegroundColor White
Write-Host "  检查状态:       pwsh -File check-status.ps1" -ForegroundColor Gray
Write-Host "  停止服务:       taskkill /F /IM java.exe" -ForegroundColor Gray
Write-Host "  查看端口占用:   netstat -ano | findstr `"8080 8081 8082`"" -ForegroundColor Gray
Write-Host ""

Write-Host "⏱️  提示:" -ForegroundColor White
Write-Host "  - 所有服务在后台运行，不会弹出窗口" -ForegroundColor Gray
Write-Host "  - 服务完全启动需要 2-3 分钟" -ForegroundColor Gray
Write-Host "  - 请等待所有服务注册到Nacos后再访问" -ForegroundColor Gray
Write-Host "  - 可以在Nacos控制台查看服务注册情况" -ForegroundColor Gray
Write-Host ""

Write-Host "🎯 后续操作:" -ForegroundColor White
Write-Host "  1. 等待2-3分钟让所有服务完全启动" -ForegroundColor Gray
Write-Host "  2. 访问 http://localhost:8848/nacos 检查服务注册" -ForegroundColor Gray
Write-Host "  3. 访问 http://localhost:5173 打开前端应用" -ForegroundColor Gray
Write-Host "  4. 如有问题，查看对应服务的日志文件" -ForegroundColor Gray
Write-Host ""
