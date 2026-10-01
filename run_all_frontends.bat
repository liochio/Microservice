@echo off
set "PATH=C:\Program Files\nodejs;%PATH%"

echo ======================================================================
echo STARTING UNIFIED FRONTEND PLATFORM (PORT 5173 - SINGLE SOURCE FOR ALL ROLES)
echo ======================================================================
echo.

start "Liochio Unified Portal (5173)" cmd /k "set ""PATH=C:\Program Files\nodejs;%PATH%"" && cd /d %~dp0frontend\liochio-app-portal && npm run dev"

echo Unified Frontend has been started!
echo URL: http://localhost:5173 (SuperAdmin + Corp B2B + Business Ops + Retail)