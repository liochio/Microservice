@echo off
echo ======================================================================
echo STARTING LIOCHIO SUPERADMIN CORE PLATFORM (:5170)
echo ======================================================================
set "PATH=C:\Program Files\nodejs;%PATH%"
cd /d %~dp0frontend\liochio-admin
npm run dev