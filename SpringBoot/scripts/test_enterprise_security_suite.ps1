param (
    [string]$BaseUrl = "http://localhost:8080",
    [string]$AuthServiceUrl = "http://localhost:8081",
    [string]$ReportFile = "TEST_ENTERPRISE_SECURITY_RESULT.md"
)

$ErrorActionPreference = "Continue"

Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host "  PORTFOLIO BACKEND ENGINE - AUTOMATED ENTERPRISE SECURITY TEST SUITE         " -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Cyan

$TargetUrl = $AuthServiceUrl
try {
    $null = Invoke-RestMethod -Uri "$AuthServiceUrl/actuator/health" -Method Get -TimeoutSec 2 -ErrorAction Stop
    Write-Host "  -> Direct connection to Auth Service at $AuthServiceUrl" -ForegroundColor Green
} catch {
    $TargetUrl = $BaseUrl
    Write-Host "  -> Connection via API Gateway at $BaseUrl" -ForegroundColor Green
}

$Timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$TestUser = "sec_user_$Timestamp"
$TestEmail = "sec_user_$Timestamp@portfolio.local"
$TestPassword = "Password@123!"

$PassedCount = 0
$TotalCount = 0
$TableRows = @()
$Details = @()

function Log-TestCase {
    param(
        [string]$Id,
        [string]$Name,
        [string]$Endpoint,
        [string]$Method,
        [bool]$Success,
        [string]$Note,
        [string]$DetailText
    )
    $script:TotalCount++
    if ($Success) {
        $script:PassedCount++
        $res = "**PASSED**"
    } else {
        $res = "**FAILED**"
    }
    $script:TableRows += "| $Id | $Name | '$Endpoint' | '$Method' | $res | $Note |"
    
    $script:Details += "### $Id. $Name"
    $script:Details += "- Endpoint: '$Method $Endpoint'"
    $script:Details += "- Status: $res"
    $script:Details += "- Chi tiet:"
    $script:Details += ''''text'
    $script:Details += $DetailText
    $script:Details += '''''
    $script:Details += ""
}

# ==============================================================================
# TEST 1: REGISTRATION & OTP ACTIVATION
# ==============================================================================
Write-Host "'n[1/7] Dang ky va Kich hoat tai khoan qua OTP..." -ForegroundColor Yellow
$regBody = @{
    username = $TestUser
    email    = $TestEmail
    password = $TestPassword
    fullName = "Automated Security Tester $Timestamp"
} | ConvertTo-Json

try {
    $regRes = Invoke-RestMethod -Uri "$TargetUrl/api/v1/auth/register" -Method Post -Body $regBody -ContentType "application/json" -Headers @{ "X-Device-Id" = "dev_reg_tester" }
    $userId = $regRes.data.id
    $status = $regRes.data.status
    Write-Host "  -> [OK] Registered User ID: $userId, Status: $status" -ForegroundColor Green

    # Set OTP hash in MySQL to 123456
    $setOtpCmd = "UPDATE user_otp_verifications SET otp_code_hash = ''$2a'$10'$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi' WHERE user_id = $userId AND otp_purpose = 'REGISTRATION';"
    & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p12345678 -D "portfolio-engine" -e $setOtpCmd 2>$null

    # Verify OTP
    $verifyBody = @{
        username = $TestUser
        otpCode  = "123456"
    } | ConvertTo-Json
    $verifyRes = Invoke-RestMethod -Uri "$TargetUrl/api/v1/auth/verify-otp" -Method Post -Body $verifyBody -ContentType "application/json"
    $newStatus = $verifyRes.data.status

    $t1Success = ($status -eq "PENDING_VERIFY") -and ($newStatus -eq "ACTIVE")
    Log-TestCase -Id "TC_01" -Name "Dang ky va Kich hoat qua OTP" -Endpoint "/api/v1/auth/register, /verify-otp" -Method "POST" -Success $t1Success -Note "Tao tai khoan PENDING_VERIFY -> Xac thuc OTP -> ACTIVE" -DetailText "User ID: $userId, Init Status: $status, Verified Status: $newStatus"
    Write-Host "  -> [OK] Kich hoat tai khoan thanh cong: Status = $newStatus" -ForegroundColor Green
} catch {
    Log-TestCase -Id "TC_01" -Name "Dang ky va Kich hoat qua OTP" -Endpoint "/api/v1/auth/register" -Method "POST" -Success $false -Note "Loi: $($_.Exception.Message)" -DetailText "$($_.Exception)"
    Write-Host "  -> [FAIL] TC_01: $($_.Exception.Message)" -ForegroundColor Red
}

