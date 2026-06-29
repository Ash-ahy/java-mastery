@echo off
set JAVA_HOME=C:\PROGRA~1\Java\jdk-17
set MAVEN_HOME=D:\apache-maven-3.9.14
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%
cd /d C:\Users\aihey\Desktop\java_study\java-mastery

echo ========================================
echo   Java Mastery v2.0 Pro
echo ========================================

:: Start Docker Desktop if not running
echo [1/2] Starting Docker Desktop...
start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
echo Waiting for Docker to start...
:wait_docker
timeout /t 5 /nobreak >nul
docker info >nul 2>&1
if %ERRORLEVEL% neq 0 goto wait_docker
echo Docker is ready!

:: Start databases
echo [2/2] Starting databases...
docker compose up -d
echo.
echo ========================================
echo   All services started!
echo   App:    http://localhost:8080
echo   Docs:   http://localhost:8080/doc.html
echo   MySQL:  localhost:13306
echo   Redis:  localhost:6380
echo ========================================
pause
