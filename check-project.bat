@echo off
echo ========================================
echo    项目结构检查工具
echo ========================================
echo.

echo [检查] 后端项目结构...
if exist "backend\pom.xml" (
    echo 后端主pom.xml存在
) else (
    echo 后端主pom.xml缺失
)

if exist "backend\common-core\pom.xml" (
    echo 公共核心模块存在
) else (
    echo 公共核心模块缺失
)

if exist "backend\gateway-service\pom.xml" (
    echo 网关服务存在
) else (
    echo 网关服务缺失
)

if exist "backend\user-service\pom.xml" (
    echo 用户服务存在
) else (
    echo 用户服务缺失
)

if exist "backend\product-service\pom.xml" (
    echo 商品服务存在
) else (
    echo 商品服务缺失
)

echo.
echo [检查] 前端项目结构...
if exist "frontend\package.json" (
    echo 前端package.json存在
) else (
    echo 前端package.json缺失
)

if exist "frontend\src\main.js" (
    echo 前端入口文件存在
) else (
    echo 前端入口文件缺失
)

if exist "frontend\src\App.vue" (
    echo 前端App组件存在
) else (
    echo 前端App组件缺失
)

echo.
echo [检查] 配置文件...
if exist "docker-compose.yml" (
    echo Docker Compose配置存在
) else (
    echo Docker Compose配置缺失
)

if exist "sql\init.sql" (
    echo 数据库初始化脚本存在
) else (
    echo 数据库初始化脚本缺失
)

echo.
echo [检查] 启动脚本...
if exist "start-dev-silent.bat" (
    echo 启动脚本存在
) else (
    echo 启动脚本缺失
)

echo.
echo ========================================
echo 项目结构检查完成！
echo ========================================
echo.
echo 如果所有项目都显示存在，说明项目结构完整
echo 可以使用 start-dev-silent.bat 启动项目
echo.
pause

