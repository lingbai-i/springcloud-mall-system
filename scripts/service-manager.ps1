# 在线商城 - 服务管理工具
# 作者: lingbai
# 版本: 1.0
# 更新日期: 2025-11-11

param(
    [Parameter(Position=0)]
    [string]$Action = "menu",
    
    [Parameter(Position=1)]
    [string]$ServiceName = ""
)

# 设置UTF-8编码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

# 服务端口映射
$servicePortMap = @{
    'gateway-service' = 8080
    'auth-service' = 8081
    'user-service' = 8082
    'product-service' = 8083
    'order-service' = 8084
    'payment-service' = 8085
    'admin-service' = 8086
    'merchant-service' = 8087
    'cart-service' = 8088
    'sms-service' = 8089
}

# 排除的目录
$excludeDirs = @('common-bom', 'common-core', 'simple-test')

# 颜色输出函数
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

# 显示标题
function Show-Header {
    Clear-Host
    Write-ColorOutput "`n========================================" "Cyan"
    Write-ColorOutput "      在线商城 - 服务管理工具" "Cyan"
    Write-ColorOutput "========================================" "Cyan"
    Write-ColorOutput "版本: 1.0 | 作者: lingbai`n" "Gray"
}

# 扫描所有服务
function Get-AllServices {
    $services = @()
    $backendDir = Join-Path $PSScriptRoot 'backend'
    
    if (Test-Path $backendDir) {
        Get-ChildItem -Path $backendDir -Directory | ForEach-Object {
            $serviceName = $_.Name
            
            if ($excludeDirs -notcontains $serviceName) {
                $pomPath = Join-Path $_.FullName 'pom.xml'
                if (Test-Path $pomPath) {
                    $port = $servicePortMap[$serviceName]
                    
                    if (-not $port) {
                        $appYml = Join-Path $_.FullName 'src\main\resources\application.yml'
                        if (Test-Path $appYml) {
                            $content = Get-Content $appYml -Raw
                            if ($content -match 'port:\s*(\d+)') {
                                $port = [int]$matches[1]
                            }
                        }
                    }
                    
                    if ($port) {
                        $services += @{
                            Name = $serviceName
                            Port = $port
                            Path = $_.FullName
                            LogFile = "logs\$serviceName.log"
                        }
                    }
                }
            }
        }
    }
    
    return $services | Sort-Object -Property Port
}

# 检查端口是否被占用
function Test-PortInUse {
    param([int]$Port)
    $result = netstat -ano | Select-String ":$Port " -Quiet
    return $result
}

# 检查服务状态
function Get-ServiceStatus {
    param($Service)
    
    $isRunning = Test-PortInUse -Port $Service.Port
    $logSize = "N/A"
    
    if (Test-Path $Service.LogFile) {
        $size = (Get-Item $Service.LogFile).Length
        if ($size -lt 1KB) {
            $logSize = "$([math]::Round($size, 2)) B"
        } elseif ($size -lt 1MB) {
            $logSize = "$([math]::Round($size / 1KB, 2)) KB"
        } else {
            $logSize = "$([math]::Round($size / 1MB, 2)) MB"
        }
    }
    
    return @{
        IsRunning = $isRunning
        LogSize = $logSize
    }
}

# 显示所有服务
function Show-AllServices {
    Show-Header
    Write-ColorOutput "📋 可用服务列表`n" "Green"
    
    $services = Get-AllServices
    $index = 1
    
    foreach ($service in $services) {
        $status = Get-ServiceStatus -Service $service
        $statusText = if ($status.IsRunning) { "[运行中]" } else { "[已停止]" }
        $statusColor = if ($status.IsRunning) { "Green" } else { "Red" }
        
        Write-Host "  $index. " -NoNewline
        Write-Host "$($service.Name) " -NoNewline -ForegroundColor White
        Write-Host $statusText -NoNewline -ForegroundColor $statusColor
        Write-Host " - 端口: $($service.Port) - 日志: $($status.LogSize)" -ForegroundColor Gray
        
        $index++
    }
    
    Write-Host ""
}

