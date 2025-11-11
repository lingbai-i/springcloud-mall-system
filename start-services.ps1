# 一键启动在线商城本地开发环境（基础设施 + 后端微服务 + 前端可选）
# 作者: lingbai
# 版本: 1.0

param(
  [ValidateSet('docker','local','hybrid')]
  [string]$Mode = 'hybrid',
  [switch]$StartFrontend = $true,
  [switch]$StartSmsLocal = $false,
  [int]$WaitSeconds = 30
)

# 简单时间戳函数，用于日志
function Get-Ts {
  return (Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
}

# 统一日志输出（带级别）
function Write-Log {
  param(
    [string]$Level = 'INFO',
    [string]$Message,
    [hashtable]$Meta
  )
  $ts = Get-Ts
  if ($Meta) {
    Write-Host "[$ts] [$Level] $Message" -ForegroundColor Cyan
    Write-Host (ConvertTo-Json $Meta -Depth 3) -ForegroundColor DarkCyan
  } else {
    Write-Host "[$ts] [$Level] $Message" -ForegroundColor Cyan
  }
}

# 检查必要命令是否可用
function Ensure-Command {
  param([string]$Name)
  $exists = Get-Command $Name -ErrorAction SilentlyContinue
  if (-not $exists) {
    throw "缺少必要命令：$Name，请先安装/配置环境变量"
  }
}

# 端口健康检查
function Wait-Port {
  param(
    [string]$Host,
    [int]$Port,
    [int]$TimeoutSec = 60
  )
  $deadline = (Get-Date).AddSeconds($TimeoutSec)
  while ((Get-Date) -lt $deadline) {
    try {
      $tcpClient = New-Object System.Net.Sockets.TcpClient
      $async = $tcpClient.BeginConnect($Host, $Port, $null, $null)
      $connected = $async.AsyncWaitHandle.WaitOne(1000)
      if ($connected -and $tcpClient.Connected) {
        $tcpClient.Close()
        Write-Log -Level 'INFO' -Message "端口可用：$Host:$Port"
        return $true
      }
      $tcpClient.Close()
    } catch {
      Start-Sleep -Milliseconds 500
    }
  }
  Write-Log -Level 'WARN' -Message "端口等待超时：$Host:$Port"
  return $false
}

# 启动 Docker 基础设施
function Start-Infrastructure {
  Write-Log -Message '开始启动 Docker 基础设施（MySQL、Redis、Nacos）'
  Ensure-Command -Name 'docker-compose'
  & docker-compose -f docker-compose-dev.yml up -d mysql redis nacos | Out-Null
  Write-Log -Message '基础设施已发起启动，等待端口可用'
  Wait-Port -Host 'localhost' -Port 3307 -TimeoutSec 60 | Out-Null
  Wait-Port -Host 'localhost' -Port 6379 -TimeoutSec 60 | Out-Null
  Wait-Port -Host 'localhost' -Port 8848 -TimeoutSec 60 | Out-Null
}

# 启动 SMS 服务（docker 或本地）
function Start-SmsService {
  param([switch]$Local)
  if ($Local) {
    Write-Log -Message '以本地模式启动 sms-service（mvn spring-boot:run 端口 8083）'
    Ensure-Command -Name 'mvn'
    $svcPath = Join-Path $PSScriptRoot 'backend\sms-service'
    if (Test-Path $svcPath) {
      Start-Process -FilePath 'cmd.exe' -ArgumentList "/k cd /d `"$svcPath`" && mvn spring-boot:run" -WorkingDirectory $svcPath
      Start-Sleep -Seconds 2
      Wait-Port -Host 'localhost' -Port 8083 -TimeoutSec 60 | Out-Null
    } else {
      Write-Log -Level 'WARN' -Message '未找到 backend/sms-service 目录，跳过本地启动'
    }
  } else {
    Write-Log -Message '以 Docker 模式启动 sms-service（容器端口 8083）'
    Ensure-Command -Name 'docker-compose'
    & docker-compose -f docker-compose-dev.yml up -d sms-service | Out-Null
    Wait-Port -Host 'localhost' -Port 8083 -TimeoutSec 60 | Out-Null
  }
}

# 启动一个本地微服务（新终端窗口）
function Start-JavaService {
  param(
    [string]$Name,
    [string]$RelPath
  )
  Ensure-Command -Name 'mvn'
  $svcPath = Join-Path $PSScriptRoot $RelPath
  if (Test-Path $svcPath) {
    Write-Log -Message "启动本地服务：$Name ($RelPath)"
    Start-Process -FilePath 'cmd.exe' -ArgumentList "/k cd /d `"$svcPath`" && mvn spring-boot:run" -WorkingDirectory $svcPath
  } else {
    Write-Log -Level 'WARN' -Message "服务目录缺失，跳过：$Name ($RelPath)"
  }
}

# 启动前端开发服务器
function Start-FrontendDev {
  $fePath = Join-Path $PSScriptRoot 'frontend'
  if (-not (Test-Path $fePath)) {
    Write-Log -Level 'WARN' -Message '未找到 frontend 目录，跳过前端启动'
    return
  }
  Ensure-Command -Name 'npm'
  Write-Log -Message '启动前端开发服务器（Vite）'
  $hasNodeModules = Test-Path (Join-Path $fePath 'node_modules')
  $cmd = $hasNodeModules ? 'npm run dev' : 'npm install && npm run dev'
  Start-Process -FilePath 'cmd.exe' -ArgumentList "/k cd /d `"$fePath`" && $cmd" -WorkingDirectory $fePath
  Write-Log -Message '前端预览地址可能为：http://localhost:5173/ 或自动切换的 5174/5175'
}

# -------------------------------
# 主流程
# -------------------------------
Write-Host '========================================='
Write-Host '  基于SpringCloud的在线商城 - 本地启动脚本'
Write-Host "  模式: $Mode"
Write-Host '========================================='

try {
  switch ($Mode) {
    'docker' {
      Start-Infrastructure
      if ($StartSmsLocal) {
        Write-Log -Level 'WARN' -Message 'docker 模式下不建议本地启动 sms-service，改为容器模式'
      }
      Start-SmsService -Local:$false
    }
    'local' {
      # 仅启动本地后端微服务
      if ($StartSmsLocal) { Start-SmsService -Local:$true } else { Write-Log -Level 'WARN' -Message 'local 模式未启用短信服务；如需联调请添加 -StartSmsLocal' }
      Start-JavaService -Name 'auth-service' -RelPath 'backend\auth-service'
      Start-JavaService -Name 'user-service' -RelPath 'backend\user-service'
      Start-JavaService -Name 'cart-service' -RelPath 'backend\cart-service'
    }
    default {
      # hybrid：基础设施 + 本地微服务
      Start-Infrastructure
      if ($StartSmsLocal) { Start-SmsService -Local:$true } else { Start-SmsService -Local:$false }
      Start-JavaService -Name 'auth-service' -RelPath 'backend\auth-service'
      Start-JavaService -Name 'user-service' -RelPath 'backend\user-service'
      Start-JavaService -Name 'cart-service' -RelPath 'backend\cart-service'
    }
  }

  if ($StartFrontend) { Start-FrontendDev }

  Write-Host '========================================='
  Write-Host '  启动流程已完成（进程在独立窗口中运行）'
  Write-Host '  如需停止服务，请在各窗口中 Ctrl+C 或关闭窗口'
  Write-Host '========================================='
} catch {
  Write-Log -Level 'ERROR' -Message '启动过程中出现异常' -Meta @{ error = $_.Exception.Message; stack = $_.Exception.StackTrace }
  throw
}

