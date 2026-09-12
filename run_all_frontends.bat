@echo off
set "PATH=C:\Program Files\nodejs;%PATH%"

echo ======================================================================
echo STARTING 2 ENTERPRISE FRONTEND PORTALS (ADMIN:5170, APP-PORTAL:5173)
echo ======================================================================
echo.

start "1. SuperAdmin Platform (5170)" cmd /k "set ""PATH=C:\Program Files\nodejs;%PATH%"" && cd /d %~dp0frontend\liochio-admin && npm run dev"
start "2. Unified App Portal (5173)" cmd /k "set ""PATH=C:\Program Files\nodejs;%PATH%"" && cd /d %~dp0frontend\liochio-app-portal && npm run dev"

echo All 2 enterprise frontend portals have been started!
echo 1. SuperAdmin Platform:    http://localhost:5170
echo 2. Unified App Portal:     http://localhost:5173 (Corp B2B + Retail + Business)