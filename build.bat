@echo off
chcp 65001 > nul
setlocal enabledelayedexpansion

:: 配置路径变量
set "PACKAGE_DIR=E:\GITHOME\xzs\source\xzs\src\main\resources\static"
set "ADMIN_DIR=E:\GITHOME\xzs\source\vue\xzs-admin"
set "STUDENT_DIR=E:\GITHOME\xzs\source\vue\xzs-student"
set "MAVEN_PROJECT=E:\GITHOME\xzs\source\xzs"

:: 1. 删除打包文件夹下的所有内容
echo [1/5] 正在清理打包文件夹: %PACKAGE_DIR%
if exist "%PACKAGE_DIR%" (
    rmdir /s /q "%PACKAGE_DIR%"
    mkdir "%PACKAGE_DIR%"
) else (
    mkdir "%PACKAGE_DIR%"
)
echo 打包文件夹已清理完毕

:: 2. 在xzs-admin目录执行npm构建
echo [2/5] 正在xzs-admin目录执行npm构建...
cd /d "%ADMIN_DIR%"
if exist "package.json" (
    ::call npm install
    call npm run build
    echo xzs-admin构建完成
) else (
    echo 错误：%ADMIN_DIR%目录下未找到package.json
    pause
    exit /b 1
)

:: 3. 在xzs-student目录执行npm构建
echo [3/5] 正在xzs-student目录执行npm构建...
cd /d "%STUDENT_DIR%"
if exist "package.json" (
    ::call npm install
    call npm run build
    echo xzs-student构建完成
) else (
    echo 错误：%STUDENT_DIR%目录下未找到package.json
    pause
    exit /b 1
)

:: 4. 复制admin文件夹
echo [4/5] 正在复制admin文件夹...
if exist "%ADMIN_DIR%\admin" (
    xcopy /e /y /q "%ADMIN_DIR%\admin" "%PACKAGE_DIR%\admin\"
    echo admin文件夹复制完成
) else (
    echo 错误：admin构建输出目录不存在 %ADMIN_DIR%\admin
    pause
    exit /b 1
)

:: 5. 复制student文件夹
echo [5/5] 正在复制student文件夹...
if exist "%STUDENT_DIR%\student" (
    xcopy /e /y /q "%STUDENT_DIR%\student" "%PACKAGE_DIR%\student\"
    echo student文件夹复制完成
) else (
    echo 错误：student构建输出目录不存在 %STUDENT_DIR%\student
    pause
    exit /b 1
)

:: 6. 执行Maven构建
echo 正在跳转到Maven项目目录并执行构建...
cd /d "%MAVEN_PROJECT%"
if exist "pom.xml" (
    echo 正在执行 mvn install...
    call mvn install
    echo Maven构建完成！
) else (
    echo 错误：pom.xml文件未找到，目录不正确
    pause
    exit /b 1
)

:: 完成
echo 所有操作已完成！
pause