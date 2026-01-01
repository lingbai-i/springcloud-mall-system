@echo off
setlocal enabledelayedexpansion

echo ======================================================================
echo SpringCloud Mall - Production Environment Startup (Docker Full Stack)
echo ======================================================================
echo.

REM Set paths
set "PROJECT_DIR=%~dp0"
set "BACKEND_DIR=%PROJECT_DIR%backend"

REM Check for Maven
call mvn -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Maven is not installed or not in PATH.
    echo Please install Maven to build the project.
    pause
    exit /b 1
)

REM Check for Docker
call docker -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not installed or not in PATH.
    echo Please install Docker Desktop.
    pause
    exit /b 1
)

echo [INFO] Stopping any existing containers...
call docker-compose -f docker-compose.yml down 

echo.
echo [INFO] Building backend services (Skipping tests)...
echo This may take a few minutes...
call mvn -f "%BACKEND_DIR%/pom.xml" clean package -DskipTests
if %errorlevel% neq 0 (
    echo [ERROR] Maven build failed!
    pause
    exit /b %errorlevel%
)

echo.
echo [INFO] Starting production stack with Docker Compose...
call docker-compose -f docker-compose.yml up -d

echo.
echo ======================================================================
echo Startup Initiated!
echo ======================================================================
echo.
echo Please wait for containers to initialize (approx. 2-5 mins).
echo.
echo Access URLs:
echo   Frontend:  http://localhost:5173
echo   Gateway:   http://localhost:8080
echo   Nacos:     http://localhost:8848/nacos
echo   Grafana:   http://localhost:3001
echo   Kibana:    http://localhost:5601
echo   MinIO:     http://localhost:9001
echo.
echo Check status with: docker-compose ps
echo.
pause
