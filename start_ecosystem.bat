@echo off
setlocal enabledelayedexpansion

echo ======================================================================
echo 🚀 KHỞI ĐỘNG HỆ THỐNG TOÀN DIỆN LIOCHIO (MICROSERVICES + IOT + FRONTEND)
echo ======================================================================

set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "NODE_HOME=C:\Program Files\nodejs"
set "PATH=%JAVA_HOME%\bin;%NODE_HOME%;%PATH%"

echo [1/10] Starting Eureka Service Registry (8761)...
start "Eureka-8761" /min cmd.exe /k "cd /d E:\Github\Back-end\Microservice\SpringBoot && mvnw.cmd -pl service-registry spring-boot:run"
ping 127.0.0.1 -n 4 > nul

echo [2/10] Starting Auth IAM Service (8081)...
start "Auth-8081" /min cmd.exe /k "cd /d E:\Github\Back-end\Microservice\SpringBoot && mvnw.cmd -pl auth-service spring-boot:run"
ping 127.0.0.1 -n 3 > nul

echo [3/10] Starting API Gateway (8080)...
start "Gateway-8080" /min cmd.exe /k "cd /d E:\Github\Back-end\Microservice\SpringBoot && mvnw.cmd -pl api-gateway spring-boot:run"
ping 127.0.0.1 -n 2 > nul

echo [4/10] Starting Entity Service (8082)...
start "Entity-8082" /min cmd.exe /k "cd /d E:\Github\Back-end\Microservice\SpringBoot && mvnw.cmd -pl entity-service spring-boot:run"
ping 127.0.0.1 -n 2 > nul

echo [5/10] Starting Payment Service (8083)...
start "Payment-8083" /min cmd.exe /k "cd /d E:\Github\Back-end\Microservice\SpringBoot && mvnw.cmd -pl payment-service spring-boot:run"
ping 127.0.0.1 -n 2 > nul

echo [6/10] Starting Notification Service (8084)...
start "Notification-8084" /min cmd.exe /k "cd /d E:\Github\Back-end\Microservice\SpringBoot && mvnw.cmd -pl notification-service spring-boot:run"
ping 127.0.0.1 -n 2 > nul

echo [7/10] Starting Core Banking Ledger Service (8085)...
start "Ledger-8085" /min cmd.exe /k "cd /d E:\Github\Back-end\Microservice\SpringBoot && mvnw.cmd -pl ledger-service spring-boot:run"
ping 127.0.0.1 -n 2 > nul

echo [8/10] Starting Dedicated OTP Service (8094)...
start "OTP-8094" /min cmd.exe /k "cd /d E:\Github\Back-end\Microservice\SpringBoot && mvnw.cmd -pl otp-service spring-boot:run"
ping 127.0.0.1 -n 2 > nul

echo [9/10] Starting Python FinTech AI Core (8000)...
start "Python-8000" /min cmd.exe /k "cd /d E:\Github\Back-end\Microservice\Python && .\venv\Scripts\python.exe -m uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload"
ping 127.0.0.1 -n 2 > nul

echo [10/10] Starting Unified Frontend Portal (5173)...
start "Frontend-5173" /min cmd.exe /k "cd /d E:\Github\Back-end\Microservice\frontend\liochio-app-portal && npm run dev"

echo.
echo ======================================================================
echo ✅ ĐÃ KÍCH HOẠT THÀNH CÔNG 100% CÁC DỊCH VỤ!
echo • Frontend Portal:   http://localhost:5173
echo • API Gateway:       http://localhost:8080
echo • Eureka Registry:   http://localhost:8761
echo • Python AI Swagger: http://127.0.0.1:8000/docs
echo ======================================================================
