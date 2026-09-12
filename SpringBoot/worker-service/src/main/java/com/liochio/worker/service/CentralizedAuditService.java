package com.liochio.worker.service;

import com.liochio.worker.dto.AuditTraceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * ==============================================================================
 * Dịch Vụ Tra Soát & Kiểm Toán Tập Trung Toàn Hệ Thống (Centralized Audit Service)
 * ==============================================================================
 * 
 * Mục đích:
 * - Tập hợp, đối chiếu và truy vết toàn trình (End-to-End Distributed Trace).
 * - Kết nối đồng bộ đa cơ sở dữ liệu: Core Spring Boot, Python AI/Flow, Sổ cái kế toán,
 *   Nhật ký Thư tín, Trung tâm Thông báo và Dead Letter Queue.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CentralizedAuditService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Tra cứu toàn trình lịch sử một Request theo mã Trace ID
     */
    public AuditTraceResponse getTraceDetail(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            return AuditTraceResponse.builder().traceId("EMPTY").overallStatus("NOT_FOUND").build();
        }

        String cleanTraceId = traceId.trim();

        // 1. Phân hệ Core Spring Boot (Audit Logs)
        List<Map<String, Object>> coreAuditLogs = queryList(
                "SELECT id, trace_id, tenant_id, user_id, username, client_ip, module, action_type, " +
                        "action_description, http_method, request_uri, http_status_code, status, " +
                        "error_message, execution_time_ms, created_at " +
                        "FROM liochio_core_db.audit_logs WHERE trace_id = ? ORDER BY id ASC",
                cleanTraceId
        );

        // 2. Phân hệ Python Subsystem (Flow Nodes & API Logs)
        List<Map<String, Object>> pythonFlowLogs = queryList(
                "SELECT id, trace_id, node, details, created_at, updated_at " +
                        "FROM liochio_app_db.request_flow_logs WHERE trace_id = ? ORDER BY created_at ASC",
                cleanTraceId
        );

        List<Map<String, Object>> pythonApiLogs = queryList(
                "SELECT id, user_id, endpoint, method, status_code, latency_ms, status, created_at " +
                        "FROM liochio_app_db.api_request_logs WHERE req_payload LIKE ? OR res_payload LIKE ? ORDER BY created_at ASC",
                "%" + cleanTraceId + "%", "%" + cleanTraceId + "%"
        );

        // 3. Phân hệ Tài chính & Sổ cái kép (Core Banking Journal Entries)
        List<Map<String, Object>> ledgerLogs = queryList(
                "SELECT id, reference_id, reference_type, entry_date, description, status, created_at " +
                        "FROM liochio_app_db.journal_entries WHERE reference_id = ? OR description LIKE ? ORDER BY created_at ASC",
                cleanTraceId, "%" + cleanTraceId + "%"
        );

        // 4. Phân hệ Thư tín & Thông báo (Mail Logs & Notifications)
        List<Map<String, Object>> mailLogs = queryList(
                "SELECT id, trace_id, recipient, channel, template_code, subject, status, " +
                        "error_message, retry_count, execution_time_ms, created_at " +
                        "FROM liochio_app_db.mail_logs WHERE trace_id = ? ORDER BY id ASC",
                cleanTraceId
        );

        List<Map<String, Object>> notificationLogs = queryList(
                "SELECT id, tenant_id, recipient, channel, subject, content, status, error_message, created_at " +
                        "FROM liochio_app_db.notifications WHERE subject LIKE ? OR content LIKE ? ORDER BY id ASC",
                "%" + cleanTraceId + "%", "%" + cleanTraceId + "%"
        );

        // 5. Phân hệ Lỗi & Dead Letter Queue (DLQ & System Error Logs)
        List<Map<String, Object>> dlqErrors = queryList(
                "SELECT id, event_id, topic, trace_id, payload, retry_count, error_reason, status, created_at " +
                        "FROM liochio_app_db.dlq_messages WHERE trace_id = ? ORDER BY id ASC",
                cleanTraceId
        );

        List<Map<String, Object>> systemErrors = queryList(
                "SELECT id, component, error_type, stack_trace, created_at " +
                        "FROM liochio_app_db.system_logs WHERE stack_trace LIKE ? ORDER BY created_at ASC",
                "%" + cleanTraceId + "%"
        );

        // 6. Tổng hợp dữ liệu & Trạng thái toàn trình
        boolean hasFailure = !dlqErrors.isEmpty() || !systemErrors.isEmpty()
                || coreAuditLogs.stream().anyMatch(a -> "FAILED".equalsIgnoreCase(String.valueOf(a.get("status"))))
                || mailLogs.stream().anyMatch(m -> "FAILED".equalsIgnoreCase(String.valueOf(m.get("status"))));

        String overallStatus = hasFailure ? "FAILED" : "SUCCESS";
        if (coreAuditLogs.isEmpty() && pythonFlowLogs.isEmpty() && mailLogs.isEmpty()) {
            overallStatus = "NOT_FOUND";
        }

        String tenantId = "default";
        if (!coreAuditLogs.isEmpty() && coreAuditLogs.get(0).get("tenant_id") != null) {
            tenantId = String.valueOf(coreAuditLogs.get(0).get("tenant_id"));
        }

        long totalDurationMs = 0L;
        for (Map<String, Object> logRow : coreAuditLogs) {
            if (logRow.get("execution_time_ms") instanceof Number n) {
                totalDurationMs += n.longValue();
            }
        }
        for (Map<String, Object> mailRow : mailLogs) {
            if (mailRow.get("execution_time_ms") instanceof Number n) {
                totalDurationMs += n.longValue();
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCoreAuditEvents", coreAuditLogs.size());
        summary.put("totalPythonFlowNodes", pythonFlowLogs.size());
        summary.put("totalLedgerEntries", ledgerLogs.size());
        summary.put("totalMailDispatches", mailLogs.size());
        summary.put("totalNotifications", notificationLogs.size());
        summary.put("totalErrorsRecorded", dlqErrors.size() + systemErrors.size());
        summary.put("isSynchronizedAcrossSubsystems", !coreAuditLogs.isEmpty() || !pythonFlowLogs.isEmpty());

        return AuditTraceResponse.builder()
                .traceId(cleanTraceId)
                .tenantId(tenantId)
                .overallStatus(overallStatus)
                .totalDurationMs(totalDurationMs)
                .firstSeenAt(Instant.now())
                .lastSeenAt(Instant.now())
                .summary(summary)
                .coreAuditLogs(coreAuditLogs)
                .pythonFlowLogs(pythonFlowLogs)
                .pythonApiLogs(pythonApiLogs)
                .financialLedgerLogs(ledgerLogs)
                .mailLogs(mailLogs)
                .notificationLogs(notificationLogs)
                .dlqErrors(dlqErrors)
                .systemErrors(systemErrors)
                .build();
    }

    /**
     * Báo cáo tổng quan số liệu log và kiểm toán toàn hệ thống
     */
    public Map<String, Object> getAuditSummaryMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();

        try {
            Long totalAudit = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM liochio_core_db.audit_logs", Long.class);
            Long totalFailedAudit = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM liochio_core_db.audit_logs WHERE status = 'FAILED'", Long.class);
            Long totalMailSent = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM liochio_app_db.mail_logs WHERE status = 'SENT'", Long.class);
            Long totalMailPending = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM liochio_app_db.mail_logs WHERE status = 'PENDING'", Long.class);
            Long totalDlq = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM liochio_app_db.dlq_messages WHERE status = 'PENDING_RETRY'", Long.class);
            Long totalFlowNodes = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM liochio_app_db.request_flow_logs", Long.class);
            Long totalLedger = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM liochio_app_db.journal_entries", Long.class);

            metrics.put("status", "HEALTHY");
            metrics.put("totalCoreAuditLogs", totalAudit != null ? totalAudit : 0L);
            metrics.put("totalFailedRequests", totalFailedAudit != null ? totalFailedAudit : 0L);
            metrics.put("totalMailsDelivered", totalMailSent != null ? totalMailSent : 0L);
            metrics.put("totalMailsQueued", totalMailPending != null ? totalMailPending : 0L);
            metrics.put("totalDeadLetterQueueErrors", totalDlq != null ? totalDlq : 0L);
            metrics.put("totalPythonFlowNodes", totalFlowNodes != null ? totalFlowNodes : 0L);
            metrics.put("totalFinancialLedgerJournals", totalLedger != null ? totalLedger : 0L);
            metrics.put("crossSystemSyncStatus", "ACTIVE");
            metrics.put("timestamp", Instant.now().toString());

        } catch (Exception e) {
            log.warn("[CentralizedAuditService] Lỗi tính toán summary metrics: {}", e.getMessage());
            metrics.put("status", "PARTIAL");
            metrics.put("error", e.getMessage());
        }

        return metrics;
    }

    /**
     * Danh sách các bản ghi lỗi gần nhất để tra cứu sự cố tức thì
     */
    public List<Map<String, Object>> getRecentSystemErrors(int limit) {
        int safeLimit = Math.min(Math.max(limit, 5), 100);
        return queryList(
                "SELECT 'CORE_AUDIT_ERROR' as source, id, trace_id, module, action_type, http_method, " +
                        "request_uri, http_status_code, error_message, created_at " +
                        "FROM liochio_core_db.audit_logs WHERE status = 'FAILED' OR http_status_code >= 400 " +
                        "ORDER BY id DESC LIMIT ?",
                safeLimit
        );
    }

    private List<Map<String, Object>> queryList(String sql, Object... args) {
        try {
            return jdbcTemplate.queryForList(sql, args);
        } catch (Exception e) {
            log.debug("[CentralizedAuditService] Query '{}': {}", sql, e.getMessage());
            return Collections.emptyList();
        }
    }
}
