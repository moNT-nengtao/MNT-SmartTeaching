# ============================================================
#  MNT-SmartTeaching 前端构建（生产打包）
#  产物输出：smart-teaching-web/dist
# ============================================================
. "$PSScriptRoot\lib\common.ps1"
$ErrorActionPreference = 'Stop'

Write-Host 'MNT-SmartTeaching 前端构建 ...'
if (-not (Test-Npm)) { Write-Host '未检测到 npm，请先安装 Node.js 18+'; exit 1 }
Push-Location $WEB_DIR
try {
    & npm run build
    if ($LASTEXITCODE -ne 0) { Write-Host '构建失败，请查看上方 npm 报错'; exit 1 }
} finally { Pop-Location }
if (-not (Test-Path (Join-Path $DistDir 'index.html'))) {
    Write-Host '构建产物缺失：dist/index.html 不存在'; exit 1
}
Write-Host "构建完成：$DistDir"