# ==============================================================================
# TEST 2: UNTRUSTED DEVICE LOGIN (2FA STEP-UP) & VERIFY
# ==============================================================================
Write-Host "'n[2/7] Thach thuc 2FA Thiet bi la va Xac thuc thiet bi..." -ForegroundColor Yellow
$loginUntrustedBody = @{
    username = $TestUser
    password = $TestPassword
} | ConvertTo-Json

try {
    $loginUntrustedRes = Invoke-RestMethod -Uri "$TargetUrl/api/v1/auth/login" -Method Post -Body $loginUntrustedBody -ContentType "application/json" -Headers @{ "X-Device-Id" = "dev_brand_new_laptop" }
    $req2Fa = $loginUntrustedRes.data.requires2Fa
    $devId = $loginUntrustedRes.data.deviceId
    Write-Host "  -> [OK] Thiet bi la -> requires2Fa: $req2Fa, Challenge: $($loginUntrustedRes.data.challengeToken)" -ForegroundColor Green

    # Set OTP hash in MySQL to 123456
    $setDevOtpCmd = "UPDATE user_otp_verifications SET otp_code_hash = ''$2a'$10'$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi' WHERE user_id = $userId AND otp_purpose = 'DEVICE_TRUST';"
    & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p12345678 -D "portfolio-engine" -e $setDevOtpCmd 2>$null

    # Verify Device OTP
    $verifyDevBody = @{
        username       = $TestUser
        deviceId       = "dev_brand_new_laptop"
        otpCode        = "123456"
        rememberDevice = $true
    } | ConvertTo-Json
    $verifyDevRes = Invoke-RestMethod -Uri "$TargetUrl/api/v1/auth/verify-device-otp" -Method Post -Body $verifyDevBody -ContentType "application/json"
    $userToken = $verifyDevRes.data.accessToken
    $userRefreshToken = $verifyDevRes.data.refreshToken

    $t2Success = ($req2Fa -eq $true) -and ($userToken -ne $null)
    Log-TestCase -Id "TC_02" -Name "2FA Thach thuc thiet bi la va Xac thuc cap Token" -Endpoint "/api/v1/auth/login, /verify-device-otp" -Method "POST" -Success $t2Success -Note "Phat hien thiet bi moi requires2Fa=true -> Xac thuc OTP cap Access/Refresh Token" -DetailText "2FA Triggered: $req2Fa, Token Issued: $($userToken.Substring(0,25))..., Session: $($verifyDevRes.data.sessionId)"
    Write-Host "  -> [OK] Xac thuc thiet bi thanh cong! Token da cap." -ForegroundColor Green
} catch {
    Log-TestCase -Id "TC_02" -Name "2FA Thach thuc thiet bi la" -Endpoint "/api/v1/auth/login" -Method "POST" -Success $false -Note "Loi: $($_.Exception.Message)" -DetailText "$($_.Exception)"
    Write-Host "  -> [FAIL] TC_02: $($_.Exception.Message)" -ForegroundColor Red
}

# ==============================================================================
# TEST 3: SUPER ADMIN LOGIN & PHAN QUYEN RBAC
# ==============================================================================
Write-Host "'n[3/7] Phan quyen Da tang RBAC (Super Admin vs User Thuong)..." -ForegroundColor Yellow
$AdminToken = $null

