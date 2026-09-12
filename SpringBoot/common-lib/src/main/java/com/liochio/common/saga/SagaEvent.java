package com.liochio.common.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * ==============================================================================
 * Sự Kiện Giao Dịch Phân Tán Saga (Distributed Saga Event Standard)
 * ==============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sagaId;
    private String traceId;
    private String tenantId;
    private String sagaType; // PAYMENT_CHECKOUT, WALLET_TRANSFER, SUBSCRIPTION
    private String currentStep;
    private SagaStatus status;
    private String failureReason;
    
    private Map<String, Object> payload;
    private Map<String, Object> compensationPayload;
    
    @Builder.Default
    private Instant timestamp = Instant.now();
}
