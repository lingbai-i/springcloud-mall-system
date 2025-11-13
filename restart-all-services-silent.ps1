# 在线商城 - 完整重启所有服务（静默模式）
# 作者: system
# 版本: 1.0
# 更新日期: 2025-11-12

# 设置UTF-8编码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "在线商城 - 服务完整重启（静默模式）" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 获取脚本目录
$scriptDir = $PSScriptRoot
$backendDir = Join-Path $scriptDir "backend"
$logsDir = Join-Path $scriptDir "logs"

# 确保日志目录存在
if (-not (Test-Path $logsDir)) {
    New-Item -ItemType Directory -Path $logsDir | Out-Null
}

# 服务配置（按启动顺序）
$servicesConfig = @(
    @{ Name = "gateway-service"; Port = 8080; Profile = "simple"; Delay = 5 },
    @{ Name = "auth-service"; Port = 8081; Profile = ""; Delay = 3 },
    @{ Name = "user-service"; Port = 8082; Profile = ""; Delay = 3 },
    @{ Name = "product-service"; Port = 8083; Profile = ""; Delay = 3 },
    @{ Name = "order-service"; Port = 8084; Profile = ""; Delay = 3 },
    @{ Name = "payment-service"; Port = 8085; Profile = ""; Delay = 3 },
    @{ Name = "admin-service"; Port = 8086; Profile = ""; Delay = 3 },
    @{ Name = "merchant-service"; Port = 8087; Profile = ""; Delay = 3 },
    @{ Name = "cart-service"; Port = 8088; Profile = ""; Delay = 3 },
    @{ Name = "sms-service"; Port = 8089; Profile = ""; Delay = 3 }
)

# ============================================
# 第一步：停止所有服务
# ============================================
Write-Host "[步骤 1/4] 停止所有正在运行的服务..." -ForegroundColor Yellow
Write-Host ""

# 停止所有Java进程
Write-Host "  停止所有微服务进程..." -ForegroundColor Gray
Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Write-Host "  ✓ 微服务进程已停止" -ForegroundColor Green

# 停止Node.js进程
Write-Host "  停止前端服务进程..." -ForegroundColor Gray
Get-Process -Name node -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Write-Host "  ✓ 前端服务进程已停止" -ForegroundColor Green

# 停止Docker容器
Write-Host "  停止Docker基础设施..." -ForegroundColor Gray
Set-Location $scriptDir
docker-compose -f docker-compose-dev.yml down 2>&1 | Out-Null
Write-Host "  ✓ Docker容器已停止" -ForegroundColor Green

Write-Host ""
Write-Host "等待端口释放 (5秒)..." -ForegroundColor Gray
Start-Sleep -Seconds 5

# ============================================
# 第二步：启动基础设施
# ============================================
Write-Host "[步骤 2/4] 启动基础设施 (MySQL, Redis, Nacos)..." -ForegroundColor Yellow
Write-Host ""

Set-Location $scriptDir
$dockerProcess = Start-Process -FilePath "docker-compose" -ArgumentList "-f", "docker-compose-dev.yml", "up", "-d" -NoNewWindow -Wait -PassThru

if ($dockerProcess.ExitCode -eq 0) {
    Write-Host "  ✓ 基础设施启动成功" -ForegroundColor Green
} else {
    Write-Host "  ✗ 基础设施启动失败" -ForegroundColor Red
    Write-Host ""
    Write-Host "请检查:" -ForegroundColor Yellow
    Write-Host "  1. Docker Desktop 是否正在运行" -ForegroundColor Gray
    Write-Host "  2. 端口是否被占用 (3307, 6379, 8848)" -ForegroundColor Gray
    exit 1
}

Write-Host ""
Write-Host "等待基础设施初始化 (20秒)..." -ForegroundColor Gray
Start-Sleep -Seconds 20
Write-Host "  ✓ 基础设施就绪" -ForegroundColor Green

# ============================================
# 第三步：启动微服务
# ============================================
Write-Host ""
Write-Host "[步骤 3/4] 启动微服务（按依赖顺序）..." -ForegroundColor Yellow
Write-Host ""

