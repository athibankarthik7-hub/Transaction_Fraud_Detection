@echo off
echo ================================================================
echo   Launching FraudEngine JavaFX Analyst Desktop Dashboard...
echo ================================================================

java --module-path "lib" --add-modules javafx.controls,javafx.graphics,javafx.base -cp "bin;lib/*" com.fraudengine.ui.AnalystDashboardLauncher
pause
