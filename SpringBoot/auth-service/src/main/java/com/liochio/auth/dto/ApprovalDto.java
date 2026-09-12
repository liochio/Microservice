package com.liochio.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalDto {
    private Long id;
    private String tenantId;
    private String requestCode;
    private String requestType;
    private String entityType;
    private String entityId;
    private String title;
    private Long makerUserId;
    private String makerUsername;
    private String makerNote;
    private String payloadBefore;
    private String payloadAfter;
    private Long checkerUserId;
    private String checkerUsername;
    private String checkerNote;
    private String status;
    private String rejectionReason;
    private Instant createdAt;
    private Instant reviewedAt;
    private List<ApprovalHistoryDto> history;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalHistoryDto {
        private Long id;
        private Long actorUserId;
        private String actorUsername;
        private String action;
        private String actionNote;
        private String ipAddress;
        private Instant createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmitRequest {
        @NotBlank(message = "requestType is required")
        private String requestType;
        @NotBlank(message = "entityType is required")
        private String entityType;
        private String entityId;
        @NotBlank(message = "title is required")
        private String title;
        private String makerNote;
        private String payloadBefore;
        @NotBlank(message = "payloadAfter is required")
        private String payloadAfter;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionRequest {
        @NotBlank(message = "action is required (APPROVED / REJECTED)")
        private String action;
        private String checkerNote;
        private String rejectionReason;
    }
}
