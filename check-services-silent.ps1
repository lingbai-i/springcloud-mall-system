# 检查在线商城所有服务的运行状态
# 作者: lingbai
# 版本: 1.0

# 设置UTF-8编码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

# 服务配置
$services = @{
  '基础设施' = @(
    @{ Name = 'MySQL'; Port = 3307; Container = 'mall-mysql-dev' }
    @{ Name = 'Redis'; Port = 6379; Container = 'mall-redis-dev' }
    @{ Name = 'Nacos'; Port = 8848; Container = 'mall-nacos-dev' }
  )
  '微服务'  = @(
    @{ Name = 'Gateway Service'; Port = 8080; LogFile = 'logs\gateway.log' }
    @{ Name = 'User Service'; Port = 8082; LogFile = 'logs\user.log' }
    @{ Name = 'Product Service'; Port = 8083; LogFile = 'logs\product.log' }
    @{ Name = 'Order Service'; Port = 8084; LogFile = 'logs\order.log' }
    @{ Name = 'Payment Service'; Port = 8085; LogFile = 'logs\payment.log' }
    @{ Name = 'Admin Service'; Port = 8086; LogFile = 'logs\admin.log' }
    @{ Name = 'Merchant Service'; Port = 8087; LogFile = 'logs\merchant.log' }
    @{ Name = 'Cart Service'; Port = 8088; LogFile = 'logs\cart.log' }
    @{ Name = 'SMS Service'; Port = 8089; LogFile = 'logs\sms.log' }
  )
  '前端'   = @(
    @{ Name = 'Frontend'; Port = 5173; LogFile = 'logs\frontend.log' }
  )
}

# 辅助函数：检查端口
function Test-Port {
  param([int]$Port)
  $result = netstat -ano | Select-String ":$Port " -Quiet
  return $result
}

# 辅助函数：检查Docker容器
function Test-Container {
  param([string]$ContainerName)
  try {
    $container = docker ps --filter "name=$ContainerName" --format "{{.Status}}" 2>$null
    return $container
  }
  catch {
    return $null
  }
}

# 辅助函数：获取日志文件大小
function Get-LogSize {
  param([string]$LogFile)
  if (Test-Path $LogFile) {
    $size = (Get-Item $LogFile).Length
    if ($size -lt 1KB) {
      return "$([math]::Round($size, 2)) B"
    }
    elseif ($size -lt 1MB) {
      return "$([math]::Round($size / 1KB, 2)) KB"
    }
    else {
      return "$([math]::Round($size / 1MB, 2)) MB"
    }
  }
  return "0 B"
}

# 显示标题
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "在线商城服务状态检查" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "检查时间: $timestamp" -ForegroundColor Gray
Write-Host ""

# 统计变量
$totalServices = 0
$runningServices = 0

# 检查Docker基础设施
Write-Host "Docker 基础设施:" -ForegroundColor Green
Write-Host "----------------------------------------" -ForegroundColor Gray

foreach ($service in $services['基础设施']) {
  $totalServices++
  $status = Test-Container $service.Container
  $portStatus = Test-Port $service.Port
    
  Write-Host "  $($service.Name) " -NoNewline
    
  if ($status -and $status -match "Up") {
    Write-Host "[运行中]" -ForegroundColor Green -NoNewline
    $runningServices++
        
    if ($status -match "healthy") {
      Write-Host " (健康)" -ForegroundColor Green -NoNewline
    }
    elseif ($status -match "starting") {
      Write-Host " (启动中)" -ForegroundColor Yellow -NoNewline
    }
        
    Write-Host " - 端口 $($service.Port)" -ForegroundColor Gray
  }
  else {
    Write-Host "[未运行]" -ForegroundColor Red -NoNewline
    Write-Host " - 端口 $($service.Port)" -ForegroundColor Gray
  }
}

# 检查微服务
Write-Host "`n后端微服务:" -ForegroundColor Green
Write-Host "----------------------------------------" -ForegroundColor Gray

foreach ($service in $services['微服务']) {
  $totalServices++
  $portStatus = Test-Port $service.Port
  $logSize = if ($service.LogFile) { Get-LogSize $service.LogFile } else { "N/A" }
    
  Write-Host "  $($service.Name) " -NoNewline
    
  if ($portStatus) {
    Write-Host "[运行中]" -ForegroundColor Green -NoNewline
    $runningServices++
    Write-Host " - 端口 $($service.Port) - 日志 $logSize" -ForegroundColor Gray
  }
  else {
    Write-Host "[未运行]" -ForegroundColor Red -NoNewline
    if ($service.LogFile -and (Test-Path $service.LogFile)) {
      Write-Host " - 端口 $($service.Port) - 日志 $logSize" -ForegroundColor DarkGray
    }
    else {
      Write-Host " - 端口 $($service.Port)" -ForegroundColor DarkGray
    }
  }
}

# 检查前端
Write-Host "`n前端服务:" -ForegroundColor Green
Write-Host "----------------------------------------" -ForegroundColor Gray

foreach ($service in $services['前端']) {
  $totalServices++
  $portStatus = Test-Port $service.Port
  $logSize = if ($service.LogFile) { Get-LogSize $service.LogFile } else { "N/A" }
    
  Write-Host "  $($service.Name) " -NoNewline
    
  if ($portStatus) {
    Write-Host "[运行中]" -ForegroundColor Green -NoNewline
    $runningServices++
    Write-Host " - 端口 $($service.Port) - 日志 $logSize" -ForegroundColor Gray
  }
  else {
    Write-Host "[未运行]" -ForegroundColor Red -NoNewline
    Write-Host " - 端口 $($service.Port)" -ForegroundColor DarkGray
  }
}

# 显示统计信息
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "统计信息" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$percentage = [math]::Round(($runningServices / $totalServices) * 100, 1)
$color = if ($percentage -eq 100) { 'Green' } elseif ($percentage -ge 70) { 'Yellow' } else { 'Red' }

Write-Host "  运行中: " -NoNewline
Write-Host "$runningServices" -ForegroundColor $color -NoNewline
Write-Host " / $totalServices 服务 " -NoNewline
Write-Host "($percentage%)" -ForegroundColor $color

# 显示访问地址
if ($runningServices -gt 0) {
  Write-Host "`n服务访问地址:" -ForegroundColor Green
    
  if (Test-Port 5173) {
    Write-Host "  前端应用:    " -NoNewline -ForegroundColor Gray
    Write-Host "http://localhost:5173" -ForegroundColor White
  }
    
  if (Test-Port 8080) {
    Write-Host "  API网关:     " -NoNewline -ForegroundColor Gray
    Write-Host "http://localhost:8080" -ForegroundColor White
  }
    
  if (Test-Port 8848) {
    Write-Host "  Nacos控制台: " -NoNewline -ForegroundColor Gray
    Write-Host "http://localhost:8848/nacos" -ForegroundColor White -NoNewline
    Write-Host " (nacos/nacos)" -ForegroundColor DarkGray
  }
}

# 显示操作提示
Write-Host "`n常用操作:" -ForegroundColor Yellow
Write-Host "  查看日志:   pwsh -File tail-logs.ps1 [服务名]" -ForegroundColor Gray
Write-Host "  重启服务:   pwsh -File restart-service.ps1 [服务名]" -ForegroundColor Gray
Write-Host "  停止所有:   stop-dev-silent.bat" -ForegroundColor Gray
Write-Host "  启动所有:   start-dev-silent.bat" -ForegroundColor Gray

Write-Host ""