$startedCount = 0
foreach ($service in $servicesConfig) {
    $servicePath = Join-Path $backendDir $service.Name
    $pomFile = Join-Path $servicePath "pom.xml"
    
    if (Test-Path $pomFile) {
        $startedCount++
        $logFile = Join-Path $logsDir "$($service.Name).log"
        
        Write-Host "  [$startedCount/$($servicesConfig.Count)] 启动 $($service.Name) (端口: $($service.Port))..." -ForegroundColor Cyan
        
        # 构建Maven命令
        $mvnArgs = @("spring-boot:run")
        if ($service.Profile -ne "") {
            $mvnArgs += "-Dspring-boot.run.profiles=$($service.Profile)"
        }
        
        # 在后台启动服务
        Set-Location $servicePath
        
        # 使用 Start-Process 启动后台进程
        $processInfo = New-Object System.Diagnostics.ProcessStartInfo
        $processInfo.FileName = "cmd.exe"
        $processInfo.Arguments = "/c mvn $($mvnArgs -join ' ') > `"$logFile`" 2>&1"
        $processInfo.WorkingDirectory = $servicePath
        $processInfo.WindowStyle = [System.Diagnostics.ProcessWindowStyle]::Hidden
        $processInfo.CreateNoWindow = $true
        $processInfo.UseShellExecute = $false
        
        $process = New-Object System.Diagnostics.Process
        $process.StartInfo = $processInfo
        $process.Start() | Out-Null
        
        Write-Host "    进程ID: $($process.Id)" -ForegroundColor Gray
        
        # 等待服务初始化
        if ($service.Delay -gt 0) {
            Start-Sleep -Seconds $service.Delay
        }
    } else {
        Write-Host "  [跳过] $($service.Name) (目录不存在)" -ForegroundColor Yellow
    }
}

Set-Location $scriptDir

Write-Host ""
Write-Host "  ✓ 已启动 $startedCount 个微服务" -ForegroundColor Green

# ============================================
# 第四步：启动前端
# ============================================
Write-Host ""
Write-Host "[步骤 4/4] 启动前端服务..." -ForegroundColor Yellow
Write-Host ""

$frontendDir = Join-Path $scriptDir "frontend"
if (Test-Path (Join-Path $frontendDir "package.json")) {
    Set-Location $frontendDir
    
    # 检查 node_modules
    if (-not (Test-Path "node_modules")) {
        Write-Host "  首次运行，正在安装前端依赖..." -ForegroundColor Gray
        npm install 2>&1 | Out-Null
    }
    
    Write-Host "  启动前端开发服务器 (端口: 5173)..." -ForegroundColor Cyan
    
    $frontendLog = Join-Path $logsDir "frontend.log"
    
    # 启动前端服务
    $processInfo = New-Object System.Diagnostics.ProcessStartInfo
    $processInfo.FileName = "cmd.exe"
    $processInfo.Arguments = "/c npm run dev > `"$frontendLog`" 2>&1"
    $processInfo.WorkingDirectory = $frontendDir
    $processInfo.WindowStyle = [System.Diagnostics.ProcessWindowStyle]::Hidden
    $processInfo.CreateNoWindow = $true
    $processInfo.UseShellExecute = $false
    
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $processInfo
    $process.Start() | Out-Null
    
    Write-Host "  ✓ 前端服务已启动" -ForegroundColor Green
} else {
    Write-Host "  [警告] 未找到前端项目" -ForegroundColor Yellow
}

Set-Location $scriptDir

# ============================================
# 完成
# ============================================
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "      所有服务已在后台启动！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "📊 启动统计:" -ForegroundColor White
Write-Host "  微服务数量:    $startedCount" -ForegroundColor Gray
Write-Host "  基础设施:      3 (MySQL, Redis, Nacos)" -ForegroundColor Gray
Write-Host "  前端应用:      1" -ForegroundColor Gray
Write-Host ""

Write-Host "🌐 服务访问地址:" -ForegroundColor White
Write-Host "  前端应用:       http://localhost:5173" -ForegroundColor Cyan
Write-Host "  API网关:        http://localhost:8080" -ForegroundColor Cyan
Write-Host "  Nacos控制台:    http://localhost:8848/nacos" -ForegroundColor Cyan
Write-Host "  登录凭证:       nacos / nacos" -ForegroundColor Gray
Write-Host ""

Write-Host "💾 基础设施连接:" -ForegroundColor White
Write-Host "  MySQL:          localhost:3307 (root/123456)" -ForegroundColor Gray
Write-Host "  Redis:          localhost:6379" -ForegroundColor Gray
Write-Host ""

Write-Host "📋 日志管理:" -ForegroundColor White
Write-Host "  日志目录:       $logsDir" -ForegroundColor Gray
Write-Host "  查看实时日志:   pwsh -File tail-logs.ps1 [服务名]" -ForegroundColor Gray
Write-Host "  检查服务状态:   pwsh -File check-services-silent.ps1" -ForegroundColor Gray
Write-Host ""

Write-Host "📝 已启动的服务列表:" -ForegroundColor White
foreach ($service in $servicesConfig) {
    if (Test-Path (Join-Path $backendDir "$($service.Name)\pom.xml")) {
        Write-Host "  - $($service.Name) (端口: $($service.Port))" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "⏱️  提示:" -ForegroundColor White
Write-Host "  - 服务在后台静默运行（无CMD窗口）" -ForegroundColor Gray
Write-Host "  - 所有服务完全启动需要 1-2 分钟" -ForegroundColor Gray
Write-Host "  - 如遇问题，请查看对应服务的日志文件" -ForegroundColor Gray
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "重启完成！准备进行商家入驻审批测试" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

