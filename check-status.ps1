# 检查所有服务状态
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "SpringCloud Mall - 服务状态检查" -ForegroundColor Cyan  
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查Docker容器
Write-Host "[1] Docker 基础设施状态" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$containers = docker ps --filter "name=mall-" --format "{{.Names}}\t{{.Status}}" 2>$null
if ($containers) {
    $containers | ForEach-Object {
        $parts = $_ -split '\t'
        Write-Host "  ✓ $($parts[0])" -ForegroundColor Green -NoNewline
        Write-Host " - $($parts[1])" -ForegroundColor Gray
    }
} else {
    Write-Host "  ✗ 无运行中的Docker容器" -ForegroundColor Red
}

Write-Host ""
Write-Host "[2] 微服务端口监听状态" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

$services = @(
    @{Name="Gateway"; Port=8080},
    @{Name="Auth"; Port=8081},
    @{Name="User"; Port=8082},
    @{Name="Product"; Port=8083},
    @{Name="Order"; Port=8084},
    @{Name="Payment"; Port=8085},
    @{Name="Admin"; Port=8086},
    @{Name="Merchant"; Port=8087},
    @{Name="Cart"; Port=8088},
    @{Name="SMS"; Port=8089}
)

$runningCount = 0
foreach ($svc in $services) {
    $conn = Get-NetTCPConnection -LocalPort $svc.Port -State Listen -ErrorAction SilentlyContinue
    if ($conn) {
        $proc = Get-Process -Id $conn.OwningProcess -ErrorAction SilentlyContinue
        Write-Host "  ✓ $($svc.Name) Service" -ForegroundColor Green -NoNewline
        Write-Host " (端口: $($svc.Port), PID: $($proc.Id))" -ForegroundColor Gray
        $runningCount++
    } else {
        Write-Host "  ✗ $($svc.Name) Service" -ForegroundColor Red -NoNewline
        Write-Host " (端口: $($svc.Port) 未监听)" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "[3] 前端服务状态" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$frontendConn = Get-NetTCPConnection -LocalPort 5173 -State Listen -ErrorAction SilentlyContinue
if ($frontendConn) {
    $proc = Get-Process -Id $frontendConn.OwningProcess -ErrorAction SilentlyContinue
    Write-Host "  ✓ Frontend Dev Server (端口: 5173, PID: $($proc.Id))" -ForegroundColor Green
} else {
    Write-Host "  ✗ Frontend Dev Server (端口: 5173 未监听)" -ForegroundColor Red
}

Write-Host ""
Write-Host "[4] Java进程统计" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$javaProcs = Get-Process -Name java -ErrorAction SilentlyContinue
if ($javaProcs) {
    Write-Host "  Java进程数量: $($javaProcs.Count)" -ForegroundColor Cyan
    $javaProcs | Select-Object -First 10 | ForEach-Object {
        Write-Host "    PID: $($_.Id) | CPU: $([math]::Round($_.CPU, 2))s | 内存: $([math]::Round($_.WorkingSet64/1MB, 0))MB" -ForegroundColor Gray
    }
} else {
    Write-Host "  ✗ 无Java进程运行" -ForegroundColor Red
}

Write-Host ""
Write-Host "[5] PowerShell Job状态" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray
$jobs = Get-Job -ErrorAction SilentlyContinue
if ($jobs) {
    $jobs | Format-Table Name, State, HasMoreData -AutoSize | Out-String | ForEach-Object { Write-Host $_ -ForegroundColor Gray }
} else {
    Write-Host "  无活动的PowerShell Job" -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "服务统计" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  运行中的微服务: $runningCount / 10" -ForegroundColor $(if($runningCount -ge 8){"Green"}elseif($runningCount -ge 5){"Yellow"}else{"Red"})
Write-Host "  基础设施服务: $($containers.Count) / 4" -ForegroundColor $(if($containers.Count -eq 4){"Green"}else{"Yellow"})
Write-Host ""

if ($runningCount -ge 8) {
    Write-Host "✓ 系统运行正常！" -ForegroundColor Green
    Write-Host ""
    Write-Host "访问地址:" -ForegroundColor White
    Write-Host "  前端:  http://localhost:5173" -ForegroundColor Cyan
    Write-Host "  网关:  http://localhost:8080" -ForegroundColor Cyan
    Write-Host "  Nacos: http://localhost:8848/nacos" -ForegroundColor Cyan
} elseif ($runningCount -ge 3) {
    Write-Host "! 部分服务正在启动中..." -ForegroundColor Yellow
    Write-Host "  请等待1-2分钟后再次检查" -ForegroundColor Gray
} else {
    Write-Host "✗ 多数服务未启动" -ForegroundColor Red
    Write-Host "  建议查看日志: logs\*.log" -ForegroundColor Gray
    Write-Host "  或重新运行: start-all-services.ps1" -ForegroundColor Gray
}
Write-Host ""
