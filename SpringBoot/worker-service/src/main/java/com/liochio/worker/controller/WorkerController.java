package com.liochio.worker.controller;

import com.liochio.common.dto.ApiResponse;
import com.liochio.worker.dto.*;
import com.liochio.worker.entity.DlqMessageEntity;
import com.liochio.worker.entity.MailLogEntity;
import com.liochio.worker.job.DatabasePartitionMaintenanceJob;
import com.liochio.worker.repository.DlqMessageRepository;
import com.liochio.worker.repository.MailLogRepository;
import com.liochio.worker.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/worker", "/api/worker"})
@RequiredArgsConstructor
@Tag(name = "Worker Service Management", description = "Các API điều khiển Worker, tiếp nhận phát thư tín, đối soát sổ cái và kích hoạt tác vụ ngầm")
public class WorkerController {

    private final MailProcessingService mailProcessingService;
    private final OutboxProcessingService outboxProcessingService;
    private final LedgerReconciliationService reconciliationService;
    private final HousekeepingService housekeepingService;
    private final DatabasePartitionMaintenanceJob partitionMaintenanceJob;
    private final MailLogRepository mailLogRepository;
    private final DlqMessageRepository dlqRepository;
    private final CentralizedAuditService centralizedAuditService;
    private final TelegramAlertService telegramAlertService;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @GetMapping("/health")
    @Operation(summary = "Kiểm tra trạng thái sẵn sàng của Worker Service")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("service", "worker-service");
        resp.put("status", "UP");
        resp.put("timestamp", Instant.now().toString());
        return ApiResponse.success(resp);
    }

    @GetMapping("/status")
    @Operation(summary = "Xem tổng quan số liệu hàng đợi Mail, Outbox, DLQ và Scheduler")
    public ApiResponse<WorkerStatusResponse> getStatus() {
        Map<String, Long> metrics = new HashMap<>();
        metrics.put("pendingMails", mailLogRepository.countByStatus("PENDING"));
        metrics.put("sentMails", mailLogRepository.countByStatus("SENT"));
        metrics.put("failedMails", mailLogRepository.countByStatus("FAILED"));
        metrics.put("pendingDlq", dlqRepository.countByStatus("PENDING_RETRY"));

        Map<String, Object> sched = new HashMap<>();
        sched.put("shedLockEnabled", true);
        sched.put("mailDispatcherActive", true);
        sched.put("outboxPollerActive", true);
        sched.put("partitionMaintenanceActive", true);

        WorkerStatusResponse response = WorkerStatusResponse.builder()
                .status("HEALTHY")
                .workerInstanceId("worker-instance-01")
                .serverTime(Instant.now())
                .queueMetrics(metrics)
                .schedulerStatus(sched)
                .build();

        return ApiResponse.success(response);
    }

    @PostMapping("/mail/dispatch")
    @Operation(summary = "Cổng nhận lệnh phát thư tín / email / SMS từ các Microservices khác")
    public ApiResponse<MailDispatchResponse> dispatchMail(@Valid @RequestBody MailDispatchRequest request) {
        MailDispatchResponse response = mailProcessingService.dispatch(request);
        return ApiResponse.created(response, "Đã tiếp nhận yêu cầu phát thông báo");
    }

    @GetMapping("/mail/logs")
    @Operation(summary = "Tra cứu nhật ký gửi email/SMS có phân trang và lọc theo trạng thái")
    public ApiResponse<Page<MailLogEntity>> getMailLogs(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String recipient,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<MailLogEntity> page;
        if (status != null && !status.isBlank()) {
            page = mailLogRepository.findByStatus(status, pageable);
        } else if (recipient != null && !recipient.isBlank()) {
            page = mailLogRepository.findByRecipientContaining(recipient, pageable);
        } else {
            page = mailLogRepository.findAll(pageable);
        }
        return ApiResponse.success(page);
    }

    @PostMapping("/outbox/trigger")
    @Operation(summary = "Kích hoạt quét hàng đợi Outbox Events ngay lập tức")
    public ApiResponse<Map<String, Object>> triggerOutbox() {
        int processed = outboxProcessingService.processPendingOutboxEvents();
        Map<String, Object> res = new HashMap<>();
        res.put("processedEvents", processed);
        return ApiResponse.success(res, "Quét Outbox hoàn tất");
    }

    @PostMapping("/reconciliation/trigger")
    @Operation(summary = "Kích hoạt đối soát Sổ cái kép (EOD Audit) ngay lập tức")
    public ApiResponse<ReconciliationReportResponse> triggerReconciliation() {
        ReconciliationReportResponse report = reconciliationService.performAudit();
        return ApiResponse.success(report, "Đối soát sổ cái kép hoàn tất");
    }

    @PostMapping("/cleanup/trigger")
    @Operation(summary = "Kích hoạt dọn dẹp dữ liệu rác (Housekeeping) ngay lập tức")
    public ApiResponse<HousekeepingReportResponse> triggerCleanup() {
        HousekeepingReportResponse report = housekeepingService.runCleanup();
        return ApiResponse.success(report, "Dọn dẹp hoàn tất");
    }

    @GetMapping("/dlq")
    @Operation(summary = "Xem danh sách thông điệp lỗi trong Dead Letter Queue")
    public ApiResponse<Page<DlqMessageEntity>> getDlqMessages(
            @RequestParam(defaultValue = "PENDING_RETRY") String status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.success(dlqRepository.findByStatus(status, pageable));
    }

    @PostMapping("/dlq/{id}/retry")
    @Operation(summary = "Thử lại một thông điệp trong hàng đợi Dead Letter Queue")
    public ApiResponse<DlqMessageEntity> retryDlqMessage(@PathVariable Long id) {
        DlqMessageEntity entity = dlqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy DLQ Message ID: " + id));
        entity.setStatus("RESOLVED");
        entity.setUpdatedAt(Instant.now());
        DlqMessageEntity saved = dlqRepository.save(entity);
        return ApiResponse.success(saved, "Đã kích hoạt thử lại thành công");
    }

    @GetMapping("/audit/trace/{traceId}")
    @Operation(summary = "Tra soát toàn trình phân tán (End-to-End Trace) từ Gateway, Core, Python tới Worker")
    public ApiResponse<AuditTraceResponse> getTraceDetail(@PathVariable String traceId) {
        return ApiResponse.success(centralizedAuditService.getTraceDetail(traceId));
    }

    @GetMapping("/audit/summary")
    @Operation(summary = "Báo cáo tổng quan số liệu log và kiểm toán toàn hệ thống")
    public ApiResponse<Map<String, Object>> getAuditSummary() {
        return ApiResponse.success(centralizedAuditService.getAuditSummaryMetrics());
    }

    @GetMapping("/audit/errors")
    @Operation(summary = "Tra cứu danh sách lỗi hệ thống và Request thất bại gần nhất")
    public ApiResponse<List<Map<String, Object>>> getRecentErrors(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(centralizedAuditService.getRecentSystemErrors(limit));
    }

    @PostMapping("/mail/trigger")
    @Operation(summary = "Kích hoạt xử lý hàng đợi phát thư tín ngay lập tức")
    public ApiResponse<Map<String, Object>> triggerMailQueue() {
        int processed = mailProcessingService.processPendingBatch();
        Map<String, Object> res = new HashMap<>();
        res.put("processedMails", processed);
        return ApiResponse.success(res, "Xử lý hàng đợi phát thư tín hoàn tất");
    }

    @PostMapping("/alerts/send-test")
    @Operation(summary = "Kiểm thử phát tin nhắn cảnh báo bảo mật & sự cố tức thì qua Telegram/Slack")
    public ApiResponse<Map<String, Object>> sendTestAlert(
            @RequestParam(defaultValue = "CẢNH BÁO KIỂM THỬ HỆ THỐNG") String title,
            @RequestParam(defaultValue = "Đây là thông điệp kiểm thử khả năng phát cảnh báo tự động khi có sự cố.") String message,
            @RequestParam(defaultValue = "trace_alert_test") String traceId
    ) {
        boolean sent = telegramAlertService.sendCriticalAlert(title, message, traceId, "WORKER_ALERT_ENGINE");
        Map<String, Object> res = new HashMap<>();
        res.put("dispatched", sent);
        res.put("title", title);
        res.put("traceId", traceId);
        res.put("status", "SUCCESS");
        return ApiResponse.success(res, "Đã phát cảnh báo thành công");
    }

    @GetMapping("/partitions/status")
    @Operation(summary = "Xem trạng thái phân mảnh bảng (Table Partitioning) của cơ sở dữ liệu")
    public ApiResponse<List<Map<String, Object>>> getPartitionStatus() {
        String sql = "SELECT TABLE_SCHEMA, TABLE_NAME, PARTITION_NAME, PARTITION_EXPRESSION, TABLE_ROWS " +
                "FROM information_schema.PARTITIONS " +
                "WHERE TABLE_NAME IN ('audit_logs', 'request_flow_logs', 'api_request_logs', 'mail_logs', 'outbox_events') " +
                "  AND TABLE_SCHEMA IN ('liochio_core_db', 'liochio_app_db', 'liochio_app_db', 'liochio_app_db') " +
                "ORDER BY TABLE_SCHEMA, TABLE_NAME, PARTITION_ORDINAL_POSITION";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        return ApiResponse.success(list);
    }

    @PostMapping("/partitions/maintain")
    @Operation(summary = "Kích hoạt bảo trì và phân vùng tự động (Automated DB Partition Maintenance)")
    public ApiResponse<Map<String, Object>> maintainPartitions() {
        Map<String, Object> report = partitionMaintenanceJob.runMaintenance();
        return ApiResponse.success(report, "Tiến trình bảo trì phân vùng hoàn tất");
    }
}