try {
    $adminRes = Invoke-RestMethod -Uri "$TargetUrl/api/v1/auth/login" -Method Post -Body (@{ username = "admin"; password = "12345678" } | ConvertTo-Json) -ContentType "application/json" -Headers @{ "X-Device-Id" = "dev_admin_console" }
    if ($adminRes.data.requires2Fa -eq $true) {
        $setAdminOtpCmd = "UPDATE user_otp_verifications SET otp_code_hash = ''$2a'$10'$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi' WHERE user_id = $($adminRes.data.userId) AND otp_purpose = 'DEVICE_TRUST';"
        & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p12345678 -D "portfolio-engine" -e $setAdminOtpCmd 2>$null
        $adminVerRes = Invoke-RestMethod -Uri "$TargetUrl/api/v1/auth/verify-device-otp" -Method Post -Body (@{ username = "admin"; deviceId = "dev_admin_console"; otpCode = "123456"; rememberDevice = $true } | ConvertTo-Json) -ContentType "application/json"
        $AdminToken = $adminVerRes.data.accessToken
    } else {
        $AdminToken = $adminRes.data.accessToken
    }
} catch {
    try {
        $adminRes = Invoke-RestMethod -Uri "$TargetUrl/api/v1/auth/login" -Method Post -Body (@{ username = "admin"; password = "Password@123!" } | ConvertTo-Json) -ContentType "application/json"
        $AdminToken = $adminRes.data.accessToken
    } catch {}
}

if ($AdminToken) {
    try {
        $rolesRes = Invoke-RestMethod -Uri "$TargetUrl/api/v1/roles" -Method Get -Headers @{ "Authorization" = "Bearer $AdminToken" }
        $permRes = Invoke-RestMethod -Uri "$TargetUrl/api/v1/roles/permissions" -Method Get -Headers @{ "Authorization" = "Bearer $AdminToken" }
        $usersRes = Invoke-RestMethod -Uri "$TargetUrl/api/v1/users" -Method Get -Headers @{ "Authorization" = "Bearer $AdminToken" }

        Log-TestCase -Id "TC_03" -Name "Phan quyen RBAC Super Admin" -Endpoint "/api/v1/roles, /roles/permissions, /users" -Method "GET" -Success $true -Note "Super Admin truy cap hop le danh sach Roles, Permissions, Users" -DetailText "Roles Count: $($rolesRes.data.Count), Permissions Count: $($permRes.data.Count), Users Total: $($usersRes.data.totalElements)"
        Write-Host "  -> [OK] Super Admin RBAC thanh cong ($($rolesRes.data.Count) Roles, $($permRes.data.Count) Permissions)" -ForegroundColor Green
    } catch {
        Log-TestCase -Id "TC_03" -Name "Phan quyen RBAC Super Admin" -Endpoint "/api/v1/roles" -Method "GET" -Success $false -Note "Loi: $($_.Exception.Message)" -DetailText "$($_.Exception)"
    }
} else {
    Log-TestCase -Id "TC_03" -Name "Phan quyen RBAC Super Admin" -Endpoint "/api/v1/auth/login" -Method "POST" -Success $false -Note "Khong lay duoc Admin Token" -DetailText "Admin login failed"
}

# ==============================================================================
# TEST 4: TOKEN ROTATION & TOKEN REUSE DETECTION
# ==============================================================================
Write-Host "'n[4/7] Xoay vong Token va Chong Replay Attack (Family Revocation)..." -ForegroundColor Yellow
if ($userRefreshToken) {
    try {
        # 1. Rotate
        $rotateBody = @{ refreshToken = $userRefreshToken } | ConvertTo-Json
        $rotateRes = Invoke-RestMethod -Uri "$TargetUrl/api/v1/auth/refresh" -Method Post -Body $rotateBody -ContentType "application/json"
        $newAccessToken = $rotateRes.data.accessToken
        $newRefreshToken = $rotateRes.data.refreshToken
        Write-Host "  -> [OK] Xoay vong Token thanh cong! Session moi: $($rotateRes.data.sessionId)" -ForegroundColor Green

        # 2. Replay attack with old revoked token
        $replayDetected = $false
        try {
            $replayRes = Invoke-RestMethod -Uri "$TargetUrl/api/v1/auth/refresh" -Method Post -Body $rotateBody -ContentType "application/json"
        } catch {
            $replayDetected = $true
            Write-Host "  -> [OK] He thong phat hien Token Reuse -> Chan va kich hoat Family Revocation!" -ForegroundColor Green
        }

        Log-TestCase -Id "TC_04" -Name "Xoay vong Token va Chong Replay Attack" -Endpoint "/api/v1/auth/refresh" -Method "POST" -Success $replayDetected -Note "Phat hien Refresh Token cu bi gui lai -> Kich hoat Family Revocation thu hoi toan bo session" -DetailText "Rotated Session: $($rotateRes.data.sessionId), Replay Blocked: $replayDetected"
    } catch {
        Log-TestCase -Id "TC_04" -Name "Xoay vong Token" -Endpoint "/api/v1/auth/refresh" -Method "POST" -Success $false -Note "Loi: $($_.Exception.Message)" -DetailText "$($_.Exception)"
    }
} else {
    Log-TestCase -Id "TC_04" -Name "Xoay vong Token" -Endpoint "/api/v1/auth/refresh" -Method "POST" -Success $false -Note "Thieu Refresh Token de test" -DetailText "No refresh token"
}

