@echo off
title Cloud ERP Launcher (Java Spring Boot + React)
echo ===================================================
echo Starting Cloud ERP (Spring Boot + React Vite)
echo ===================================================

cd /d "%~dp0cloud_erp\backend-java"
start "Cloud ERP Backend (Spring Boot)" java -jar target\cloud-erp-backend-1.0.0.jar

timeout /t 3 /nobreak >nul

cd /d "%~dp0cloud_erp\frontend"
start "Cloud ERP Frontend (Vite)" cmd /k "npm run dev"

echo.
echo ===================================================
echo Backend API : http://127.0.0.1:8000
echo Frontend UI : http://localhost:5173
echo ===================================================
echo.
