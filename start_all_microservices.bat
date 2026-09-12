@echo off
chcp 65001 > nul
setlocal enabledelayedexpansion

echo ======================================================================
echo 🚀 KHỞI ĐỘNG TOÀN BỘ HỆ SINH THÁI MICROSERVICES (SPRING BOOT + PYTHON)
echo ======================================================================
echo.

set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "NODE_HOME=C:\Program Files\nodejs"
set "PATH=%JAVA_HOME%\bin;%NODE_HOME%;%PATH%"

echo [1/10] Khởi động Service Registry (Eureka - Port 8761)...
start "1. Eureka Registry (:8761)" cmd /k "set ""JAVA_HOME=C:\Program Files\Java\jdk-17"" && set ""PATH=C:\Program Files\Java\jdk-17\bin;%PATH%"" && cd /d %~dp0SpringBoot && .\mvnw.cmd -pl service-registry spring-boot:run"
ping 127.0.0.1 -n 5 > nul

echo [2/10] Khởi động Auth IAM & Master Menus Engine (Port 8081)...
start "2. Auth IAM (:8081)" cmd /k "set ""JAVA_HOME=C:\Program Files\Java\jdk-17"" && set ""PATH=C:\Program Files\Java\jdk-17\bin;%PATH%"" && cd /d %~dp0SpringBoot && .\mvnw.cmd -pl auth-service spring-boot:run"
ping 127.0.0.1 -n 3 > nul

echo [3/10] Khởi động Reactive API Gateway (Port 8080)...
start "3. API Gateway (:8080)" cmd /k "set ""JAVA_HOME=C:\Program Files\Java\jdk-17"" && set ""PATH=C:\Program Files\Java\jdk-17\bin;%PATH%"" && cd /d %~dp0SpringBoot && .\mvnw.cmd -pl api-gateway spring-boot:run"
ping 127.0.0.1 -n 3 > nul

echo [4/10] Khởi động Entity & Headless CMS Engine (Port 8082)...
start "4. Entity Service (:8082)" cmd /k "set ""JAVA_HOME=C:\Program Files\Java\jdk-17"" && set ""PATH=C:\Program Files\Java\jdk-17\bin;%PATH%"" && cd /d %~dp0SpringBoot && .\mvnw.cmd -pl entity-service spring-boot:run"
ping 127.0.0.1 -n 2 > nul

echo [5/10] Khởi động Media Storage Service (Port 8083)...
start "5. Media Service (:8083)" cmd /k "set ""JAVA_HOME=C:\Program Files\Java\jdk-17"" && set ""PATH=C:\Program Files\Java\jdk-17\bin;%PATH%"" && cd /d %~dp0SpringBoot && .\mvnw.cmd -pl media-service spring-boot:run"
ping 127.0.0.1 -n 2 > nul

echo [6/10] Khởi động Enterprise Notification Service (Port 8084)...
start "6. Notification Service (:8084)" cmd /k "set ""JAVA_HOME=C:\Program Files\Java\jdk-17"" && set ""PATH=C:\Program Files\Java\jdk-17\bin;%PATH%"" && cd /d %~dp0SpringBoot && .\mvnw.cmd -pl notification-service spring-boot:run"
ping 127.0.0.1 -n 2 > nul

echo [7/10] Khởi động Payment & Transaction Service (Port 8085)...
start "7. Payment Service (:8085)" cmd /k "set ""JAVA_HOME=C:\Program Files\Java\jdk-17"" && set ""PATH=C:\Program Files\Java\jdk-17\bin;%PATH%"" && cd /d %~dp0SpringBoot && .\mvnw.cmd -pl payment-service spring-boot:run"
ping 127.0.0.1 -n 2 > nul

echo [8/10] Khởi động Dedicated SmartOTP Service (Port 8094)...
start "8. OTP Service (:8094)" cmd /k "set ""JAVA_HOME=C:\Program Files\Java\jdk-17"" && set ""PATH=C:\Program Files\Java\jdk-17\bin;%PATH%"" && cd /d %~dp0SpringBoot && .\mvnw.cmd -pl otp-service spring-boot:run"
ping 127.0.0.1 -n 2 > nul

echo [9/10] Khởi động Central Worker & Audit Service (Port 8095)...
start "9. Worker Service (:8095)" cmd /k "set ""JAVA_HOME=C:\Program Files\Java\jdk-17"" && set ""PATH=C:\Program Files\Java\jdk-17\bin;%PATH%"" && cd /d %~dp0SpringBoot && .\mvnw.cmd -pl worker-service spring-boot:run"
ping 127.0.0.1 -n 2 > nul

echo [10/10] Khởi động Python FinTech AI Core (Port 8089)...
start "10. Python FinTech AI (:8089)" cmd /k "cd /d %~dp0Python && .\venv\Scripts\python.exe -m uvicorn app.main:app --host 0.0.0.0 --port 8089"

echo.
echo ======================================================================
echo ✅ TOÀN BỘ MICROSERVICES BACKEND ĐÃ ĐƯỢC KHỞI ĐỘNG THÀNH CÔNG!
echo • Service Registry (Eureka):       http://localhost:8761
echo • API Gateway (Netty Reactive):    http://localhost:8080
echo • Auth IAM & Menus Engine:         http://localhost:8081
echo • Dynamic Entity & SDUI:           http://localhost:8082
echo • Media Storage Service:           http://localhost:8083
echo • Notification Engine:             http://localhost:8084
echo • Payment & Booking Service:       http://localhost:8085
echo • Python FinTech AI Swagger:       http://127.0.0.1:8089/docs
echo • Dedicated SmartOTP Service:      http://localhost:8094
echo • Worker & Audit Service:          http://localhost:8095
echo ======================================================================
