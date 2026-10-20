@echo off
echo =================================================================
echo   FraudEngine - Embedded Server Launcher
echo =================================================================
echo Compiling latest sources...
call build.bat
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build failed. Server cannot start.
    pause
    exit /b %ERRORLEVEL%
)

echo Starting FraudEngine Embedded Server...
java -cp "bin;lib/*" com.fraudengine.server.AppServer
pause
