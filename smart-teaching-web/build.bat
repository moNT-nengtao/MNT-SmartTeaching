@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo       前端自动构建部署脚本
echo ========================================
echo.

:: ========== 路径定义 ==========
set NGINX_DIR=F:\GitHub\porject\MNT-SmartTeaching\mnt-st\nginx-1.31.3
set NGINX_EXE=%NGINX_DIR%\nginx.exe
set NGINX_NAME=nginx.exe
set DIST_PATH=dist
set NGINX_PID_FILE=%NGINX_DIR%\logs\nginx.pid

:: ========================================
:: 步骤1：彻底关闭 Nginx
:: ========================================
echo [1/4] 正在关闭 Nginx...

:: 方法1：通过 PID 文件关闭
if exist "%NGINX_PID_FILE%" (
    echo        通过 PID 文件关闭...
    for /f %%i in ('type "%NGINX_PID_FILE%"') do (
        taskkill /pid %%i 2>nul
    )
    timeout /t 2 /nobreak >nul
)

:: 方法2：通过进程名关闭
tasklist | findstr /i "%NGINX_NAME%" >nul
if !errorlevel! equ 0 (
    echo        终止 Nginx 进程...
    taskkill /f /im "%NGINX_NAME%" >nul 2>&1
    timeout /t 3 /nobreak >nul
)

:: 验证是否真的关闭了
tasklist | findstr /i "%NGINX_NAME%" >nul
if !errorlevel! equ 0 (
    echo        ✗ Nginx 关闭失败，请手动关闭
    pause
    exit /b 1
) else (
    echo        ✓ Nginx 已关闭
)

:: 删除可能残留的 PID 文件
if exist "%NGINX_PID_FILE%" (
    del "%NGINX_PID_FILE%" 2>nul
)
echo.

:: ========================================
:: 步骤2：删除 dist 文件夹
:: ========================================
echo [2/4] 正在删除 dist 文件夹...
if exist "%DIST_PATH%" (
    rmdir /s /q "%DIST_PATH%"
    if !errorlevel! equ 0 (
        echo        ✓ dist 文件夹已删除
    ) else (
        echo        ✗ dist 文件夹删除失败
        pause
        exit /b 1
    )
) else (
    echo        dist 文件夹不存在，跳过
)
echo.

:: ========================================
:: 步骤3：执行 npm 构建
:: ========================================
echo [3/4] 正在构建项目...
echo.
call npm run build

if !errorlevel! neq 0 (
    echo.
    echo        ✗ 构建失败，请检查错误信息
    pause
    exit /b 1
)

echo.
echo        ✓ 构建成功！
echo.

:: ========================================
:: 步骤4：启动 Nginx
:: ========================================
echo [4/4] 正在启动 Nginx...

cd /d "%NGINX_DIR%"

if exist "%NGINX_EXE%" (
    echo        启动 Nginx...
    
    :: 使用 start 启动
    start "Nginx" "%NGINX_EXE%"
    
    :: 等待启动
    timeout /t 3 /nobreak >nul
    
    :: 检查进程
    tasklist | findstr /i "%NGINX_NAME%" >nul
    if !errorlevel! equ 0 (
        echo        ✓ Nginx 已启动
    ) else (
        echo        尝试直接启动...
        "%NGINX_EXE%"
        timeout /t 3 /nobreak >nul
        tasklist | findstr /i "%NGINX_NAME%" >nul
        if !errorlevel! equ 0 (
            echo        ✓ Nginx 已启动
        ) else (
            echo        ✗ Nginx 启动失败
            echo        请手动检查 Nginx 配置
            pause
            exit /b 1
        )
    )
) else (
    echo        ✗ 找不到 Nginx 文件: %NGINX_EXE%
    pause
    exit /b 1
)

cd /d "%~dp0"
echo.

:: ========================================
:: 完成
:: ========================================
echo ========================================
echo ✓ 部署完成！
echo   Nginx 已重新启动
echo ========================================
echo.

pause
exit /b 0