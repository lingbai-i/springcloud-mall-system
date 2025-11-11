# 重启指定的微服务
# 作者: lingbai
# 版本: 1.0

param(
  [Parameter(Mandatory = $true, Position = 0)]
  [ValidateSet('gateway', 'user', 'product', 'cart', 'order', 'payment', 'admin', 'merchant', 'sms', 'frontend')]
  [string]$ServiceName
)

# 设置UTF-8编码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

# 服务配置
$serviceConfig = @{
  'gateway'  = @{ 
    Name    = 'Gateway Service'
    Port    = 8080
    Path    = 'backend\gateway-service'
    Command = 'mvn spring-boot:run -Dspring-boot.run.profiles=simple'
    LogFile = 'logs\gateway.log'
  }
  'user'     = @{ 
    Name    = 'User Service'
    Port    = 8082
    Path    = 'backend\user-service'
    Command = 'mvn spring-boot:run'
    LogFile = 'logs\user.log'
  }
  'product'  = @{ 
    Name    = 'Product Service'
    Port    = 8083
    Path    = 'backend\product-service'
    Command = 'mvn spring-boot:run'
    LogFile = 'logs\product.log'
  }
  'cart'     = @{ 
    Name    = 'Cart Service'
    Port    = 8088
    Path    = 'backend\cart-service'
    Command = 'mvn spring-boot:run'
    LogFile = 'logs\cart.log'
  }
  'order'    = @{ 
    Name    = 'Order Service'
    Port    = 8084
    Path    = 'backend\order-service'
    Command = 'mvn spring-boot:run'
    LogFile = 'logs\order.log'
  }
  'payment'  = @{ 
    Name    = 'Payment Service'
    Port    = 8085
    Path    = 'backend\payment-service'
    Command = 'mvn spring-boot:run'
    LogFile = 'logs\payment.log'
  }
  'admin'    = @{ 
    Name    = 'Admin Service'
    Port    = 8086
    Path    = 'backend\admin-service'
    Command = 'mvn spring-boot:run'
    LogFile = 'logs\admin.log'
  }
  'merchant' = @{ 
    Name    = 'Merchant Service'
    Port    = 8087
    Path    = 'backend\merchant-service'
    Command = 'mvn spring-boot:run'
    LogFile = 'logs\merchant.log'
  }
  'sms'      = @{ 
    Name    = 'SMS Service'
    Port    = 8089
    Path    = 'backend\sms-service'
    Command = 'mvn spring-boot:run'
    LogFile = 'logs\sms.log'
  }
  'frontend' = @{ 
    Name    = 'Frontend'
    Port    = 5173
    Path    = 'frontend'
    Command = 'npm run dev'
    LogFile = 'logs\frontend.log'
  }
}

$service = $serviceConfig[$ServiceName]

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "重启服务: $($service.Name)" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 步骤1: 查找并停止占用端口的进程
Write-Host "查找端口 $($service.Port) 的进程..." -ForegroundColor Yellow

$connections = netstat -ano | Select-String ":$($service.Port) "
if ($connections) {
  # 提取PID
  $pids = $connections | ForEach-Object {
    if ($_ -match '\s+(\d+)\s*$') {
      $matches[1]
    }
  } | Select-Object -Unique

  foreach ($procId in $pids) {
    try {
      $process = Get-Process -Id $procId -ErrorAction SilentlyContinue
      if ($process) {
        Write-Host "   停止进程: $($process.Name) (PID: $procId)" -ForegroundColor Gray
        Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 2
      }
    }
    catch {
      Write-Host "   无法停止进程 PID: $pid" -ForegroundColor DarkYellow
    }
  }
  Write-Host "   旧进程已停止" -ForegroundColor Green
}
else {
  Write-Host "   端口未被占用" -ForegroundColor Gray
}

# 步骤2: 添加分隔符到日志文件
Write-Host "`n准备日志文件..." -ForegroundColor Yellow

if (Test-Path $service.LogFile) {
  $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
  $separator = "`n========== 服务重启于 $timestamp ==========`n"
  Add-Content -Path $service.LogFile -Value $separator
  Write-Host "   日志分隔符已添加" -ForegroundColor Green
}
else {
  # 确保logs目录存在
  if (-not (Test-Path "logs")) {
    New-Item -ItemType Directory -Path "logs" | Out-Null
  }
  Write-Host "   创建新日志文件" -ForegroundColor Green
}

# 步骤3: 启动服务
Write-Host "`n启动服务..." -ForegroundColor Yellow

$scriptPath = $PSScriptRoot
if ([string]::IsNullOrEmpty($scriptPath)) {
  $scriptPath = Get-Location
}

$fullPath = Join-Path $scriptPath $service.Path
$logPath = Join-Path $scriptPath $service.LogFile

# 构建启动命令
$startCommand = "cmd /c `"cd /d `"$fullPath`" && $($service.Command) > `"$logPath`" 2>&1`""

# 后台启动
Start-Process -FilePath "cmd.exe" -ArgumentList "/c", $startCommand -WindowStyle Hidden

Write-Host "   服务已启动（后台运行）" -ForegroundColor Green

# 步骤4: 等待并验证
Write-Host "`n等待服务启动..." -ForegroundColor Yellow
Write-Host "   等待15秒..." -ForegroundColor Gray

Start-Sleep -Seconds 15

# 检查端口
$portCheck = netstat -ano | Select-String ":$($service.Port) "
if ($portCheck) {
  Write-Host "   服务启动成功！端口 $($service.Port) 已监听" -ForegroundColor Green
}
else {
  Write-Host "   端口 $($service.Port) 未监听，服务可能还在启动中" -ForegroundColor Yellow
  Write-Host "   请使用以下命令查看日志:" -ForegroundColor Gray
  Write-Host "      pwsh -File tail-logs.ps1 $ServiceName" -ForegroundColor White
}

# 显示日志最后几行
Write-Host "`n最近日志（最后10行）:" -ForegroundColor Cyan
Write-Host "----------------------------------------" -ForegroundColor Gray

if (Test-Path $service.LogFile) {
  Get-Content $service.LogFile -Tail 10 | ForEach-Object {
    if ($_ -match 'ERROR|Exception') {
      Write-Host $_ -ForegroundColor Red
    }
    elseif ($_ -match 'WARN') {
      Write-Host $_ -ForegroundColor Yellow
    }
    elseif ($_ -match 'Started|Completed') {
      Write-Host $_ -ForegroundColor Green
    }
    else {
      Write-Host $_ -ForegroundColor Gray
    }
  }
}
else {
  Write-Host "（日志文件尚未生成）" -ForegroundColor Gray
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "重启完成！" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "提示:" -ForegroundColor Yellow
Write-Host "   - 查看实时日志: pwsh -File tail-logs.ps1 $ServiceName" -ForegroundColor Gray
Write-Host "   - 检查服务状态: pwsh -File check-services-silent.ps1" -ForegroundColor Gray
Write-Host ""