# 启动服务
function Start-MicroService {
    param([string]$ServiceName)
    
    $services = Get-AllServices
    $service = $services | Where-Object { $_.Name -eq $ServiceName }
    
    if (-not $service) {
        Write-ColorOutput "错误: 未找到服务 '$ServiceName'" "Red"
        return
    }
    
    $status = Get-ServiceStatus -Service $service
    if ($status.IsRunning) {
        Write-ColorOutput "服务 '$ServiceName' 已在运行中 (端口: $($service.Port))" "Yellow"
        return
    }
    
    Write-ColorOutput "正在启动服务: $ServiceName (端口: $($service.Port))..." "Cyan"
    
    # 确保日志目录存在
    $logDir = Split-Path $service.LogFile -Parent
    if (-not (Test-Path $logDir)) {
        New-Item -ItemType Directory -Path $logDir | Out-Null
    }
    
    # 启动服务
    Push-Location $service.Path
    $process = Start-Process -FilePath "cmd" -ArgumentList "/c", "mvn spring-boot:run > `"$($service.LogFile)`" 2>&1" -WindowStyle Hidden -PassThru
    Pop-Location
    
    # 等待服务启动
    Start-Sleep -Seconds 5
    
    $status = Get-ServiceStatus -Service $service
    if ($status.IsRunning) {
        Write-ColorOutput "✓ 服务启动成功！" "Green"
    } else {
        Write-ColorOutput "⚠ 服务可能未成功启动，请查看日志: $($service.LogFile)" "Yellow"
    }
}

# 停止服务
function Stop-MicroService {
    param([string]$ServiceName)
    
    $services = Get-AllServices
    $service = $services | Where-Object { $_.Name -eq $ServiceName }
    
    if (-not $service) {
        Write-ColorOutput "错误: 未找到服务 '$ServiceName'" "Red"
        return
    }
    
    $status = Get-ServiceStatus -Service $service
    if (-not $status.IsRunning) {
        Write-ColorOutput "服务 '$ServiceName' 未运行" "Yellow"
        return
    }
    
    Write-ColorOutput "正在停止服务: $ServiceName (端口: $($service.Port))..." "Cyan"
    
    # 查找并终止进程
    $connections = netstat -ano | Select-String ":$($service.Port) "
    if ($connections) {
        $connections | ForEach-Object {
            if ($_ -match '\s+(\d+)\s*$') {
                $pid = $matches[1]
                try {
                    Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
                    Write-ColorOutput "✓ 已停止进程 (PID: $pid)" "Green"
                } catch {
                    Write-ColorOutput "⚠ 无法停止进程 (PID: $pid)" "Yellow"
                }
            }
        }
    }
    
    Start-Sleep -Seconds 2
    
    $status = Get-ServiceStatus -Service $service
    if (-not $status.IsRunning) {
        Write-ColorOutput "✓ 服务已停止" "Green"
    } else {
        Write-ColorOutput "⚠ 服务可能仍在运行，请手动检查" "Yellow"
    }
}

# 重启服务
function Restart-MicroService {
    param([string]$ServiceName)
    
    Write-ColorOutput "`n重启服务: $ServiceName" "Cyan"
    Write-ColorOutput "----------------------------------------`n" "Gray"
    
    Stop-MicroService -ServiceName $ServiceName
    Start-Sleep -Seconds 2
    Start-MicroService -ServiceName $ServiceName
}

# 查看服务日志
function Show-ServiceLog {
    param([string]$ServiceName)
    
    $services = Get-AllServices
    $service = $services | Where-Object { $_.Name -eq $ServiceName }
    
    if (-not $service) {
        Write-ColorOutput "错误: 未找到服务 '$ServiceName'" "Red"
        return
    }
    
    if (-not (Test-Path $service.LogFile)) {
        Write-ColorOutput "日志文件不存在: $($service.LogFile)" "Yellow"
        return
    }
    
    Write-ColorOutput "`n查看日志: $ServiceName" "Cyan"
    Write-ColorOutput "日志文件: $($service.LogFile)" "Gray"
    Write-ColorOutput "----------------------------------------`n" "Gray"
    
    Get-Content $service.LogFile -Tail 50
}

