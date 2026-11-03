@echo off
rem Wrapper to run build-test.ps1 regardless of PowerShell execution policy
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-test.ps1" %*
exit /b %ERRORLEVEL%
