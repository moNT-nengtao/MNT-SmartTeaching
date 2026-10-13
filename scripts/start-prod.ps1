# ============================================================
#  MNT-SmartTeaching 生产模式一键启动（Nginx 部署）
#  流程：环境自检 -> 构建前端 dist -> 生成 nginx 配置 -> 启动 Nginx -> 校验
#  页面入口：http://localhost:8888 （默认端口，可改下方 $Port）
#  附加参数：-Build 强制重新构建前端
#
#  说明：项目位于中文路径下，Windows 版 Nginx 无法直接处理
#  中文路径（报 1113 编码错误），脚本自动在 C:\Users\Public 建立
#  两个 junction（目录联接）映射到 nginx 与 dist，以纯 ASCII 路径运行。
# ============================================================
param([switch]$Build)
$ErrorActionPreference = 'Stop'

# ---------- 配置 ----------
$Port = 8888                # Nginx 监听端口（80 常被占用，默认 8888）
$GatewayPort = 8080         # 网关端口

# ---------- 路径 ----------
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = Split-Path -Parent $ScriptDir
$WebDir = Join-Path $RepoRoot 'smart-teaching-web'
$DistDir = Join-Path $WebDir 'dist'                     # 真实 dist（用于判断产物）
$NginxDirReal = Join-Path $RepoRoot 'nginx-1.31.3'      # 真实 nginx 目录
$NginxExeReal = Join-Path $NginxDirReal 'nginx.exe'
$PublicDir = 'C:\Users\Public'
$NginxLink = Join-Path $PublicDir 'mnt-nginx'           # ASCII junction -> nginx
$DistLink = Join-Path $PublicDir 'mnt-dist'             # ASCII junction -> dist
$NginxExe = Join-Path $NginxLink 'nginx.exe'
$Template = Join-Path $ScriptDir 'nginx\nginx-prod.conf.template'
$RuntimeConf = Join-Path $NginxLink 'conf\nginx-prod.conf'

# ---------- 输出函数 ----------
function Write-Ok  { $msg = $args -join ' '; Write-Host "  [OK]   $msg" -ForegroundColor Green }
function Write-Err { $msg = $args -join ' '; Write-Host "  [失败] $msg" -ForegroundColor Red; exit 1 }

Write-Host '========================================'
Write-Host ' MNT-SmartTeaching 生产模式启动（Nginx）'
Write-Host '========================================'

# ---------- 0. 环境自检 ----------
Write-Host '-- 0. 环境自检 --'
if (-not (Test-Path $NginxExeReal)) {
    Write-Err "未找到 Nginx：$NginxDirReal。请将 nginx 解压到仓库根目录 nginx-1.31.3/"
}
Write-Ok "Nginx 就绪：$NginxExeReal"

if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
    Write-Err '未检测到 npm，请先安装 Node.js 18+'
}
Write-Ok "npm 就绪：$(npm -v)"

# ---------- 0.5 建立 ASCII junction（绕开中文路径） ----------
Write-Host '-- 0.5 建立 ASCII 运行路径 --'
if (-not (Test-Path $NginxExe)) {
    cmd /c "mklink /J `"$NginxLink`" `"$NginxDirReal`"" | Out-Null
    if (-not (Test-Path $NginxExe)) { Write-Err "创建 junction 失败：$NginxLink （请确认 C:\Users\Public 可写）" }
}
if (-not (Test-Path (Join-Path $DistLink 'index.html'))) {
    cmd /c "mklink /J `"$DistLink`" `"$DistDir`"" | Out-Null
    if (-not (Test-Path (Join-Path $DistLink 'index.html'))) { Write-Err "创建 junction 失败：$DistLink" }
}
Write-Ok "运行路径：$NginxLink / $DistLink"

