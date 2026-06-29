@echo off
taskkill /F /IM java.exe >nul 2>&1
taskkill /F /IM javaw.exe >nul 2>&1
timeout /t 2 /nobreak >nul
set JAVA_HOME=C:\PROGRA~1\Java\jdk-17
set MAVEN_HOME=D:\apache-maven-3.9.14
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%
set SPRING_PROFILE=docker
cd /d C:\Users\aihey\Desktop\java_study\java-mastery
rmdir /s /q target >nul 2>&1
call mvn clean package -DskipTests -q > build.log 2>&1
echo %ERRORLEVEL% > build_exit.txt
if %ERRORLEVEL% equ 0 (
    start javaw -jar target\java-mastery-2.0.0.jar --spring.profiles.active=%SPRING_PROFILE% --server.port=8080
    echo STARTED > build_status.txt
) else (
    echo FAILED > build_status.txt
)
