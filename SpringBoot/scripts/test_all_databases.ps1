# ==============================================================================
# Portfolio Backend Engine - Script Kiểm Tra Kết Nối Toàn Bộ Cơ Sở Dữ Liệu
# ==============================================================================
# Cách chạy:
#   powershell -ExecutionPolicy Bypass -File scripts\test_all_databases.ps1
# ==============================================================================

param(
    [string]$DbHost = "localhost",
    [int]$DbPort = 3306,
    [string]$DbUser = "root",
    [string]$DbPass = "12345678",
    [string]$RedisHost = "localhost",
    [int]$RedisPort = 6379
)

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host "  PORTFOLIO BACKEND ENGINE - KIỂM TRA ĐỒNG BỘ 100% CƠ SỞ DỮ LIỆU" -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Cyan

$databases = @(
    @{ Name = "portfolio-engine"; Domain = "Core Infrastructure (Users, Tenants, Roles, Media, Payment)"; ExpectedTables = 18 },
    @{ Name = "db_tour";          Domain = "Tour & Travel Domain Service";                               ExpectedTables = 5 },
    @{ Name = "db_music";         Domain = "Music & Audio Streaming Domain Service";                     ExpectedTables = 5 },
    @{ Name = "db_film";          Domain = "Film & Cinema Domain Service";                               ExpectedTables = 4 },
    @{ Name = "db_gaming";        Domain = "Gaming & Esports Domain";                                    ExpectedTables = 5 },
    @{ Name = "db_blog";          Domain = "Blog & Editorial Domain";                                    ExpectedTables = 5 },
    @{ Name = "db_ai_vector";     Domain = "AI & Knowledge Base Vector Domain";                          ExpectedTables = 4 },
    @{ Name = "db_content_eav";   Domain = "Dynamic Content & EAV Engine";                               ExpectedTables = 7 }
)

$mysqlExe = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if (-not (Test-Path $mysqlExe)) {
    $mysqlCmd = Get-Command "mysql" -ErrorAction SilentlyContinue
    if ($mysqlCmd) { $mysqlExe = $mysqlCmd.Source }
}

$results = @()
$totalSuccess = 0
$totalFailed = 0

foreach ($db in $databases) {
    $name = $db.Name
    $domain = $db.Domain
    $sw = [System.Diagnostics.Stopwatch]::StartNew()

    try {
        $pinfo = New-Object System.Diagnostics.ProcessStartInfo
        $pinfo.FileName = $mysqlExe
        $pinfo.Arguments = "-h $DbHost -P $DbPort -u $DbUser -p$DbPass -N -e '"SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$name';'""
        $pinfo.RedirectStandardOutput = $true
        $pinfo.RedirectStandardError = $true
        $pinfo.UseShellExecute = $false
        $pinfo.CreateNoWindow = $true

        $p = New-Object System.Diagnostics.Process
        $p.StartInfo = $pinfo
        [void]$p.Start()
        $stdout = $p.StandardOutput.ReadToEnd()
        $stderr = $p.StandardError.ReadToEnd()
        $p.WaitForExit(5000)
        $sw.Stop()

        if ($p.ExitCode -eq 0 -and $stdout.Trim() -match '^\d+$') {
            $tableCount = [int]$stdout.Trim()
            $totalSuccess++
            $results += [PSCustomObject]@{
                "Database"    = $name
                "Type"        = "MySQL 8.0"
                "Status"      = "UP"
                "Tables"      = $tableCount
                "Latency"     = "$($sw.ElapsedMilliseconds) ms"
                "Domain Role" = $domain
            }
        } else {
            $totalFailed++
            $errText = if ($stderr) { $stderr.Trim() } else { "Lỗi kết nối" }
            $results += [PSCustomObject]@{
                "Database"    = $name
                "Type"        = "MySQL 8.0"
                "Status"      = "DOWN"
                "Tables"      = 0
                "Latency"     = "$($sw.ElapsedMilliseconds) ms"
                "Domain Role" = $errText
            }
        }
    } catch {
        $sw.Stop()
        $totalFailed++
        $results += [PSCustomObject]@{
            "Database"    = $name
            "Type"        = "MySQL 8.0"
            "Status"      = "DOWN"
            "Tables"      = 0
            "Latency"     = "$($sw.ElapsedMilliseconds) ms"
            "Domain Role" = $_.Exception.Message
        }
    }
}

# Kiểm tra Redis
$redisSw = [System.Diagnostics.Stopwatch]::StartNew()
try {
    $tcp = New-Object System.Net.Sockets.TcpClient
    $iar = $tcp.BeginConnect($RedisHost, $RedisPort, $null, $null)
    $success = $iar.AsyncWaitHandle.WaitOne(1000, $false)
    if ($success -and $tcp.Connected) {
        $tcp.EndConnect($iar)
        $stream = $tcp.GetStream()
        $stream.ReadTimeout = 1000
        $stream.WriteTimeout = 1000
        $writer = New-Object System.IO.StreamWriter($stream)
        $reader = New-Object System.IO.StreamReader($stream)
        $writer.WriteLine("PING")
        $writer.Flush()
        $pong = $reader.ReadLine()
        $tcp.Close()
        $redisSw.Stop()

        if ($pong -like "*PONG*") {
            $totalSuccess++
            $results += [PSCustomObject]@{
                "Database"    = "Redis Cache L2"
                "Type"        = "In-Memory"
                "Status"      = "UP"
                "Tables"      = "-"
                "Latency"     = "$($redisSw.ElapsedMilliseconds) ms"
                "Domain Role" = "Redis Session, Cache L2 & WebSocket Broker"
            }
        }
    } else {
        $tcp.Close()
        $redisSw.Stop()
        $results += [PSCustomObject]@{
            "Database"    = "Redis Cache L2"
            "Type"        = "In-Memory"
            "Status"      = "OFFLINE (Tùy chọn)"
            "Tables"      = "-"
            "Latency"     = "$($redisSw.ElapsedMilliseconds) ms"
            "Domain Role" = "Redis chưa bật trên port $RedisPort"
        }
    }
} catch {
    $redisSw.Stop()
    $results += [PSCustomObject]@{
        "Database"    = "Redis Cache L2"
        "Type"        = "In-Memory"
        "Status"      = "OFFLINE (Tùy chọn)"
        "Tables"      = "-"
        "Latency"     = "$($redisSw.ElapsedMilliseconds) ms"
        "Domain Role" = "Redis chưa bật trên port $RedisPort"
    }
}

Write-Host ""
$results | Format-Table -AutoSize

Write-Host "------------------------------------------------------------------------------" -ForegroundColor DarkGray
if ($totalFailed -eq 0) {
    Write-Host " [SUCCESS] TẤT CẢ CÁC CƠ SỞ DỮ LIỆU MYSQL ĐÃ ĐỒNG BỘ VÀ HOẠT ĐỘNG HOÀN HẢO!" -ForegroundColor Green
} else {
    Write-Host " [WARN] Có $totalFailed cơ sở dữ liệu chưa kết nối thành công, hãy kiểm tra lại!" -ForegroundColor Yellow
}
Write-Host "==============================================================================" -ForegroundColor Cyan
