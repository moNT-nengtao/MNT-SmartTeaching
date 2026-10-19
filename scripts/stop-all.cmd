@echo off
rem ============================================================
rem  MNT-SmartTeaching 一键停止入口（调用 stop-all.ps1）
rem  停止 Nginx + 9个微服务 + Nacos（含 -Dev 的 Vite），保留 MySQL/Redis
rem ============================================================
chcp 936 >nul
echo 正在停止 MNT-SmartTeaching 项目进程...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop-all.ps1"
echo.
pause