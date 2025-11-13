# 系统诊断脚本 - 自动检测并报告所有潜在问题
# 作者: lingbai
# 版本: 1.0
# 创建日期: 2025-11-11

# 设置UTF-8编码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   在线商城 - 系统诊断工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$script:issueCount = 0
$script:warningCount = 0

function Test-Command {
    param([string]$Command)
    try {
        Get-Command $Command -ErrorAction Stop | Out-Null
        return $true
    } catch {
        return $false
    }
}

function Write-Issue {
    param([string]$Message)
    Write-Host "[✗] $Message" -ForegroundColor Red
    $script:issueCount++
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[!] $Message" -ForegroundColor Yellow
    $script:warningCount++
}

function Write-Success {
    param([string]$Message)
    Write-Host "[√] $Message" -ForegroundColor Green
}

function Write-Info {
    param([string]$Message)
    Write-Host "    $Message" -ForegroundColor Gray
}

# ============================================
# 1. 检查 Docker
# ============================================
Write-Host "1. 检查 Docker 环境" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

if (Test-Command docker) {
    $dockerVersion = docker --version
    Write-Success "Docker 已安装: $dockerVersion"
    
    # 检查 Docker Desktop 是否运行
    try {
        $null = docker ps 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Docker Desktop 正在运行"
            
            # 检查运行中的容器
            $containers = docker ps --filter "name=mall-" --format "{{.Names}}"
            if ($containers) {
                Write-Success "发现商城容器:"
                foreach ($container in $containers) {
                    Write-Info "- $container"
                }
            } else {
                Write-Warning "未发现商城相关容器 (可能尚未启动)"
            }
        } else {
            Write-Issue "Docker Desktop 未运行"
            Write-Info "解决: 启动 Docker Desktop 并等待完全启动"
        }
    } catch {
        Write-Issue "Docker Desktop 未运行或无法连接"
        Write-Info "解决: 启动 Docker Desktop 应用程序"
    }
} else {
    Write-Issue "Docker 未安装"
    Write-Info "解决: 下载并安装 Docker Desktop"
    Write-Info "下载地址: https://www.docker.com/products/docker-desktop"
}

Write-Host ""

# ============================================
# 2. 检查 Maven
# ============================================
Write-Host "2. 检查 Maven 环境" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

if (Test-Command mvn) {
    $mavenVersion = mvn --version 2>&1 | Select-String "Apache Maven" | Select-Object -First 1
    Write-Success "Maven 已安装: $mavenVersion"
    
    # 检查 Maven 配置
    $mavenHome = $env:MAVEN_HOME
    if ($mavenHome) {
        Write-Info "MAVEN_HOME: $mavenHome"
    }
    
    # 检查 settings.xml
    $settingsFile = Join-Path $env:USERPROFILE ".m2\settings.xml"
    if (Test-Path $settingsFile) {
        Write-Success "Maven settings.xml 存在"
        
        # 检查是否配置了镜像
        $content = Get-Content $settingsFile -Raw
        if ($content -match '<mirror>') {
            Write-Success "已配置 Maven 镜像"
        } else {
            Write-Warning "未配置 Maven 镜像，依赖下载可能较慢"
            Write-Info "建议: 配置阿里云镜像加速下载"
        }
    } else {
        Write-Warning "Maven settings.xml 不存在"
        Write-Info "建议: 创建 settings.xml 并配置国内镜像"
    }
} else {
    Write-Issue "Maven 未安装或未配置到 PATH"
    Write-Info "解决: 下载 Maven 并配置环境变量"
    Write-Info "下载地址: https://maven.apache.org/download.cgi"
}

Write-Host ""

# ============================================
# 3. 检查 Java
# ============================================
Write-Host "3. 检查 Java 环境" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

