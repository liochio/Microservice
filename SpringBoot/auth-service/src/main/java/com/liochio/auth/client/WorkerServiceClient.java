package com.liochio.auth.client;

import com.liochio.common.dto.ApiResponse;
import com.liochio.common.dto.worker.HousekeepingReportResponse;
import com.liochio.common.dto.worker.MailDispatchRequest;
import com.liochio.common.dto.worker.MailDispatchResponse;
import com.liochio.common.dto.worker.ReconciliationReportResponse;
import com.liochio.common.dto.worker.WorkerStatusResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "worker-service", url = "${worker-service.url:http://localhost:8095}")
public interface WorkerServiceClient {

    @GetMapping("/api/v1/worker/health")
    ApiResponse<Map<String, Object>> getHealth();

    @GetMapping("/api/v1/worker/status")
    ApiResponse<WorkerStatusResponse> getStatus();

    @PostMapping("/api/v1/worker/mail/dispatch")
    ApiResponse<MailDispatchResponse> dispatchMail(@RequestBody MailDispatchRequest request);

    @PostMapping("/api/v1/worker/outbox/trigger")
    ApiResponse<Map<String, Object>> triggerOutbox();

    @PostMapping("/api/v1/worker/reconciliation/trigger")
    ApiResponse<ReconciliationReportResponse> triggerReconciliation();

    @PostMapping("/api/v1/worker/cleanup/trigger")
    ApiResponse<HousekeepingReportResponse> triggerCleanup();
}
