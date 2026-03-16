@echo off
echo ========================================
echo 大学问答平台 - GitHub部署脚本
echo ========================================
echo.

REM 检查是否已初始化Git仓库
if exist .git (
    echo [√] Git仓库已初始化
) else (
    echo [!] 正在初始化Git仓库...
    git init
)

echo.
echo [1/4] 添加所有文件到Git...
git add .

echo.
echo [2/4] 提交代码到本地仓库...
git commit -m "Initial commit: 大学问答平台项目

- 完成前后端API联调
- 实现完整的中文化界面
- 添加用户、课程、问题、回答管理功能
- 集成AI智能助手功能
- 完善项目文档"

echo.
echo [3/4] 连接到GitHub远程仓库...
echo 请输入你的GitHub仓库地址（格式：https://github.com/用户名/仓库名.git）
set /p REPO_URL=GitHub仓库地址:

git remote add origin %REPO_URL%

echo.
echo [4/4] 推送代码到GitHub...
git branch -M main
git push -u origin main

echo.
echo ========================================
echo [√] 部署完成！
echo ========================================
echo.
echo 现在你可以访问你的GitHub仓库查看项目了！
echo.
pause