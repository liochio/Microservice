@echo off
setlocal enabledelayedexpansion

echo ======================================================================
echo STARTING ALL 13 LIOCHIO ECOSYSTEM SERVICES
echo ======================================================================
echo.

set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "NODE_HOME=C:\Program Files\nodejs"
set "PATH=%JAVA_HOME%\bin;%NODE_HOME%;%PATH%"

echo [1/13] Starting Eureka Service Registry (Port 8761) ...
start "1. Eureka Registry (8761)" cmd /k "set ""JAVA_HOME=C:\Program Files\Java\jdk-17"" && set ""PATH=C:\Program Files\Java\jdk-17\bin;%PATH%"" && cd /d %~dp0SpringBoot && .\mvnw.cmd -pl service-registry spring-boot:run"
ping 127.0.0.1 -n 4 > nul

echo [2/13] Starting Auth IAM Service (Port 8081) ...
start "2. Auth Service (8081)" cmd /k "set ""JAVA_HOME=C:\Program Files\Java\jdk-17"" && set ""PATH=C:\Program Files\Java\jdk-17\bin;%PATH%"" && cd /d %~dp0SpringBoot && .\mvnw.cmd -pl auth-service spring-boot:run"
ping 127.0.0.1 -n 3 > nul

echo [3/13] Starting API Gateway (Port 8080) ...
start "3. API Gateway (8080)" cmd /k "set ""JAVA_HOME=C:\Program Files\Java\jdk-17"" && set ""PATH=C:\Program Files\Java\jdk-17\bin;%PATH%"" && cd /d %~dp0SpringBoot && .\mvnw.cmd -pl api-gateway spring-boot:run"
ping 127.0.0.1 -n 3 > nul

echo [4/13] Starting Entity Service (Port 8082) ...
start "4. Entity Service (8082)" cmd /k "set ""JAVA_HOME=C:\Program Files\Java\jdk-17"" && set ""PATH=C:\Program Files\Java\jdk-17\bin;%PATH%"" && cd /d %~dp0SpringBoot && .\mvnw.cmd -pl entity-service spring-boot:run"

echo [5/13] Starting Media Service (Port 8083) ...
start "5. Media Service (8083)" cmd /k "set ""JAVA_HOME=C:\Program Files\Java\jdk-17"" && set ""PATH=C:\Program Files\Java\jdk-17\bin;%PATH%"" && cd /d %~dp0SpringBoot && .\mvnw.cmd -pl media-service spring-boot:run"

echo [6/13] Starting Notification Service (Port 8084) ...
start "6. Notification Service (8084)" cmd /k "set ""JAVA_HOME=C:\Program Files\Java\jdk-17"" && set ""PATH=C:\Program Files\Java\jdk-17\bin;%PATH%"" && cd /d %~dp0SpringBoot && .\mvnw.cmd -pl notification-service spring-boot:run"

echo [7/13] Starting Payment Service (Port 8085) ...
start "7. Payment Service (8085)" cmd /k "set ""JAVA_HOME=C:\Program Files\Java\jdk-17"" && set ""PATH=C:\Program Files\Java\jdk-17\bin;%PATH%"" && cd /d %~dp0SpringBoot && .\mvnw.cmd -pl payment-service spring-boot:run"

echo [8/13] Starting Dedicated OTP Service (Port 8094) ...
start "8. OTP Service (8094)" cmd /k "set ""JAVA_HOME=C:\Program Files\Java\jdk-17"" && set ""PATH=C:\Program Files\Java\jdk-17\bin;%PATH%"" && cd /d %~dp0SpringBoot && .\mvnw.cmd -pl otp-service spring-boot:run"

echo [9/13] Starting Dedicated Worker Service (Port 8095) ...
start "9. Worker Service (8095)" cmd /k "set ""JAVA_HOME=C:\Program Files\Java\jdk-17"" && set ""PATH=C:\Program Files\Java\jdk-17\bin;%PATH%"" && cd /d %~dp0SpringBoot && .\mvnw.cmd -pl worker-service spring-boot:run"

echo [10/13] Starting Python FinTech AI Core (Port 8089) ...
start "10. Python FinTech (8089)" cmd /k "cd /d %~dp0Python && .\venv\Scripts\python.exe -m uvicorn app.main:app --host 127.0.0.1 --port 8089 --reload"

echo [11/12] Starting SuperAdmin Platform Portal (Port 5170) ...
start "11. SuperAdmin Portal (5170)" cmd /k "set ""PATH=C:\Program Files\nodejs;%PATH%"" && cd /d %~dp0frontend\liochio-admin && npm run dev"

echo [12/12] Starting Unified App Portal (Port 5173 - Corp B2B, Retail, Business) ...
start "12. Unified App Portal (5173)" cmd /k "set ""PATH=C:\Program Files\nodejs;%PATH%"" && cd /d %~dp0frontend\liochio-app-portal && npm run dev"

echo.
echo ======================================================================
echo ALL 12 ENTERPRISE ECOSYSTEM SERVICES HAVE BEEN LAUNCHED!
echo SuperAdmin Core Portal:        http://localhost:5170
echo Unified App Portal:            http://localhost:5173
echo Reactive API Gateway:          http://localhost:8080
echo Service Registry (Eureka):     http://localhost:8761
echo Python FinTech AI Swagger:     http://127.0.0.1:8089/docs
echo ======================================================================
