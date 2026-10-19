@echo off
rem ============================================================
rem  MNT-SmartTeaching 一键启动入口（调用 start-all.ps1）
rem  统一走 Nginx 反向代理：页面 http://localhost:8888
rem  辅助脚本已下沉到 tools\ 子目录，本目录仅保留一键启停
rem  加参数 -Dev 可额外启动 Vite(5173) 用于前端热更新调试
rem ============================================================
chcp 936 >nul
echo 正在启动 MNT-SmartTeaching 全栈服务（后端 + Nginx:8888）...
echo 请保持本窗口打开，启动完成后将显示各端口状态。
echo.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-all.ps1" %*
echo.
echo 按任意键关闭本窗口...
pause >nul
exit