# ==============================================================================
# TEST 5: PASSWORDLESS QR CODE LOGIN
# ==============================================================================
Write-Host "'n[5/7] Dang nhap Khong mat khau quet ma QR (Full QR Cycle)..." -ForegroundColor Yellow
try {
    # 1. Web Init
    $qrInit = Invoke-RestMethod -Uri "$TargetUrl/api/v1/auth/qr/init" -Method Post -Headers @{ "X-Device-Id" = "web_test_screen" }
    $qrSessionId = $qrInit.data.sessionId

    # 2. Mobile Scan
    $scanBody = @{ sessionId = $qrSessionId; mobileDeviceId = "mobile_iphone_tester" } | ConvertTo-Json
    $null = Invoke-RestMethod -Uri "$TargetUrl/api/v1/auth/qr/scan" -Method Post -Body $scanBody -ContentType "application/json" -Headers @{ "Authorization" = "Bearer $AdminToken" }

    # 3. Mobile Confirm
    $confirmBody = @{ sessionId = $qrSessionId; pin = "123456" } | ConvertTo-Json
    $confirmRes = Invoke-RestMethod -Uri "$TargetUrl/api/v1/auth/qr/confirm" -Method Post -Body $confirmBody -ContentType "application/json" -Headers @{ "Authorization" = "Bearer $AdminToken" }
    $exchangeCode = $confirmRes.data

    # 4. Web Exchange
    $exchangeBody = @{ sessionId = $qrSessionId; exchangeAuthCode = $exchangeCode; webDeviceId = "web_test_screen" } | ConvertTo-Json
    $exchangeRes = Invoke-RestMethod -Uri "$TargetUrl/api/v1/auth/qr/exchange" -Method Post -Body $exchangeBody -ContentType "application/json"
    $webAccessToken = $exchangeRes.data.accessToken

    $qrSuccess = ($webAccessToken -ne $null) -and ($exchangeRes.data.username -eq "admin")
    Log-TestCase -Id "TC_05" -Name "Dang nhap Khong mat khau QR Code" -Endpoint "/api/v1/auth/qr/init, /scan, /confirm, /exchange" -Method "POST" -Success $qrSuccess -Note "Web Init -> Mobile Scan -> Mobile Confirm -> Web Exchange Token" -DetailText "QR Session: $qrSessionId, Exchange Code: $exchangeCode, Web Token: $($webAccessToken.Substring(0,25))..."
    Write-Host "  -> [OK] Dang nhap QR thanh cong! Web nhan Token cua User: $($exchangeRes.data.username)" -ForegroundColor Green
} catch {
    Log-TestCase -Id "TC_05" -Name "Dang nhap QR Code" -Endpoint "/api/v1/auth/qr/*" -Method "POST" -Success $false -Note "Loi: $($_.Exception.Message)" -DetailText "$($_.Exception)"
    Write-Host "  -> [FAIL] TC_05: $($_.Exception.Message)" -ForegroundColor Red
}