# 主菜单
function Show-Menu {
    Show-Header
    
    Write-ColorOutput "请选择操作：`n" "White"
    Write-ColorOutput "  1. 查看所有服务状态" "White"
    Write-ColorOutput "  2. 启动服务" "White"
    Write-ColorOutput "  3. 停止服务" "White"
    Write-ColorOutput "  4. 重启服务" "White"
    Write-ColorOutput "  5. 查看服务日志" "White"
    Write-ColorOutput "  6. 启动所有服务" "White"
    Write-ColorOutput "  7. 停止所有服务" "White"
    Write-ColorOutput "  0. 退出`n" "White"
    
    $choice = Read-Host "请输入选项"
    
    switch ($choice) {
        "1" {
            Show-AllServices
            Read-Host "`n按Enter键继续"
            Show-Menu
        }
        "2" {
            Show-AllServices
            $serviceName = Read-Host "`n请输入要启动的服务名称"
            Start-MicroService -ServiceName $serviceName
            Read-Host "`n按Enter键继续"
            Show-Menu
        }
        "3" {
            Show-AllServices
            $serviceName = Read-Host "`n请输入要停止的服务名称"
            Stop-MicroService -ServiceName $serviceName
            Read-Host "`n按Enter键继续"
            Show-Menu
        }
        "4" {
            Show-AllServices
            $serviceName = Read-Host "`n请输入要重启的服务名称"
            Restart-MicroService -ServiceName $serviceName
            Read-Host "`n按Enter键继续"
            Show-Menu
        }
        "5" {
            Show-AllServices
            $serviceName = Read-Host "`n请输入要查看日志的服务名称"
            Show-ServiceLog -ServiceName $serviceName
            Read-Host "`n按Enter键继续"
            Show-Menu
        }
        "6" {
            Write-ColorOutput "`n正在启动所有服务..." "Cyan"
            & "$PSScriptRoot\start-dev-silent.bat"
            Read-Host "`n按Enter键继续"
            Show-Menu
        }
        "7" {
            Write-ColorOutput "`n正在停止所有服务..." "Cyan"
            & "$PSScriptRoot\stop-dev-silent.bat"
            Read-Host "`n按Enter键继续"
            Show-Menu
        }
        "0" {
            Write-ColorOutput "`n再见！`n" "Green"
            exit
        }
        default {
            Write-ColorOutput "`n无效的选项，请重新选择" "Red"
            Start-Sleep -Seconds 2
            Show-Menu
        }
    }
}

# 主程序入口
switch ($Action.ToLower()) {
    "list" {
        Show-AllServices
    }
    "start" {
        if ($ServiceName) {
            Start-MicroService -ServiceName $ServiceName
        } else {
            Write-ColorOutput "错误: 请指定服务名称" "Red"
            Write-ColorOutput "用法: .\service-manager.ps1 start <服务名>" "Yellow"
        }
    }
    "stop" {
        if ($ServiceName) {
            Stop-MicroService -ServiceName $ServiceName
        } else {
            Write-ColorOutput "错误: 请指定服务名称" "Red"
            Write-ColorOutput "用法: .\service-manager.ps1 stop <服务名>" "Yellow"
        }
    }
    "restart" {
        if ($ServiceName) {
            Restart-MicroService -ServiceName $ServiceName
        } else {
            Write-ColorOutput "错误: 请指定服务名称" "Red"
            Write-ColorOutput "用法: .\service-manager.ps1 restart <服务名>" "Yellow"
        }
    }
    "log" {
        if ($ServiceName) {
            Show-ServiceLog -ServiceName $ServiceName
        } else {
            Write-ColorOutput "错误: 请指定服务名称" "Red"
            Write-ColorOutput "用法: .\service-manager.ps1 log <服务名>" "Yellow"
        }
    }
    default {
        Show-Menu
    }
}
