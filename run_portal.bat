@echo off
echo ======================================================================
echo STARTING LIOCHIO UNIFIED APP PORTAL (:5173 - CORP B2B, RETAIL, BUSINESS)
echo ======================================================================
set "PATH=C:\Program Files\nodejs;%PATH%"
cd /d %~dp0frontend\liochio-app-portal
npm run dev
