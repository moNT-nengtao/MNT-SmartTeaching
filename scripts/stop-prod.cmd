@echo off
rem ============================================================
rem  MNT-SmartTeaching 生产模式停止（停止 Nginx）
rem ============================================================
chcp 936 >nul
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop-prod.ps1" %*
echo.
pause
