@echo off
echo ================================================================
echo   Launching FraudEngine Embedded Web Application Server...
echo ================================================================
echo   Opening: http://localhost:8080/
echo   Press Ctrl+C to stop the server.
echo ================================================================

java -cp "bin;lib/*" com.fraudengine.server.AppServer
pause
