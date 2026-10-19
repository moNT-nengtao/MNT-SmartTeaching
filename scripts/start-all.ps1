# ============================================================
# MNT-SmartTeaching 一键启动脚本（全部静默后台，无控制台窗口）
# 启动前自动检查环境：JDK / Node / MySQL / Redis / Nacos / 后端构建产物 / 前端依赖
# 缺少任意组件时直接提示，不启动
# 依次启动：MySQL(服务) -> Redis(服务) -> Nacos -> 9个微服务 -> 前端构建 -> Nginx(8888)
# 页面统一入口：http://localhost:8888 （Nginx 反向代理，/api 转发网关 8080）
# 可选参数 -Dev：额外启动 Vite 开发服务器(5173)，便于前端热更新调试
# ============================================================
param([switch]$Dev)
. "$PSScriptRoot\tools\lib\common.ps1"
$ErrorActionPreference = 'Continue'

# ---------- 0. 启动条件自检 ----------
Write-Host '[0/7] 环境自检（缺少组件将中止启动）...'
$missing = @()

if (-not (Test-Path $JAVA_BIN)) {
    $missing += "JDK：未找到 $JAVA_BIN ，请安装 JDK 17+，或修改 tools\lib\common.ps1 顶部 JAVA_BIN 变量指向你的 javaw.exe"
}
if (-not (Test-Path $NODE_EXE)) {
    $missing += "Node.js：未找到 $NODE_EXE ，请安装 Node.js 18+，或修改 tools\lib\common.ps1 顶部 NODE_EXE 变量指向你的 node.exe"
}
if (-not (Get-Service -Name 'MySQL80' -ErrorAction SilentlyContinue)) {
    $missing += 'MySQL：系统服务 MySQL80 不存在，请安装 MySQL 8.x 并注册为 Windows 服务（服务名 MySQL80）'
}
if (-not (Get-Service -Name 'Redis' -ErrorAction SilentlyContinue)) {
    $missing += 'Redis：系统服务 Redis 不存在，请安装 Redis 并注册为 Windows 服务（服务名 Redis）'
}
if (-not (Test-Path "$NACOS\target\nacos-server.jar")) {
    $missing += "Nacos：未找到 $NACOS\target\nacos-server.jar ，请将 nacos-server-3.2.4 解压到仓库根目录"
}
if (-not (Test-Path "$WEB_DIR\node_modules")) {
    $missing += '前端依赖：缺少 smart-teaching-web/node_modules ，请在 smart-teaching-web 目录执行 npm install'
}
foreach ($svc in $SERVICES) {
    $jar = "$BASE\$svc\target\$svc-0.0.1-SNAPSHOT.jar"
    if (-not (Test-Path $jar)) {
        $missing += "$svc 构建产物：缺少 $jar ，请在仓库根目录执行 mvn clean package"
    }
}

if ($missing.Count -gt 0) {
    Write-Host ''
    Write-Host ('发现 {0} 项环境缺失，启动已中止：' -f $missing.Count) -ForegroundColor Red
    $i = 0
    foreach ($m in $missing) {
        $i++
        Write-Host ("  [{0}] {1}" -f $i, $m) -ForegroundColor Yellow
    }
    Write-Host ''
    Write-Host '补齐上述组件后重新运行本脚本。' -ForegroundColor Red
    exit 1
}
Write-Host '      环境自检通过，开始启动...'

# ---------- 1. MySQL ----------
Write-Host '[1/7] MySQL (3306)...'
if (Test-Port 3306) { Write-Host '      已在运行，跳过' }
else {
    Write-Host '      未运行，尝试启动服务 MySQL80...'
    try { Start-Service -Name 'MySQL80' -ErrorAction Stop; Write-Host '      MySQL80 已启动' }
    catch { Write-Host '      [!] MySQL80 启动失败，请手动启动 MySQL 后重试' }
    Start-Sleep -Seconds 3
}

# ---------- 2. Redis ----------
Write-Host '[2/7] Redis (6379)...'
if (Test-Port 6379) { Write-Host '      已在运行，跳过' }
else {
    Write-Host '      未运行，尝试启动服务 Redis...'
    try { Start-Service -Name 'Redis' -ErrorAction Stop; Write-Host '      Redis 已启动' }
    catch { Write-Host '      [!] Redis 启动失败，请手动启动 Redis 后重试' }
    Start-Sleep -Seconds 3
}

