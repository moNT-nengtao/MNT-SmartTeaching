# ============================================================
#  MNT-SmartTeaching 公共定义与工具库
#  被 scripts/ 下各脚本 dot-source（. "$PSScriptRoot\lib\common.ps1"）
#  统一：路径基准 / 常量 / 输出函数 / 端口与进程工具
# ============================================================

$ScriptsRoot = Split-Path -Parent $PSScriptRoot        # scripts/
$RepoRoot    = Split-Path -Parent $ScriptsRoot         # 仓库根

# ---------- 路径与常量（按机器实际环境修改此处） ----------
$JAVA_BIN   = 'C:\Program Files\Java\jdk-23\bin\javaw.exe'
$NODE_EXE   = 'C:\Program Files\nodejs\node.exe'
$BASE       = Join-Path $RepoRoot 'smart-teaching-cloud'
$NACOS_DIR  = Join-Path $RepoRoot 'nacos-server-3.2.4'
$NACOS      = Join-Path $NACOS_DIR 'nacos'
$WEB_DIR    = Join-Path $RepoRoot 'smart-teaching-web'
$DistDir    = Join-Path $WEB_DIR 'dist'
$NginxDirReal = Join-Path $RepoRoot 'nginx-1.31.3'
$NginxExeReal = Join-Path $NginxDirReal 'nginx.exe'
$PublicDir  = 'C:\Users\Public'
$NginxLink  = Join-Path $PublicDir 'mnt-nginx'          # ASCII junction -> nginx 目录
$DistLink   = Join-Path $PublicDir 'mnt-dist'           # ASCII junction -> dist
$NginxExe   = Join-Path $NginxLink 'nginx.exe'
$NginxConfTemplate = Join-Path $ScriptsRoot 'nginx\nginx-prod.conf.template'
$NginxRuntimeConf  = Join-Path $NginxLink 'conf\nginx-prod.conf'

$SERVICES = @(
    'gateway-service','uaa-service','course-service','selection-score-service',
    'attendance-service','warning-service','ai-service','analysis-service','teaching-service'
)
$PORTS = @{ 'gateway-service'=8080; 'uaa-service'=8081; 'course-service'=8082;
            'selection-score-service'=8083; 'attendance-service'=8084; 'warning-service'=8085;
            'ai-service'=8086; 'analysis-service'=8087; 'teaching-service'=8088 }

$ProdPort    = 8888        # 生产模式 Nginx 监听端口（80 常被占用）
$GatewayPort = 8080        # 网关端口

# ---------- 输出函数 ----------
function Write-Ok  { $msg = $args -join ' '; Write-Host "  [OK]   $msg" -ForegroundColor Green }
function Write-Err { $msg = $args -join ' '; Write-Host "  [失败] $msg" -ForegroundColor Red; exit 1 }

# ---------- 工具函数 ----------
function Test-Port([int]$port) {
    $c = New-Object Net.Sockets.TcpClient
    try { $c.Connect('127.0.0.1', $port); $c.Close(); return $true }
    catch { return $false }
}

function Start-Hidden([string]$file, [string[]]$argList, [string]$log, [string]$workDir = '') {
    $p = @{ FilePath = $file; ArgumentList = $argList; WindowStyle = 'Hidden';
            RedirectStandardOutput = $log; RedirectStandardError = "$log.err" }
    if ($workDir) { $p.WorkingDirectory = $workDir }
    Start-Process @p | Out-Null
}

function Test-Npm {
    return [bool](Get-Command npm -ErrorAction SilentlyContinue)
}

# 建立 ASCII junction，绕开 Windows Nginx 无法处理中文路径的问题
function Ensure-AsciiLinks {
    if (-not (Test-Path $NginxExe)) {
        cmd /c "mklink /J `"$NginxLink`" `"$NginxDirReal`"" | Out-Null
    }
    if (-not (Test-Path (Join-Path $DistLink 'index.html'))) {
        cmd /c "mklink /J `"$DistLink`" `"$DistDir`"" | Out-Null
    }
    if (-not (Test-Path $NginxExe) -or -not (Test-Path (Join-Path $DistLink 'index.html'))) {
        Write-Err "创建 ASCII 运行路径失败：$NginxLink / $DistLink （请确认 C:\Users\Public 可写）"
    }
}

# 优雅停止 Nginx（若在运行）
function Stop-Nginx {
    if (-not (Test-Path $NginxExe)) { return }
    if (Test-Path (Join-Path $NginxLink 'logs\nginx.pid')) {
        Write-Host '停止 Nginx ...'
        & $NginxExe -p "$NginxLink" -c "$NginxRuntimeConf" -s stop
        Start-Sleep -Seconds 1
    }
}
