package com.liochio.entityservice.controller;

import com.liochio.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * ==============================================================================
 * Controller Kiểm Tra Kết Nối Toàn Bộ Cơ Sở Dữ Liệu (All Database Health API)
 * ==============================================================================
 * 
 * Endpoint:
 * - GET /api/v1/system/databases/health
 * - GET /api/system/databases/health
 * 
 * Mục đích:
 * - Kiểm tra kết nối trực tiếp đến 100% các Database chuyên biệt trong hệ sinh thái
 * - Đo độ trễ (latency ms) và đếm số lượng bảng của từng Database
 * - Kiểm tra kết nối Redis In-Memory L2
 */
@Slf4j
@RestController
@RequestMapping({"/api/v1/system/databases", "/api/system/databases"})
@Tag(name = "System Database Health API", description = "Kiểm tra tình trạng kết nối 100% Database trong hệ thống")
public class DatabaseHealthController {

    @Value("${DB_HOST:${MYSQL_HOST:localhost}}")
    private String dbHost;

    @Value("${DB_PORT:${MYSQL_PORT:3306}}")
    private String dbPort;

    @Value("${DB_USER:${MYSQL_USER:root}}")
    private String dbUser;

    @Value("${DB_PASSWORD:${MYSQL_PASSWORD:12345678}}")
    private String dbPassword;

    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;

    @Data
    @Builder
    public static class DatabaseStatus {
        private String name;
        private String domain;
        private String type;
        private String status;
        private long latencyMs;
        private Integer tableCount;
        private String details;
    }

    @Data
    @Builder
    public static class OverallHealthReport {
        private String overallStatus;
        private int totalDatabasesChecked;
        private int successfulDatabases;
        private int failedDatabases;
        private List<DatabaseStatus> databases;
        private Instant timestamp;
    }

    @GetMapping("/health")
    @Operation(summary = "Kiểm tra kết nối toàn bộ Database", description = "Ping trực tiếp và đo độ trễ tới 8 Database MySQL + Redis")
    public ApiResponse<OverallHealthReport> checkAllDatabasesHealth() {
        List<DatabaseStatus> reportList = new ArrayList<>();

        // Danh sách các Database miền dịch vụ FinTech cần kiểm tra
        List<DatabaseTarget> targets = List.of(
                new DatabaseTarget("liochio_core_db", "Core Identity & Access Management (IAM)", "MySQL 8.0"),
                new DatabaseTarget("liochio_ledger_db", "Core Banking Double-Entry Ledger Service", "MySQL 8.0"),
                new DatabaseTarget("liochio_entity_db", "Dynamic Menus, Config Matrix & Entity Service", "MySQL 8.0"),
                new DatabaseTarget("liochio_payment_db", "Payment Gateway & Transaction Processing", "MySQL 8.0"),
                new DatabaseTarget("liochio_notification_db", "Multi-Channel Notification Service", "MySQL 8.0"),
                new DatabaseTarget("liochio_app_db", "Python IoT Piggy Bank & Wallets Satellite", "MySQL 8.0")
        );

        int successCount = 0;
        int failedCount = 0;

        // 1. Kiểm tra từng MySQL Database
        for (DatabaseTarget target : targets) {
            DatabaseStatus status = testMySQLDatabase(target.getName(), target.getDomain(), target.getType());
            if ("UP".equals(status.getStatus())) {
                successCount++;
            } else {
                failedCount++;
            }
            reportList.add(status);
        }

        // 2. Kiểm tra Redis
        DatabaseStatus redisStatus = testRedis();
        if ("UP".equals(redisStatus.getStatus())) {
            successCount++;
        } else {
            failedCount++;
        }
        reportList.add(redisStatus);

        String overallStatus = (failedCount == 0) ? "HEALTHY (100% ONLINE)" : "DEGRADED (" + failedCount + " OFFLINE)";

        OverallHealthReport report = OverallHealthReport.builder()
                .overallStatus(overallStatus)
                .totalDatabasesChecked(reportList.size())
                .successfulDatabases(successCount)
                .failedDatabases(failedCount)
                .databases(reportList)
                .timestamp(Instant.now())
                .build();

        return ApiResponse.success(report, "Kiểm tra kết nối toàn bộ cơ sở dữ liệu hoàn tất");
    }

    private DatabaseStatus testMySQLDatabase(String dbName, String domain, String type) {
        String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=3000",
                dbHost, dbPort, dbName);

        long start = System.currentTimeMillis();
        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             Statement stmt = conn.createStatement()) {

            // Test query
            stmt.executeQuery("SELECT 1");

            // Count tables
            int tableCount = 0;
            try (ResultSet rs = stmt.executeQuery(
                    String.format("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '%s'", dbName))) {
                if (rs.next()) {
                    tableCount = rs.getInt(1);
                }
            }

            long latency = System.currentTimeMillis() - start;
            return DatabaseStatus.builder()
                    .name(dbName)
                    .domain(domain)
                    .type(type)
                    .status("UP")
                    .latencyMs(latency)
                    .tableCount(tableCount)
                    .details("Kết nối thành công. Tổng số bảng: " + tableCount)
                    .build();

        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            log.error("[DatabaseHealth] Không thể kết nối tới DB '{}': {}", dbName, e.getMessage());
            return DatabaseStatus.builder()
                    .name(dbName)
                    .domain(domain)
                    .type(type)
                    .status("DOWN")
                    .latencyMs(latency)
                    .tableCount(0)
                    .details("Lỗi kết nối: " + e.getMessage())
                    .build();
        }
    }

    private DatabaseStatus testRedis() {
        long start = System.currentTimeMillis();
        if (redisConnectionFactory == null) {
            return DatabaseStatus.builder()
                    .name("redis")
                    .domain("In-Memory Cache L2 & WebSocket Hub")
                    .type("Redis 7.x")
                    .status("UNKNOWN")
                    .latencyMs(0)
                    .details("Redis connection factory chưa được cấu hình")
                    .build();
        }

        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            String pingResult = connection.ping();
            long latency = System.currentTimeMillis() - start;
            return DatabaseStatus.builder()
                    .name("redis")
                    .domain("In-Memory Cache L2 & WebSocket Hub")
                    .type("Redis 7.x")
                    .status("UP")
                    .latencyMs(latency)
                    .details("Ping Redis thành công: " + pingResult)
                    .build();
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            log.error("[DatabaseHealth] Không thể kết nối tới Redis: {}", e.getMessage());
            return DatabaseStatus.builder()
                    .name("redis")
                    .domain("In-Memory Cache L2 & WebSocket Hub")
                    .type("Redis 7.x")
                    .status("DOWN")
                    .latencyMs(latency)
                    .details("Lỗi kết nối Redis: " + e.getMessage())
                    .build();
        }
    }

    @Data
    private static class DatabaseTarget {
        private final String name;
        private final String domain;
        private final String type;
    }
}