if (Test-Command java) {
    $javaVersion = java -version 2>&1 | Select-String "version" | Select-Object -First 1
    Write-Success "Java 已安装: $javaVersion"
    
    # 提取版本号
    if ($javaVersion -match '"(\d+)\.') {
        $majorVersion = [int]$matches[1]
    } elseif ($javaVersion -match '"(\d+)"') {
        $majorVersion = [int]$matches[1]
    }
    
    if ($majorVersion -ge 17) {
        Write-Success "Java 版本满足要求 (需要 17+)"
    } else {
        Write-Issue "Java 版本过低 (当前: $majorVersion, 需要: 17+)"
        Write-Info "解决: 升级到 Java 17 或更高版本"
    }
    
    # 检查 JAVA_HOME
    $javaHome = $env:JAVA_HOME
    if ($javaHome) {
        Write-Info "JAVA_HOME: $javaHome"
    } else {
        Write-Warning "未设置 JAVA_HOME 环境变量"
    }
} else {
    Write-Issue "Java 未安装或未配置到 PATH"
    Write-Info "解决: 下载并安装 JDK 17+"
}

Write-Host ""

# ============================================
# 4. 检查 Node.js
# ============================================
Write-Host "4. 检查 Node.js 环境" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

if (Test-Command node) {
    $nodeVersion = node --version
    Write-Success "Node.js 已安装: $nodeVersion"
    
    # 检查 npm
    if (Test-Command npm) {
        $npmVersion = npm --version
        Write-Success "npm 已安装: $npmVersion"
    }
} else {
    Write-Warning "Node.js 未安装 (前端开发需要)"
    Write-Info "建议: 安装 Node.js 18+ 用于前端开发"
    Write-Info "下载地址: https://nodejs.org/"
}

Write-Host ""

# ============================================
# 5. 检查端口占用
# ============================================
Write-Host "5. 检查端口占用情况" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

$ports = @{
    3307 = "MySQL"
    6379 = "Redis"
    8848 = "Nacos"
    8080 = "Gateway"
    8081 = "Auth Service"
    8082 = "User Service"
    8083 = "Product Service"
    8084 = "Order Service"
    8085 = "Payment Service"
    8086 = "Admin Service"
    8087 = "Merchant Service"
    8088 = "Cart Service"
    8089 = "SMS Service"
    5173 = "Frontend"
}

$occupiedPorts = @()

foreach ($port in $ports.Keys) {
    $connection = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($connection) {
        $occupiedPorts += $port
        $processId = $connection[0].OwningProcess
        $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
        
        Write-Warning "$($ports[$port]) 端口 $port 被占用"
        if ($process) {
            Write-Info "占用进程: $($process.Name) (PID: $processId)"
            Write-Info "结束命令: taskkill /PID $processId /F"
        }
    }
}

if ($occupiedPorts.Count -eq 0) {
    Write-Success "所有端口可用"
} else {
    Write-Info "建议: 结束占用进程或修改服务端口配置"
}

Write-Host ""

# ============================================
# 6. 检查项目文件
# ============================================
Write-Host "6. 检查项目文件结构" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

$projectRoot = $PSScriptRoot
$backendDir = Join-Path $projectRoot "backend"
$frontendDir = Join-Path $projectRoot "frontend"
$dockerComposeFile = Join-Path $projectRoot "docker-compose-dev.yml"

# 检查 docker-compose 文件
if (Test-Path $dockerComposeFile) {
    Write-Success "docker-compose-dev.yml 存在"
} else {
    Write-Issue "docker-compose-dev.yml 不存在"
    Write-Info "解决: 确认文件是否被删除或移动"
}

# 检查 backend 目录
if (Test-Path $backendDir) {
    Write-Success "backend 目录存在"
    
    # 扫描服务
    $excludeDirs = @('common-bom', 'common-core', 'simple-test')
    $serviceCount = 0
    
    Get-ChildItem -Path $backendDir -Directory | ForEach-Object {
        $serviceName = $_.Name
        if ($excludeDirs -notcontains $serviceName) {
            $pomPath = Join-Path $_.FullName 'pom.xml'
            if (Test-Path $pomPath) {
                $serviceCount++
            }
        }
    }
    
    Write-Success "发现 $serviceCount 个可启动的服务"
} else {
    Write-Issue "backend 目录不存在"
}

