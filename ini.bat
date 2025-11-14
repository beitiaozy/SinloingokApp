@echo off
setlocal enabledelayedexpansion

:: 配置路径
set "PROJECT_DIR=E:\sinloingok\github\sinloingok"
set "REMOTE_URL=git@github.com:beitiaozy/Sinloingok.git"

:: 进入项目目录
cd /d "%PROJECT_DIR%"
if errorlevel 1 (
    echo ❌ 目录不存在: %PROJECT_DIR%
    pause
    exit /b 1
)

echo 🧹 正在删除旧的 .git 目录...
rd /s /q .git

echo 🌱 初始化 Git 仓库...
git init

echo 📄 创建 .gitignore 文件...
(
echo # Java
echo target/
echo *.class
echo *.log
echo:
echo # Node
echo node_modules/
echo dist/
echo:
echo # IDE
echo .idea/
echo .vscode/
echo:
echo # 环境变量
echo .env
echo *.key
echo *.pem
) > .gitignore

echo ➕ 添加所有文件到 Git...
git add .

echo ✅ 提交初始版本...
git commit -m "Initial commit for Sinloingok"

echo 🌐 设置远程仓库地址...
git remote remove origin 2>nul
git remote add origin %REMOTE_URL%

echo 🚀 推送到 GitHub 仓库（强制推送 main）...
git branch -M main
git push -u origin main --force

echo ✅ 上传完成，请登录 https://github.com/beitiaozy/Sinloingok 查看代码
pause
