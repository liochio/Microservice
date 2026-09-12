package com.liochio.worker.service;

import com.liochio.worker.dto.HousekeepingReportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class HousekeepingService {

    private final JdbcTemplate jdbcTemplate;

    public HousekeepingReportResponse runCleanup() {
        log.info("[HousekeepingService] Khởi động tác vụ dọn dẹp dữ liệu rác định kỳ...");
        Instant now = Instant.now();

        int expiredOtps = 0;
        int expiredKeys = 0;
        int oldDlq = 0;

        try {
            // 1. Xóa OTP hết hạn quá hạn
            expiredOtps = jdbcTemplate.update("DELETE FROM liochio_core_db.otps WHERE expires_at < NOW() AND status != 'PENDING'");
        } catch (Exception e) {
            log.debug("[HousekeepingService] Dọn OTP bỏ qua: {}", e.getMessage());
        }

        try {
            // 2. Xóa Idempotency Keys cũ hơn 24h
            expiredKeys = jdbcTemplate.update("DELETE FROM liochio_core_db.idempotency_keys WHERE expires_at < NOW()");
        } catch (Exception e) {
            log.debug("[HousekeepingService] Dọn Idempotency bỏ qua: {}", e.getMessage());
        }

        try {
            // 3. Xóa các DLQ đã xử lý xong
            oldDlq = jdbcTemplate.update("DELETE FROM liochio_app_db.dlq_messages WHERE status = 'RESOLVED'");
        } catch (Exception e) {
            log.debug("[HousekeepingService] Dọn DLQ bỏ qua: {}", e.getMessage());
        }

        log.info("[HousekeepingService] ✅ Dọn dẹp hoàn tất: {} OTPs, {} Idempotency Keys, {} DLQs",
                expiredOtps, expiredKeys, oldDlq);

        return HousekeepingReportResponse.builder()
                .executionTimestamp(now)
                .expiredOtpsDeleted(expiredOtps)
                .staleSessionsRevoked(0)
                .expiredIdempotencyKeysDeleted(expiredKeys)
                .oldDlqCleaned(oldDlq)
                .status("SUCCESS")
                .build();
    }
}
