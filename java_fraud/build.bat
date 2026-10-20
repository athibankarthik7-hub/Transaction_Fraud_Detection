@echo off
echo ================================================================
echo   Compiling Deterministic Fraud Detection Engine Project...
echo ================================================================

if not exist "bin" mkdir bin
if not exist "logs" mkdir logs

javac -encoding UTF-8 -d bin -sourcepath src/main/java -cp "lib/*;bin;src/main/java" src/main/java/com/fraudengine/core/*.java src/main/java/com/fraudengine/exceptions/*.java src/main/java/com/fraudengine/rules/*.java src/main/java/com/fraudengine/data/*.java src/main/java/com/fraudengine/concurrency/*.java src/main/java/com/fraudengine/web/*.java src/main/java/com/fraudengine/ui/*.java src/main/java/com/fraudengine/server/*.java

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Build completed successfully! Class files generated in bin/
) else (
    echo [ERROR] Compilation failed. Please check errors above.
)
