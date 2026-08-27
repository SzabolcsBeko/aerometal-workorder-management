@echo off
setlocal

echo ==========================================
echo   WorkOrder Management Application UI
echo ==========================================
echo.

set "ROOT=%~dp0"
set "BACKEND=%ROOT%backend"
set "FRONTEND=%ROOT%frontend"


echo Starting React frontend...
start "React Frontend" cmd /k "cd /d "%FRONTEND%" && npm run dev"

echo.
echo ==========================================
echo Application startup initiated.
echo ==========================================
echo Backend:  http://localhost:8080
echo Frontend: http://localhost:5173
echo ==========================================

endlocal


