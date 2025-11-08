@echo off
chcp 65001
echo ========================================
echo    基于SpringCloud的在线商城系统
echo ========================================
echo.

echo [0] 构建后端可执行包...
echo 正在执行 Maven 构建（跳过测试）以生成各服务 JAR...
cd /d "%~dp0backend"
mvn -q -DskipTests clean package

echo.
echo [1] 通过 Docker 启动全部服务...
echo 正在启动基础设施 + 后端微服务 + 前端...
cd /d "%~dp0"
docker-compose up -d

echo.
echo [2] 等待服务启动完成...
timeout /t 30 /nobreak

echo.
echo [3] 服务状态检查...
docker-compose ps

echo.
echo ========================================
echo 服务启动完成！
echo ========================================
echo 前端地址: http://localhost:5173
echo 网关地址: http://localhost:8080
echo Nacos控制台: http://localhost:8848/nacos
echo ========================================
echo.
pause