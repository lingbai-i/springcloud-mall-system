# 实时查看在线商城微服务日志
# 作者: lingbai
# 版本: 1.0

param(
  [Parameter(Position = 0)]
  [ValidateSet('gateway', 'user', 'product', 'cart', 'order', 'payment', 'admin', 'merchant', 'sms', 'frontend', 'all', '')]
  [string]$ServiceName = 'all',
    
  [Parameter()]
  [int]$Lines = 20
)

# 设置UTF-8编码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

# 服务名称映射
$serviceMap = @{
  'gateway'  = @{ Name = 'Gateway Service'; File = 'gateway.log'; Color = 'Cyan' }
  'user'     = @{ Name = 'User Service'; File = 'user.log'; Color = 'Green' }
  'product'  = @{ Name = 'Product Service'; File = 'product.log'; Color = 'Yellow' }
  'cart'     = @{ Name = 'Cart Service'; File = 'cart.log'; Color = 'Magenta' }
  'order'    = @{ Name = 'Order Service'; File = 'order.log'; Color = 'Blue' }
  'payment'  = @{ Name = 'Payment Service'; File = 'payment.log'; Color = 'DarkCyan' }
  'admin'    = @{ Name = 'Admin Service'; File = 'admin.log'; Color = 'DarkGreen' }
  'merchant' = @{ Name = 'Merchant Service'; File = 'merchant.log'; Color = 'DarkYellow' }
  'sms'      = @{ Name = 'SMS Service'; File = 'sms.log'; Color = 'DarkMagenta' }
  'frontend' = @{ Name = 'Frontend'; File = 'frontend.log'; Color = 'White' }
}

# 检查logs目录
if (-not (Test-Path "logs")) {
  Write-Host "错误: logs 目录不存在" -ForegroundColor Red
  Write-Host "   请先运行 start-dev-silent.bat 启动服务" -ForegroundColor Yellow
  exit 1
}

# 显示标题
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "在线商城服务日志查看器" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 单个服务日志查看
if ($ServiceName -ne 'all' -and $ServiceName -ne '') {
  $service = $serviceMap[$ServiceName]
  $logFile = "logs\$($service.File)"
    
  if (-not (Test-Path $logFile)) {
    Write-Host "错误: 日志文件不存在: $logFile" -ForegroundColor Red
    Write-Host "   服务可能还未启动或日志文件未生成" -ForegroundColor Yellow
    exit 1
  }
    
  Write-Host "查看服务: $($service.Name)" -ForegroundColor $service.Color
  Write-Host "日志文件: $logFile" -ForegroundColor Gray
  Write-Host "实时模式: 按 Ctrl+C 退出`n" -ForegroundColor Gray
  Write-Host "----------------------------------------`n" -ForegroundColor Gray
    
  # 实时跟踪日志
  Get-Content $logFile -Wait -Tail $Lines | ForEach-Object {
    # 根据日志级别着色
    $line = $_
    if ($line -match '\bERROR\b|\bFAIL\b|\bException\b') {
      Write-Host $line -ForegroundColor Red
    }
    elseif ($line -match '\bWARN\b|\bWARNING\b') {
      Write-Host $line -ForegroundColor Yellow
    }
    elseif ($line -match '\bINFO\b|\bStarted\b|\bCompleted\b') {
      Write-Host $line -ForegroundColor Green
    }
    elseif ($line -match '\bDEBUG\b|\bTRACE\b') {
      Write-Host $line -ForegroundColor Gray
    }
    else {
      Write-Host $line
    }
  }
}
# 所有服务日志查看
else {
  Write-Host "查看所有服务日志（实时模式）" -ForegroundColor Green
  Write-Host "按 Ctrl+C 退出`n" -ForegroundColor Gray
    
  # 获取所有日志文件
  $logFiles = Get-ChildItem "logs\*.log" -File
    
  if ($logFiles.Count -eq 0) {
    Write-Host "错误: 未找到任何日志文件" -ForegroundColor Red
    Write-Host "   请先运行 start-dev-silent.bat 启动服务" -ForegroundColor Yellow
    exit 1
  }
    
  Write-Host "找到 $($logFiles.Count) 个日志文件:`n" -ForegroundColor Gray
    
  # 显示找到的日志文件
  foreach ($file in $logFiles) {
    $serviceName = $file.BaseName
    $size = [math]::Round($file.Length / 1KB, 2)
    Write-Host "  $serviceName - ${size}KB" -ForegroundColor Gray
  }
    
  Write-Host "`n----------------------------------------`n" -ForegroundColor Gray
    
  # 使用多路复用方式查看所有日志
  try {
    $logFiles | ForEach-Object {
      $serviceName = $_.BaseName
      $color = if ($serviceMap.ContainsKey($serviceName)) { 
        $serviceMap[$serviceName].Color 
      }
      else { 
        'White' 
      }
            
      # 显示每个服务的最后几行
      Write-Host "[$serviceName]" -ForegroundColor $color -NoNewline
      Write-Host " 最后 $Lines 行:" -ForegroundColor Gray
      Get-Content $_.FullName -Tail $Lines | ForEach-Object {
        Write-Host "  $_" -ForegroundColor DarkGray
      }
      Write-Host ""
    }
        
    Write-Host "`n提示: 要实时跟踪单个服务，请使用:" -ForegroundColor Yellow
    Write-Host "   pwsh -File tail-logs.ps1 <服务名>" -ForegroundColor Gray
    Write-Host "   例如: pwsh -File tail-logs.ps1 user`n" -ForegroundColor Gray
        
  }
  catch {
    Write-Host "`n读取日志文件时出错: $_" -ForegroundColor Red
  }
}