# ---------- 3. Nacos ----------
Write-Host '[3/7] Nacos 注册中心 (9848)...'
if (Test-Port 9848) { Write-Host '      已在运行，跳过' }
else {
    Write-Host '      未运行，静默启动（日志 nacos-start.log）...'
    Start-Hidden "$JAVA_BIN" @(
        '-Xms512m','-Xmx512m','-Xmn256m',
        '--add-opens=java.base/java.lang=ALL-UNNAMED',
        '--add-opens=java.base/java.lang.reflect=ALL-UNNAMED',
        '--add-opens=java.base/java.util=ALL-UNNAMED',
        '-Dnacos.standalone=true','-Dnacos.deployment.type=server',
        "-Dloader.path=$NACOS\plugins", "-Dnacos.home=$NACOS",
        '-Dnacos.core.auth.enabled=false',
        '-jar', "$NACOS\target\nacos-server.jar",
        "--spring.config.additional-location=file:$NACOS/conf/",
        "--logging.config=$NACOS/conf/nacos-logback.xml",
        'nacos.nacos','-m','standalone'
    ) "$NACOS_DIR\nacos-start.log"

    Write-Host '      等待 Nacos 就绪（最长 120s）...'
    for ($i = 0; $i -lt 60; $i++) {
        if (Test-Port 9848) { Write-Host '      Nacos 已就绪'; break }
        if ($i -eq 59) { Write-Host '      [!] Nacos 未在 120s 内就绪，请检查 nacos-start.log' }
        Start-Sleep -Seconds 2
    }
}

# ---------- 4. 9 个微服务 ----------
Write-Host '[4/7] 微服务（9 个，javaw 静默启动）...'
foreach ($svc in $SERVICES) {
    $port = $PORTS[$svc]
    if (Test-Port $port) { Write-Host "      $svc ($port) 已在运行，跳过"; continue }
    $jar  = "$BASE\$svc\target\$svc-0.0.1-SNAPSHOT.jar"
    $log  = "$BASE\$svc\target\$svc.log"
    Start-Hidden "$JAVA_BIN" @('-Xms128m','-Xmx256m','-jar', $jar) $log
    Write-Host "      已启动 $svc ($port)"
    Start-Sleep -Seconds 1
}

Write-Host '      等待微服务注册（最长 90s）...'
for ($i = 0; $i -lt 45; $i++) {
    $ready = $true
    foreach ($svc in $SERVICES) { if (-not (Test-Port $PORTS[$svc])) { $ready = $false; break } }
    if ($ready) { Write-Host '      全部微服务端口已就绪'; break }
    if ($i -eq 44) { Write-Host '      [!] 部分服务未就绪，请查看各服务 target\*.log' }
    Start-Sleep -Seconds 2
}

# ---------- 5. 前端构建（dist 缺失时自动构建） ----------
Write-Host '[5/7] 前端构建（生产包 dist）...'
if (Test-Path (Join-Path $DistDir 'index.html')) {
    Write-Host '      dist 已存在，跳过构建（如需强制重建：scripts\tools\build-web.cmd）'
} else {
    Write-Host '      dist 缺失，执行 npm run build ...'
    Push-Location $WEB_DIR
    try { & npm run build; if ($LASTEXITCODE -ne 0) { Write-Host '      [!] 前端构建失败，请查看上方 npm 报错'; exit 1 } }
    finally { Pop-Location }
    if (-not (Test-Path (Join-Path $DistDir 'index.html'))) {
        Write-Host '      [!] 构建产物缺失：dist/index.html 不存在'; exit 1
    }
    Write-Host '      前端构建完成'
}

# ---------- 6. Nginx 反向代理（8888） ----------
Write-Host '[6/7] Nginx 反向代理 (8888)...'
Ensure-AsciiLinks
if (Start-Nginx) { Write-Host '      8888 前端页面可访问' } else { Write-Host '      [!] 8888 页面校验未通过，请查看 nginx 日志' }

# ---------- 6.5 可选：Vite 开发服务器（-Dev） ----------
if ($Dev) {
    Write-Host '[6.5] Vite 开发服务器 (5173) -Dev 模式...'
    if (Test-Port 5173) { Write-Host '      已在运行，跳过' }
    else {
        Start-Hidden $NODE_EXE @("$WEB_DIR\node_modules\vite\bin\vite.js") "$WEB_DIR\vite-dev.log" $WEB_DIR
        Write-Host '      已启动（日志 vite-dev.log），热更新调试用'
        Start-Sleep -Seconds 8
    }
}

# ---------- 7. 汇总 ----------
Write-Host '[7/7] 状态汇总：'
$summary = @('Nacos:9848','Gateway:8080','uaa:8081','course:8082','selection-score:8083','attendance:8084','warning:8085','ai:8086','analysis:8087','teaching:8088','MySQL:3306','Redis:6379','Nginx页面:8888')
foreach ($s in $summary) {
    $pp = [int]($s -replace '.*:','')
    $status = if (Test-Port $pp) { 'OK' } else { 'DOWN' }
    Write-Host ("      {0,-18} {1}" -f $s, $status)
}
if ($Dev) { $s5173 = if (Test-Port 5173) { 'OK' } else { 'DOWN' }; Write-Host ("      {0,-18} {1}" -f 'Vite调试:5173', $s5173) }
Write-Host ''
Write-Host '访问入口（统一走 Nginx 反向代理）：'
Write-Host '  前端页面  http://localhost:8888'
Write-Host '  网关接口  http://localhost:8080/api/xxx'
Write-Host '  Nacos控制台 http://localhost:8089/nacos  (nacos/nacos)'
if ($Dev) { Write-Host '  Vite调试  http://localhost:5173  （仅 -Dev 模式）' }
Write-Host ''
Write-Host '一键停止所有服务（含 Nginx）：双击本目录下的 stop-all.cmd'
