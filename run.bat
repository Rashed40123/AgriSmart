@echo off
cd /d "%~dp0"
set "MAVEN_HOME=%~dp0apache-maven-3.9.9"
set "PATH=%MAVEN_HOME%\bin;%PATH%"
mvn clean compile javafx:run
pause
