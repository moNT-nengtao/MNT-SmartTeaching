@echo off
rem ============================================================
rem  MNT-SmartTeaching 一键停止入口（调用 stop-all.ps1）
rem  停止 Nacos + 9个微服务 + 前端 Vite，保留 MySQL/Redis 服务
rem ============================================================
chcp 936 >nul
echo 正在停止 MNT-SmartTeaching 相关进程...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop-all.ps1"
echo.
pause
