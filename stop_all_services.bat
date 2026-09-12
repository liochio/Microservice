@echo off
echo ======================================================================
echo STOPPING ALL LIOCHIO ECOSYSTEM SERVICES
echo ======================================================================

echo Terminating Java Spring Boot microservices...
taskkill /F /IM java.exe /T 2>nul

echo Terminating Python Uvicorn servers...
taskkill /F /IM python.exe /T 2>nul

echo Terminating Vite & NodeJS Frontend servers...
taskkill /F /IM node.exe /T 2>nul

echo.
echo ======================================================================
echo ALL SERVICES HAVE BEEN SAFELY STOPPED!
echo ======================================================================
pause
