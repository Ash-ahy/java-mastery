@echo off
set JAVA_HOME=C:\PROGRA~1\Java\jdk-17
set SPRING_PROFILE=docker
cd /d C:\Users\aihey\Desktop\java_study\java-mastery
C:\PROGRA~1\Java\jdk-17\bin\java -jar target\java-mastery-2.0.0.jar --spring.profiles.active=%SPRING_PROFILE% --server.port=8080 > startup.log 2>&1
type startup.log
pause
