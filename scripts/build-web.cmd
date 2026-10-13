@echo off
rem ============================================================
rem  MNT-SmartTeaching 前端构建（生产打包）
rem ============================================================
chcp 936 >nul
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-web.ps1" %*
echo.
pause
