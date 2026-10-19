# ============================================================
#  MNT-SmartTeaching 生产模式停止（停止 Nginx）
# ============================================================
. "$PSScriptRoot\lib\common.ps1"
$ErrorActionPreference = 'Stop'

Write-Host 'MNT-SmartTeaching 停止 Nginx ...'
if (-not (Test-Path $NginxExe)) { Write-Host "未找到 Nginx（$NginxDirReal）"; exit 1 }
if (-not (Test-Path (Join-Path $NginxLink 'logs\nginx.pid'))) {
    Write-Host 'Nginx 未在运行，无需停止。'; exit 0
}
& $NginxExe -p "$NginxLink" -c "$NginxRuntimeConf" -s stop
Start-Sleep -Seconds 1
if (Test-Path (Join-Path $NginxLink 'logs\nginx.pid')) {
    Write-Host 'Nginx 停止失败，请手动结束 nginx.exe 进程。'
} else {
    Write-Host 'Nginx 已停止。'
}