# 检查 frontend 目录
if (Test-Path $frontendDir) {
    Write-Success "frontend 目录存在"
    
    $packageJson = Join-Path $frontendDir "package.json"
    if (Test-Path $packageJson) {
        Write-Success "package.json 存在"
        
        $nodeModules = Join-Path $frontendDir "node_modules"
        if (Test-Path $nodeModules) {
            Write-Success "node_modules 已安装"
        } else {
            Write-Warning "node_modules 未安装"
            Write-Info "建议: 在 frontend 目录运行 npm install"
        }
    }
} else {
    Write-Warning "frontend 目录不存在"
}

# 检查 logs 目录
$logsDir = Join-Path $projectRoot "logs"
if (Test-Path $logsDir) {
    Write-Success "logs 目录存在"
    
    $logFiles = Get-ChildItem -Path $logsDir -Filter "*.log"
    if ($logFiles.Count -gt 0) {
        Write-Info "发现 $($logFiles.Count) 个日志文件"
    }
} else {
    Write-Warning "logs 目录不存在 (首次启动会自动创建)"
}

Write-Host ""

# ============================================
# 7. 检查项目路径
# ============================================
Write-Host "7. 检查项目路径" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

$currentPath = $PSScriptRoot
Write-Info "当前路径: $currentPath"

# 检查路径中是否包含特殊字符
if ($currentPath -match '[\u4e00-\u9fa5]') {
    Write-Warning "路径包含中文字符"
    Write-Info "建议: 移动项目到纯英文路径"
}

if ($currentPath -match '\s') {
    Write-Warning "路径包含空格"
    Write-Info "建议: 避免路径中包含空格"
}

if ($currentPath -match '[^\x00-\x7F]') {
    Write-Warning "路径包含非ASCII字符"
    Write-Info "建议: 使用纯英文路径，如 D:\workspace\springcloud-mall"
} else {
    Write-Success "路径格式正常"
}

Write-Host ""

# ============================================
# 8. 生成诊断报告
# ============================================
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   诊断完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "诊断摘要:" -ForegroundColor Yellow
Write-Host "  发现问题: $script:issueCount 个" -ForegroundColor $(if ($script:issueCount -eq 0) { "Green" } else { "Red" })
Write-Host "  警告信息: $script:warningCount 个" -ForegroundColor $(if ($script:warningCount -eq 0) { "Green" } else { "Yellow" })
Write-Host ""

if ($script:issueCount -eq 0 -and $script:warningCount -eq 0) {
    Write-Host "✓ 系统环境正常，可以启动服务！" -ForegroundColor Green
    Write-Host ""
    Write-Host "建议执行:" -ForegroundColor Cyan
    Write-Host "  start-dev-silent.bat     # 快速启动所有服务" -ForegroundColor White
    Write-Host "  或" -ForegroundColor Gray
    Write-Host "  start-dev-debug.bat      # 调试模式启动" -ForegroundColor White
} elseif ($script:issueCount -eq 0) {
    Write-Host "! 系统基本正常，但有一些建议项" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "可以尝试启动:" -ForegroundColor Cyan
    Write-Host "  start-dev-debug.bat      # 建议使用调试模式" -ForegroundColor White
} else {
    Write-Host "✗ 发现 $script:issueCount 个问题需要解决" -ForegroundColor Red
    Write-Host ""
    Write-Host "请先解决上述问题，然后再启动服务" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "相关文档:" -ForegroundColor Cyan
    Write-Host "  TROUBLESHOOTING.md       # 详细故障排查指南" -ForegroundColor White
    Write-Host "  README.md                # 快速开始和 FAQ" -ForegroundColor White
}

Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
