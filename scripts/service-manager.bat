@echo off
REM 服务管理工具启动器
REM 作者: lingbai
REM 版本: 1.0

pwsh -ExecutionPolicy Bypass -File "%~dp0service-manager.ps1" %*
