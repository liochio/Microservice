$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:PATH = "$env:JAVA_HOME\bin;C:\Program Files\nodejs;$env:PATH"
$root = $PSScriptRoot

Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host " STARTING ALL ECOSYSTEM SERVICES (SPRING BOOT + PYTHON + FRONTENDS)" -ForegroundColor Green
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host ""

# 1. Eureka Registry (:8761)
Write-Host "[1/11] Starting Eureka Service Registry (:8761)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList "/k ""set JAVA_HOME=C:\Program Files\Java\jdk-17&& set PATH=C:\Program Files\Java\jdk-17\bin;%PATH%&& cd /d $root\SpringBoot && .\mvnw.cmd -pl service-registry spring-boot:run"""
Start-Sleep -Seconds 4

# 2. Auth & IAM Service (:8081)
Write-Host "[2/11] Starting Auth & IAM Service (:8081)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList "/k ""set JAVA_HOME=C:\Program Files\Java\jdk-17&& set PATH=C:\Program Files\Java\jdk-17\bin;%PATH%&& cd /d $root\SpringBoot && .\mvnw.cmd -pl auth-service spring-boot:run"""
Start-Sleep -Seconds 3

# 3. API Gateway (:8080)
Write-Host "[3/11] Starting API Gateway (:8080)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList "/k ""set JAVA_HOME=C:\Program Files\Java\jdk-17&& set PATH=C:\Program Files\Java\jdk-17\bin;%PATH%&& cd /d $root\SpringBoot && .\mvnw.cmd -pl api-gateway spring-boot:run"""
Start-Sleep -Seconds 3

# 4. Entity Service (:8082)
Write-Host "[4/11] Starting Entity Service (:8082)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList "/k ""set JAVA_HOME=C:\Program Files\Java\jdk-17&& set PATH=C:\Program Files\Java\jdk-17\bin;%PATH%&& cd /d $root\SpringBoot && .\mvnw.cmd -pl entity-service spring-boot:run"""

# 5. Payment Service (:8083)
Write-Host "[5/11] Starting Payment Service (:8083)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList "/k ""set JAVA_HOME=C:\Program Files\Java\jdk-17&& set PATH=C:\Program Files\Java\jdk-17\bin;%PATH%&& cd /d $root\SpringBoot && .\mvnw.cmd -pl payment-service spring-boot:run"""

# 6. Notification Service (:8084)
Write-Host "[6/11] Starting Notification Service (:8084)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList "/k ""set JAVA_HOME=C:\Program Files\Java\jdk-17&& set PATH=C:\Program Files\Java\jdk-17\bin;%PATH%&& cd /d $root\SpringBoot && .\mvnw.cmd -pl notification-service spring-boot:run"""

# 7. Core Banking Ledger Service (:8085)
Write-Host "[7/11] Starting Core Banking Ledger Service (:8085)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList "/k ""set JAVA_HOME=C:\Program Files\Java\jdk-17&& set PATH=C:\Program Files\Java\jdk-17\bin;%PATH%&& cd /d $root\SpringBoot && .\mvnw.cmd -pl ledger-service spring-boot:run"""

# 8. Dedicated OTP Service (:8094)
Write-Host "[8/11] Starting SmartOTP Service (:8094)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList "/k ""set JAVA_HOME=C:\Program Files\Java\jdk-17&& set PATH=C:\Program Files\Java\jdk-17\bin;%PATH%&& cd /d $root\SpringBoot && .\mvnw.cmd -pl otp-service spring-boot:run"""

# 9. Dedicated Worker Service (:8095)
Write-Host "[9/12] Starting Worker Service (:8095)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList "/k ""set JAVA_HOME=C:\Program Files\Java\jdk-17&& set PATH=C:\Program Files\Java\jdk-17\bin;%PATH%&& cd /d $root\SpringBoot && .\mvnw.cmd -pl worker-service spring-boot:run"""

# 10. Python FinTech AI & IoT Core (:8000)
Write-Host "[10/12] Starting Python FinTech AI Core (:8000)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList "/k ""cd /d $root\Python && .\venv\Scripts\python.exe -m uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload"""

# 11. Unified Frontend Portal (:5173 - All Roles)
Write-Host "[11/11] Starting Unified Frontend Portal (:5173 - Single Source for All Roles)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList "/k ""set PATH=C:\Program Files\nodejs;%PATH%&& cd /d $root\frontend\liochio-app-portal && npm run dev"""

Write-Host ""
Write-Host "========================================================================" -ForegroundColor Green
Write-Host " ALL SERVICES HAVE BEEN LAUNCHED!" -ForegroundColor Green
Write-Host " Unified Frontend Portal:       http://localhost:5173 (SuperAdmin + Corp + Retail)" -ForegroundColor White
Write-Host " Reactive API Gateway:          http://localhost:8080" -ForegroundColor White
Write-Host " Service Registry (Eureka):     http://localhost:8761" -ForegroundColor White
Write-Host " Python FinTech AI Swagger:     http://127.0.0.1:8000/docs" -ForegroundColor White
Write-Host "========================================================================" -ForegroundColor Green
