# 启动Docker基础设施服务
# 设置UTF-8编码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "启动Docker基础设施服务" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 切换到项目目录
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

Write-Host "[1/5] 检查Docker状态..." -ForegroundColor Yellow
try {
    $dockerVersion = docker --version 2>&1
    Write-Host "  ✓ Docker版本: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Docker未安装或未运行" -ForegroundColor Red
    Write-Host ""
    Write-Host "请先启动Docker Desktop，然后重试" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "[2/5] 检查端口占用情况..." -ForegroundColor Yellow
$ports = @(3307, 6379, 8848, 9848)
$occupiedPorts = @()

foreach ($port in $ports) {
    $connection = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($connection) {
        $occupiedPorts += $port
        $process = Get-Process -Id $connection.OwningProcess -ErrorAction SilentlyContinue
        Write-Host "  ! 端口 $port 被占用 (进程: $($process.ProcessName) PID:$($process.Id))" -ForegroundColor Yellow
    }
}

if ($occupiedPorts.Count -eq 0) {
    Write-Host "  ✓ 所有端口可用" -ForegroundColor Green
}

Write-Host ""
Write-Host "[3/5] 停止现有容器..." -ForegroundColor Yellow
docker compose -f docker-compose-dev.yml down 2>&1 | Out-Null
Start-Sleep -Seconds 2
Write-Host "  ✓ 现有容器已停止" -ForegroundColor Green

Write-Host ""
Write-Host "[4/5] 启动基础设施容器..." -ForegroundColor Yellow
Write-Host "  正在拉取镜像并启动容器（首次运行可能需要较长时间）..." -ForegroundColor Gray
Write-Host ""

# 执行docker compose up并显示输出
$output = docker compose -f docker-compose-dev.yml up -d 2>&1
$output | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "  ✓ 容器启动成功" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "  ✗ 容器启动失败" -ForegroundColor Red
    Write-Host ""
    Write-Host "错误输出:" -ForegroundColor Red
    $output | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
    exit 1
}

Write-Host ""
Write-Host "[5/5] 检查容器状态..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

$containers = docker compose -f docker-compose-dev.yml ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}" 2>&1
Write-Host ""
Write-Host $containers
Write-Host ""

# 检查健康状态
Write-Host "等待服务健康检查..." -ForegroundColor Yellow
$maxWait = 60
$waited = 0
$healthy = $false

while ($waited -lt $maxWait -and -not $healthy) {
    Start-Sleep -Seconds 5
    $waited += 5
    
    $mysqlHealth = docker inspect mall-mysql-dev --format='{{.State.Health.Status}}' 2>$null
    $redisHealth = docker inspect mall-redis-dev --format='{{.State.Health.Status}}' 2>$null
    $nacosHealth = docker inspect mall-nacos-dev --format='{{.State.Health.Status}}' 2>$null
    
    Write-Host "  MySQL: $mysqlHealth | Redis: $redisHealth | Nacos: $nacosHealth (等待 ${waited}s)" -ForegroundColor Gray
    
    if ($mysqlHealth -eq "healthy" -and $redisHealth -eq "healthy" -and $nacosHealth -eq "healthy") {
        $healthy = $true
    }
}

Write-Host ""
if ($healthy) {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "✓ 所有服务启动成功并健康运行" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
} else {
    Write-Host "========================================" -ForegroundColor Yellow
    Write-Host "! 服务已启动但部分健康检查未通过" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Yellow
    Write-Host "  请等待1-2分钟让服务完全启动" -ForegroundColor Gray
}

Write-Host ""
Write-Host "服务访问地址:" -ForegroundColor White
Write-Host "  MySQL:  localhost:3307 (root/123456)" -ForegroundColor Gray
Write-Host "  Redis:  localhost:6379" -ForegroundColor Gray
Write-Host "  Nacos:  http://localhost:8848/nacos (nacos/nacos)" -ForegroundColor Gray
Write-Host ""

Write-Host "管理命令:" -ForegroundColor White
Write-Host "  查看日志: docker compose -f docker-compose-dev.yml logs -f [服务名]" -ForegroundColor Gray
Write-Host "  停止服务: docker compose -f docker-compose-dev.yml down" -ForegroundColor Gray
Write-Host "  重启服务: docker compose -f docker-compose-dev.yml restart" -ForegroundColor Gray
Write-Host ""
