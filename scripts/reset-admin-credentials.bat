@echo off
echo Important! Site monitoring MUST be running!
set /p question="Reset admin credentials to admin / admin? [Y/N]: "
set "TRUE="
if "%question%" == "Y" set TRUE=1
if "%question%" == "y" set TRUE=1
if defined TRUE (
java -jar -Djava.io.tmpdir=sitemonitoring-temp -Dspring.profiles.active=standalone -Ddbport=9001 sitemonitoring.jar --reset-admin-credentials
)
pause