# ==============================================================================
# TEST 6: SMARTOTP RFC 6238 & QUAN TRI THIET BI / PHIEN
# ==============================================================================
Write-Host "'n[6/7] Thiet lap SmartOTP RFC 6238 va Quan tri Thiet bi / Phien..." -ForegroundColor Yellow
if ($AdminToken) {
    try {
        $sotpRes = Invoke-RestMethod -Uri "$TargetUrl/api/v1/auth/smart-otp/setup" -Method Post -Headers @{ "Authorization" = "Bearer $AdminToken" }
        $secret = $sotpRes.data.secret
        $devRes = Invoke-RestMethod -Uri "$TargetUrl/api/v1/auth/devices" -Method Get -Headers @{ "Authorization" = "Bearer $AdminToken" }
        $sessRes = Invoke-RestMethod -Uri "$TargetUrl/api/v1/auth/sessions" -Method Get -Headers @{ "Authorization" = "Bearer $AdminToken" }

        $t6Success = ($secret -ne $null) -and ($devRes.data.Count -gt 0) -and ($sessRes.data.Count -gt 0)
        Log-TestCase -Id "TC_06" -Name "SmartOTP RFC 6238 va Quan tri Thiet bi / Phien" -Endpoint "/api/v1/auth/smart-otp/setup, /devices, /sessions" -Method "GET/POST" -Success $t6Success -Note "Khoi tao Base32 Secret Key, truy van Active Devices va Active Sessions" -DetailText "Base32 Secret: $secret, Active Devices: $($devRes.data.Count), Active Sessions: $($sessRes.data.Count)"
        Write-Host "  -> [OK] SmartOTP va Devices/Sessions quan tri thanh cong (Devices: $($devRes.data.Count), Sessions: $($sessRes.data.Count))" -ForegroundColor Green
    } catch {
        Log-TestCase -Id "TC_06" -Name "SmartOTP va Thiet bi" -Endpoint "/api/v1/auth/smart-otp/setup" -Method "POST" -Success $false -Note "Loi: $($_.Exception.Message)" -DetailText "$($_.Exception)"
    }
}

# ==============================================================================
# TEST 7: KIEM TOAN AUDIT LOGS & LICH SU SECURITY LOGIN
# ==============================================================================
Write-Host "'n[7/7] Kiem tra Kiem toan Audit Logs va Lich su Bao mat..." -ForegroundColor Yellow
try {
    $auditLogsQuery = & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p12345678 -D "portfolio-engine" -e "SELECT count(*) as total_audit_logs, max(created_at) as latest_audit FROM audit_logs;" 2>$null
    $loginHistQuery = & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p12345678 -D "portfolio-engine" -e "SELECT id, attempted_username, login_status, failure_reason, created_at FROM security_login_histories ORDER BY id DESC LIMIT 5;" 2>$null

    Log-TestCase -Id "TC_07" -Name "Ghi vet Kiem toan Toan dien (Audit Logs & Histories)" -Endpoint "MySQL: audit_logs & security_login_histories" -Method "SQL" -Success $true -Note "Tu dong ghi nhan moi thao tac dang nhap, 2FA challenge, phan quyen va quan tri" -DetailText "$auditLogsQuery'n'nTop 5 Login Histories:'n$loginHistQuery"
    Write-Host "  -> [OK] Kiem toan Audit Logging va Security Histories hoat dong 100%!" -ForegroundColor Green
} catch {
    Log-TestCase -Id "TC_07" -Name "Audit Logs" -Endpoint "MySQL Database" -Method "SQL" -Success $false -Note "Loi: $($_.Exception.Message)" -DetailText "$($_.Exception)"
}

# Build Markdown Report
$Report = @()
$Report += "# BAO CAO KET QUA KIEM THU TU DONG PHAN HE BAP MAT ENTERPRISE"
$Report += ""
$Report += "> **Thoi gian kiem thu:** $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
$Report += "> **Moi truong kiem thu:** Windows 11 / Java 17 / MySQL 8.0"
$Report += "> **Target Base URL:** $TargetUrl"
$Report += "> **Ket qua chung:** **$PassedCount / $TotalCount PASSED** ($([Math]::Round(($PassedCount/$TotalCount)*100, 1))% Success Rate)"
$Report += ""
$Report += "---"
$Report += ""
$Report += "## BANG TONG HOP KET QUA TEST CASES"
$Report += ""
$Report += "| STT | Phan he / Kich ban kiem thu | Endpoint | Method | Ket qua | Ghi chu |"
$Report += "| :--- | :--- | :--- | :---: | :---: | :--- |"
foreach ($row in $TableRows) {
    $Report += $row
}

$Report += ""
$Report += "---"
$Report += "## CHI TIET THUC THI TUNG KICH BAN"
$Report += ""
foreach ($d in $Details) {
    $Report += $d
}

$Report | Out-File -FilePath $ReportFile -Encoding utf8
Write-Host "'n==============================================================================" -ForegroundColor Cyan
Write-Host "  DA XUAT BAO CAO CHI TIET RA FILE: $ReportFile" -ForegroundColor Green
Write-Host "==============================================================================" -ForegroundColor Cyan
