@echo off
echo Important! Site monitoring MUST be running!
set /p question="Reset admin credentials to admin / admin? [Y/N]: "
set "TRUE="
if "%question%" == "Y" set TRUE=1
if "%question%" == "y" set TRUE=1
if defined TRUE (
java -jar sitemonitoring.jar --reset-admin-credentials
)
pause
