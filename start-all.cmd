@echo off
rem ============================================================
rem  MNT-SmartTeaching 一键启动入口（调用 start-all.ps1）
rem  全部服务静默后台启动，日志写入各 .log 文件，无任务栏黑框
rem ============================================================
chcp 936 >nul
echo 正在启动 MNT-SmartTeaching 全栈环境...
echo 请保持本窗口打开，启动完成后会显示各组件状态。
echo.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-all.ps1"
echo.
pause
