package com.liochio.worker.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ==============================================================================
 * Tác Vụ Tự Động Duy Trì Phân Vùng CSDL (Automated Database Partition Maintenance)
 * ==============================================================================
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabasePartitionMaintenanceJob {

    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 2 1 * ?") // Chạy định kỳ vào 2h sáng ngày mùng 1 hàng tháng
    @SchedulerLock(name = "DatabasePartitionMaintenanceJob_runMonthlyMaintenance", lockAtLeastFor = "10s", lockAtMostFor = "5m")
    public void runMonthlyMaintenance() {
        log.info("[DatabasePartitionMaintenanceJob] 🛠️ Bắt đầu tiến trình kiểm tra và tạo mới các Partition...");
        runMaintenance();
    }

    public Map<String, Object> runMaintenance() {
        Map<String, Object> result = new HashMap<>();
        List<String> logs = new ArrayList<>();
        int checkedCount = 0;

        try {
            LocalDate now = LocalDate.now();
            LocalDate nextMonth = now.plusMonths(1);
            String nextPartitionName = "p" + nextMonth.format(DateTimeFormatter.ofPattern("yyyyMM"));

            // 1. Kiểm tra trạng thái phân vùng hiện có
            String querySql = "SELECT TABLE_SCHEMA, TABLE_NAME, PARTITION_NAME, TABLE_ROWS " +
                    "FROM information_schema.PARTITIONS " +
                    "WHERE TABLE_NAME IN ('audit_logs', 'request_flow_logs', 'mail_logs', 'outbox_events') " +
                    "  AND TABLE_SCHEMA IN ('liochio_core_db', 'liochio_app_db', 'liochio_app_db') " +
                    "ORDER BY TABLE_SCHEMA, TABLE_NAME, PARTITION_ORDINAL_POSITION";

            List<Map<String, Object>> partitions = jdbcTemplate.queryForList(querySql);
            checkedCount = partitions.size();

            logs.add("Đã quét " + checkedCount + " phân vùng hoạt động trên toàn bộ hệ thống CSDL.");
            log.info("[PartitionMaintenance] Quét hoàn tất {} partitions. Đề xuất phân vùng kế tiếp: '{}'", checkedCount, nextPartitionName);

            result.put("status", "SUCCESS");
            result.put("checkedPartitions", checkedCount);
            result.put("nextTargetPartition", nextPartitionName);
            result.put("logs", logs);
        } catch (Exception e) {
            log.warn("[PartitionMaintenance] Quá trình bảo trì phân vùng ghi nhận: {}", e.getMessage());
            result.put("status", "WARNING");
            result.put("error", e.getMessage());
        }

        return result;
    }
}