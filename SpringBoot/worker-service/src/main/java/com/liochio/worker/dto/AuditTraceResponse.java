package com.liochio.worker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * ==============================================================================
 * DTO Kết Quả Tra Soát Phân Tán Toàn Diện (End-to-End Audit & Trace Response)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditTraceResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String traceId;
    private String tenantId;
    private String overallStatus; // SUCCESS | FAILED | WARNING
    private Long totalDurationMs;
    private Instant firstSeenAt;
    private Instant lastSeenAt;

    private Map<String, Object> summary;

    // 1. Phân hệ Core Spring Boot (Audit Logs - Request/Response & Thao tác nghiệp vụ)
    private List<Map<String, Object>> coreAuditLogs;

    // 2. Phân hệ Python Subsystem (Flow Nodes & API Logs)
    private List<Map<String, Object>> pythonFlowLogs;
    private List<Map<String, Object>> pythonApiLogs;

    // 3. Phân hệ Tài chính & Sổ cái kép (Core Banking Journal Entries)
    private List<Map<String, Object>> financialLedgerLogs;

    // 4. Phân hệ Thư tín & Thông báo (Mail Logs & Notifications)
    private List<Map<String, Object>> mailLogs;
    private List<Map<String, Object>> notificationLogs;

    // 5. Phân hệ Lỗi & Dead Letter Queue (System Errors & DLQ Messages)
    private List<Map<String, Object>> dlqErrors;
    private List<Map<String, Object>> systemErrors;
}
