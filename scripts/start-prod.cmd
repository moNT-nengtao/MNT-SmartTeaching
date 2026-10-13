@echo off
rem ============================================================
rem  MNT-SmartTeaching 生产模式一键启动（Nginx 部署）
rem ============================================================
chcp 936 >nul
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-prod.ps1" %*
echo.
pause
