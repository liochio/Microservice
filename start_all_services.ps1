$ports = @(5173, 8000, 8080, 8081, 8082, 8083, 8084, 8085, 8094, 8761)
Write-Host "Stopping existing services on ports: $ports..."
foreach ($port in $ports) {
    $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if ($conns) {
        foreach ($conn in $conns) {
            Write-Host "Killing process $($conn.OwningProcess) on port $port"
            Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
        }
    }
}
Start-Sleep -Seconds 2

$JAVA_HOME = "C:\Program Files\Java\jdk-17"
$NODE_HOME = "C:\Program Files\nodejs"
$ROOT = "E:\Github\Back-end\Microservice"

Write-Host "1. Starting Eureka Service Registry (8761)..."
Start-Process cmd.exe -ArgumentList "/c cd /d `"$ROOT\SpringBoot`" && set JAVA_HOME=$JAVA_HOME&& set PATH=$JAVA_HOME\bin;$env:PATH&& mvnw.cmd -pl service-registry spring-boot:run" -WindowStyle Minimized
Start-Sleep -Seconds 6

Write-Host "2. Starting Auth IAM Service (8081)..."
Start-Process cmd.exe -ArgumentList "/c cd /d `"$ROOT\SpringBoot`" && set JAVA_HOME=$JAVA_HOME&& set PATH=$JAVA_HOME\bin;$env:PATH&& mvnw.cmd -pl auth-service spring-boot:run" -WindowStyle Minimized
Start-Sleep -Seconds 4

Write-Host "3. Starting API Gateway (8080)..."
Start-Process cmd.exe -ArgumentList "/c cd /d `"$ROOT\SpringBoot`" && set JAVA_HOME=$JAVA_HOME&& set PATH=$JAVA_HOME\bin;$env:PATH&& mvnw.cmd -pl api-gateway spring-boot:run" -WindowStyle Minimized
Start-Sleep -Seconds 3

Write-Host "4. Starting Entity Service (8082)..."
Start-Process cmd.exe -ArgumentList "/c cd /d `"$ROOT\SpringBoot`" && set JAVA_HOME=$JAVA_HOME&& set PATH=$JAVA_HOME\bin;$env:PATH&& mvnw.cmd -pl entity-service spring-boot:run" -WindowStyle Minimized
Start-Sleep -Seconds 2

Write-Host "5. Starting Payment Service (8083)..."
Start-Process cmd.exe -ArgumentList "/c cd /d `"$ROOT\SpringBoot`" && set JAVA_HOME=$JAVA_HOME&& set PATH=$JAVA_HOME\bin;$env:PATH&& mvnw.cmd -pl payment-service spring-boot:run" -WindowStyle Minimized
Start-Sleep -Seconds 2

Write-Host "6. Starting Notification Service (8084)..."
Start-Process cmd.exe -ArgumentList "/c cd /d `"$ROOT\SpringBoot`" && set JAVA_HOME=$JAVA_HOME&& set PATH=$JAVA_HOME\bin;$env:PATH&& mvnw.cmd -pl notification-service spring-boot:run" -WindowStyle Minimized
Start-Sleep -Seconds 2

Write-Host "7. Starting Ledger Service (8085)..."
Start-Process cmd.exe -ArgumentList "/c cd /d `"$ROOT\SpringBoot`" && set JAVA_HOME=$JAVA_HOME&& set PATH=$JAVA_HOME\bin;$env:PATH&& mvnw.cmd -pl ledger-service spring-boot:run" -WindowStyle Minimized
Start-Sleep -Seconds 2

Write-Host "8. Starting OTP Service (8094)..."
Start-Process cmd.exe -ArgumentList "/c cd /d `"$ROOT\SpringBoot`" && set JAVA_HOME=$JAVA_HOME&& set PATH=$JAVA_HOME\bin;$env:PATH&& mvnw.cmd -pl otp-service spring-boot:run" -WindowStyle Minimized
Start-Sleep -Seconds 2

Write-Host "9. Starting Python FinTech AI (8000)..."
Start-Process cmd.exe -ArgumentList "/c cd /d `"$ROOT\Python`" && .\venv\Scripts\python.exe -m uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload" -WindowStyle Minimized
Start-Sleep -Seconds 2

Write-Host "10. Starting Unified Frontend Portal (5173)..."
Start-Process cmd.exe -ArgumentList "/c cd /d `"$ROOT\frontend\liochio-app-portal`" && set PATH=$NODE_HOME;$env:PATH&& npm run dev" -WindowStyle Minimized
Start-Sleep -Seconds 2

Write-Host "All background processes triggered!"
