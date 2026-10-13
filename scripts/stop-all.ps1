# ============================================================
# MNT-SmartTeaching 一键停止脚本
# 停止：Nacos + 9个微服务 + 前端 Vite + 生产模式 Nginx
# 保留：MySQL / Redis（系统常驻服务，不随项目关闭）
# ============================================================
. "$PSScriptRoot\lib\common.ps1"
$ErrorActionPreference = 'Continue'
$killed = 0
Get-CimInstance Win32_Process -ErrorAction SilentlyContinue | Where-Object {
    $_.Name -like 'java*' -or $_.Name -eq 'node.exe'
} | Where-Object {
    $_.CommandLine -match 'smart-teaching-cloud' -or
    $_.CommandLine -match 'nacos-server-3.2.4' -or
    $_.CommandLine -match 'vite'
} | ForEach-Object {
    Write-Host ("停止 PID {0} : {1}" -f $_.ProcessId, $_.Name)
    Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
    $killed++
}
Stop-Nginx
if ($killed -eq 0) { Write-Host '未发现运行中的项目进程（或已全部停止）' }
Write-Host '完成。MySQL / Redis 服务保留运行。'