# ---------- 1. 构建前端 ----------
if ($Build -or -not (Test-Path (Join-Path $DistDir 'index.html'))) {
    Write-Host '-- 1. 构建前端（npm run build）--'
    Push-Location $WebDir
    try { & npm run build; if ($LASTEXITCODE -ne 0) { Write-Err '前端构建失败，请查看上方 npm 报错' } }
    finally { Pop-Location }
} else {
    Write-Host '-- 1. 前端 dist 已存在，跳过构建（强制重建加 -Build 参数）--'
}
if (-not (Test-Path (Join-Path $DistDir 'index.html'))) { Write-Err '前端构建产物缺失：dist/index.html 不存在' }
Write-Ok "前端产物就绪：$DistDir"

# ---------- 2. 检查后端网关 ----------
Write-Host '-- 2. 检查后端服务 --'
if (-not (Get-NetTCPConnection -LocalPort $GatewayPort -State Listen -ErrorAction SilentlyContinue)) {
    Write-Err "网关 $GatewayPort 未启动，请先运行 scripts\start-all.cmd 启动后端"
}
Write-Ok "网关 $GatewayPort 已就绪"

# ---------- 3. 检查端口占用 ----------
Write-Host '-- 3. 检查端口 --'
if (Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue) {
    Write-Err "端口 $Port 已被占用，请修改本脚本顶部 $Port 变量后重试"
}
Write-Ok "端口 $Port 空闲"

# ---------- 4. 生成 nginx 配置 ----------
Write-Host '-- 4. 生成 nginx 配置 --'
$webRoot = $DistLink.Replace('\', '/')     # conf 中使用 ASCII 路径（junction 视图）
$conf = (Get-Content $Template -Raw -Encoding UTF8)
$conf = $conf.Replace('__PORT__', "$Port").Replace('__WEB_ROOT__', $webRoot).Replace('__GATEWAY_PORT__', "$GatewayPort")
Set-Content -Path $RuntimeConf -Value $conf -Encoding ASCII
Write-Ok "配置已生成：$RuntimeConf"

# ---------- 5. 启动 Nginx ----------
Write-Host '-- 5. 启动 Nginx --'
if (Test-Path (Join-Path $NginxLink 'logs\nginx.pid')) {
    Write-Err 'Nginx 已在运行，请先运行 scripts\stop-prod.cmd 停止'
}
Start-Process -FilePath $NginxExe -ArgumentList ('-p ' + $NginxLink + ' -c ' + $RuntimeConf) -WindowStyle Hidden | Out-Null
Start-Sleep -Seconds 2
if (-not (Test-Path (Join-Path $NginxLink 'logs\nginx.pid'))) { Write-Err 'Nginx 启动失败，请查看错误日志' }
Start-Sleep -Seconds 2
Write-Ok 'Nginx 已启动'

# ---------- 6. 校验 ----------
Write-Host '-- 6. 校验 --'
$pageOk = $false
for ($i = 0; $i -lt 10; $i++) {
    try {
        $r = Invoke-WebRequest -Uri "http://127.0.0.1:$Port" -UseBasicParsing -TimeoutSec 3
        if ($r.StatusCode -eq 200) { $pageOk = $true; break }
    } catch { Start-Sleep -Seconds 1 }
}
if ($pageOk) { Write-Ok "前端页面可访问：http://localhost:$Port" } else { Write-Err '前端页面无法访问，请查看 nginx 日志' }

$apiOk = $false
try {
    $body = @{ username = 'admin'; password = '123456' } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "http://127.0.0.1:$Port/api/auth/login" -Method Post -ContentType 'application/json' -Body $body -TimeoutSec 10
    if ($r.code -eq 1) { $apiOk = $true }
} catch {}
if ($apiOk) { Write-Ok 'API 代理正常（登录接口连通）' } else { Write-Warning 'API 代理校验未通过，请检查网关是否运行' }

Write-Host ''
Write-Host '生产模式部署完成：'
Write-Host "  页面入口  http://localhost:$Port"
Write-Host '  停止部署  scripts\stop-prod.cmd'
Write-Host '  停止全部  scripts\stop-all.cmd'
