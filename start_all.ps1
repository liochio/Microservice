$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:PATH = "$env:JAVA_HOME\bin;C:\Program Files\nodejs;$env:PATH"
$root = "d:\Github\Back-end\Microservice"

Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host " STARTING ALL 13 SERVICES (SPRING BOOT + PYTHON + 3 FRONTENDS)" -ForegroundColor Green
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host ""

# 1. Eureka Registry (:8761)
Write-Host "[1/13] Starting Eureka Service Registry (:8761)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList '/k "set JAVA_HOME=C:\Program Files\Java\jdk-17&& set PATH=C:\Program Files\Java\jdk-17\bin;%PATH%&& cd /d d:\Github\Back-end\Microservice\SpringBoot && .\mvnw.cmd -pl service-registry spring-boot:run"'
Start-Sleep -Seconds 4

# 2. Auth & IAM Service (:8081)
Write-Host "[2/13] Starting Auth & IAM Service (:8081)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList '/k "set JAVA_HOME=C:\Program Files\Java\jdk-17&& set PATH=C:\Program Files\Java\jdk-17\bin;%PATH%&& cd /d d:\Github\Back-end\Microservice\SpringBoot && .\mvnw.cmd -pl auth-service spring-boot:run"'
Start-Sleep -Seconds 3

# 3. API Gateway (:8080)
Write-Host "[3/13] Starting API Gateway (:8080)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList '/k "set JAVA_HOME=C:\Program Files\Java\jdk-17&& set PATH=C:\Program Files\Java\jdk-17\bin;%PATH%&& cd /d d:\Github\Back-end\Microservice\SpringBoot && .\mvnw.cmd -pl api-gateway spring-boot:run"'
Start-Sleep -Seconds 3

# 4. Entity Service (:8082)
Write-Host "[4/13] Starting Entity Service (:8082)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList '/k "set JAVA_HOME=C:\Program Files\Java\jdk-17&& set PATH=C:\Program Files\Java\jdk-17\bin;%PATH%&& cd /d d:\Github\Back-end\Microservice\SpringBoot && .\mvnw.cmd -pl entity-service spring-boot:run"'

# 5. Media Service (:8083)
Write-Host "[5/13] Starting Media Service (:8083)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList '/k "set JAVA_HOME=C:\Program Files\Java\jdk-17&& set PATH=C:\Program Files\Java\jdk-17\bin;%PATH%&& cd /d d:\Github\Back-end\Microservice\SpringBoot && .\mvnw.cmd -pl media-service spring-boot:run"'

# 6. Notification Service (:8084)
Write-Host "[6/13] Starting Notification Service (:8084)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList '/k "set JAVA_HOME=C:\Program Files\Java\jdk-17&& set PATH=C:\Program Files\Java\jdk-17\bin;%PATH%&& cd /d d:\Github\Back-end\Microservice\SpringBoot && .\mvnw.cmd -pl notification-service spring-boot:run"'

# 7. Payment Service (:8085)
Write-Host "[7/13] Starting Payment Service (:8085)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList '/k "set JAVA_HOME=C:\Program Files\Java\jdk-17&& set PATH=C:\Program Files\Java\jdk-17\bin;%PATH%&& cd /d d:\Github\Back-end\Microservice\SpringBoot && .\mvnw.cmd -pl payment-service spring-boot:run"'

# 8. Dedicated OTP Service (:8094)
Write-Host "[8/13] Starting SmartOTP Service (:8094)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList '/k "set JAVA_HOME=C:\Program Files\Java\jdk-17&& set PATH=C:\Program Files\Java\jdk-17\bin;%PATH%&& cd /d d:\Github\Back-end\Microservice\SpringBoot && .\mvnw.cmd -pl otp-service spring-boot:run"'

# 9. Dedicated Worker Service (:8095)
Write-Host "[9/13] Starting Worker Service (:8095)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList '/k "set JAVA_HOME=C:\Program Files\Java\jdk-17&& set PATH=C:\Program Files\Java\jdk-17\bin;%PATH%&& cd /d d:\Github\Back-end\Microservice\SpringBoot && .\mvnw.cmd -pl worker-service spring-boot:run"'

# 10. Python FinTech AI Core (:8089)
Write-Host "[10/13] Starting Python FinTech AI Core (:8089)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList '/k "cd /d d:\Github\Back-end\Microservice\Python && .\venv\Scripts\python.exe -m uvicorn app.main:app --host 127.0.0.1 --port 8089 --reload"'

# 11. SuperAdmin Portal (:5170)
Write-Host "[11/13] Starting SuperAdmin Portal (:5170)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList '/k "set PATH=C:\Program Files\nodejs;%PATH%&& cd /d d:\Github\Back-end\Microservice\frontend\liochio-admin && npm run dev"'

# 12. Corporate SaaS Portal (:5171)
Write-Host "[12/13] Starting Corporate SaaS Portal (:5171)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList '/k "set PATH=C:\Program Files\nodejs;%PATH%&& cd /d d:\Github\Back-end\Microservice\frontend\liochio-corp-fintech && npm run dev"'

# 13. Retail FinTech Portal (:5172)
Write-Host "[13/13] Starting Retail FinTech Portal (:5172)..." -ForegroundColor Yellow
Start-Process cmd -ArgumentList '/k "set PATH=C:\Program Files\nodejs;%PATH%&& cd /d d:\Github\Back-end\Microservice\frontend\liochio-retail-fintech && npm run dev"'

Write-Host ""
Write-Host "========================================================================" -ForegroundColor Green
Write-Host " ALL 13 SERVICES HAVE BEEN LAUNCHED!" -ForegroundColor Green
Write-Host " SuperAdmin Master Portal:     http://localhost:5170" -ForegroundColor White
Write-Host " Corporate SaaS Portal:         http://localhost:5171" -ForegroundColor White
Write-Host " Retail FinTech Consumer PWA:   http://localhost:5172" -ForegroundColor White
Write-Host " Reactive API Gateway:          http://localhost:8080" -ForegroundColor White
Write-Host " Service Registry (Eureka):     http://localhost:8761" -ForegroundColor White
Write-Host " Python FinTech AI Swagger:     http://127.0.0.1:8089/docs" -ForegroundColor White
Write-Host "========================================================================" -ForegroundColor Green
