@echo off

REM 启动 PowerShell 诊断脚本
echo.
echo ========================================
echo    启动系统诊断工具...
echo ========================================
echo.

REM 检查 PowerShell 是否可用
pwsh --version >nul 2>&1
if %errorlevel% equ 0 (
    REM 使用 PowerShell Core (pwsh)
    pwsh -ExecutionPolicy Bypass -File "%~dp0diagnose.ps1"
) else (
    REM 回退到 Windows PowerShell
    powershell -ExecutionPolicy Bypass -File "%~dp0diagnose.ps1"
)

exit /b %errorlevel%
