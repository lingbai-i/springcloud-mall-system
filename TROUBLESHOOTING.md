# 启动问题排查指南

## 🚨 常见问题：启动脚本闪退

### 问题现象

双击 `start-dev-silent.bat` 后，窗口一闪而过，服务未启动。

### 可能原因及解决方案

#### 1. ⚠️ Docker Desktop 未运行（最常见）

**检查方法**:

```bash
docker ps
```

**解决方案**:

- 启动 Docker Desktop
- 等待右下角托盘图标显示 "Docker Desktop is running"
- 重新运行启动脚本

---

#### 2. ⚠️ 端口被占用

**常用端口**:

- `3307` - MySQL
- `6379` - Redis
- `8848` - Nacos
- `8080-8089` - 微服务
- `5173` - 前端

**检查方法**:

```powershell
# 检查特定端口
netstat -ano | findstr :8080

# 检查所有商城相关端口
netstat -ano | findstr "3307 6379 8848 8080 8081 8082 8083 8084 8085 8086 8087 8088 8089 5173"
```

**解决方案**:

```powershell
# 找到占用端口的进程ID (PID)
netstat -ano | findstr :8080

# 结束进程 (将 PID 替换为实际值)
taskkill /PID <PID> /F
```

---

#### 3. ⚠️ Maven 未安装或未配置

**检查方法**:

```bash
mvn --version
```

**解决方案**:

1. 下载 Maven: https://maven.apache.org/download.cgi
2. 配置环境变量 `MAVEN_HOME`
3. 添加 `%MAVEN_HOME%\bin` 到 PATH
4. 重新打开命令行窗口验证

---

#### 4. ⚠️ Java 版本不兼容

**项目要求**: Java 17 或更高版本

**检查方法**:

```bash
java -version
```

**解决方案**:

- 升级到 Java 17+
- 配置 `JAVA_HOME` 环境变量

---

#### 5. ⚠️ 文件路径包含特殊字符

**问题**: 路径中包含中文、空格或特殊字符可能导致脚本执行失败

**解决方案**:

- 将项目移动到纯英文路径
- 例如: `D:\workspace\springcloud-mall`

---

## 🔧 调试方法

### 方法一：使用调试启动脚本（推荐）

```bash
# 运行调试版本，逐步显示详细信息
start-dev-debug.bat
```

**特点**:

- ✅ 逐步显示执行过程
- ✅ 详细的错误信息
- ✅ 暂停等待用户确认
- ✅ 显示所有执行命令

---

### 方法二：手动逐步启动

**步骤 1**: 启动基础设施

```bash
docker-compose -f docker-compose-dev.yml up -d
```

**步骤 2**: 检查容器状态

```bash
docker ps
```

应该看到 3 个容器:

- `mall-mysql-dev`
- `mall-redis-dev`
- `mall-nacos-dev`

**步骤 3**: 手动启动单个服务测试

```bash
cd backend\gateway-service
mvn spring-boot:run -Dspring-boot.run.profiles=simple
```

---

### 方法三：查看日志

启动后检查日志文件:

```bash
# 查看所有日志
dir logs\

# 查看特定服务日志
type logs\gateway-service.log

# 实时跟踪日志
pwsh -File tail-logs.ps1 gateway-service
```

---

## 📋 完整诊断清单

在报告问题前，请完成以下检查:

- [ ] Docker Desktop 已安装并运行
- [ ] Maven 已安装 (`mvn --version` 有输出)
- [ ] Java 17+ 已安装 (`java -version` 显示 17+)
- [ ] 项目路径不包含中文或特殊字符
- [ ] 所需端口未被占用 (3307, 6379, 8848, 8080-8089, 5173)
- [ ] `docker-compose-dev.yml` 文件存在
- [ ] `backend` 目录存在且包含服务
- [ ] 已尝试运行 `start-dev-debug.bat`
- [ ] 已查看 `logs` 目录中的错误日志

---

## 🆘 获取帮助

### 收集诊断信息

运行以下命令收集系统信息:

```powershell
# 保存诊断信息到文件
echo "=== 系统信息 ===" > diagnosis.txt
systeminfo | findstr /B /C:"OS Name" /C:"OS Version" >> diagnosis.txt
echo. >> diagnosis.txt

echo "=== Docker 版本 ===" >> diagnosis.txt
docker --version >> diagnosis.txt
echo. >> diagnosis.txt

echo "=== Maven 版本 ===" >> diagnosis.txt
mvn --version >> diagnosis.txt
echo. >> diagnosis.txt

echo "=== Java 版本 ===" >> diagnosis.txt
java -version 2>> diagnosis.txt
echo. >> diagnosis.txt

echo "=== 端口占用情况 ===" >> diagnosis.txt
netstat -ano | findstr "3307 6379 8848 8080 8081 8082 8083 8084 8085 8086 8087 8088 8089 5173" >> diagnosis.txt
echo. >> diagnosis.txt

echo "=== Docker 容器 ===" >> diagnosis.txt
docker ps -a >> diagnosis.txt
echo. >> diagnosis.txt

notepad diagnosis.txt
```

### 报告问题时请提供:

1. `diagnosis.txt` 内容
2. 完整的错误信息或截图
3. `logs` 目录下的相关日志文件
4. 执行 `start-dev-debug.bat` 的输出

---

## 📖 相关文档

- [快速启动指南](QUICK_START.md)
- [服务管理指南](QUICK_REFERENCE.md)
- [自动服务检测说明](docs/AUTO_SERVICE_DETECTION.md)

---

## 💡 最佳实践

### 推荐的启动顺序

1. **首次运行**:

   ```bash
   start-dev-debug.bat  # 使用调试模式
   ```

2. **日常开发**:

   ```bash
   start-dev-silent.bat  # 使用快速启动
   ```

3. **遇到问题**:

   ```bash
   # 停止所有服务
   stop-dev-silent.bat

   # 清理 Docker 容器
   docker-compose -f docker-compose-dev.yml down -v

   # 重新启动
   start-dev-debug.bat
   ```

### 性能优化建议

1. **增加 Docker 资源**:

   - Docker Desktop → Settings → Resources
   - 推荐: CPU 4 核+, 内存 8GB+

2. **Maven 加速**:

   - 配置国内镜像源 (阿里云)
   - `~/.m2/settings.xml`

3. **减少启动服务数量**:
   - 编辑 `start-dev-silent.bat`
   - 注释掉不需要的服务配置行

---

**更新时间**: 2026-01-01
**维护人**: lingbai
