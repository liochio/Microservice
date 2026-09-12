package com.liochio.common.saga;

/**
 * ==============================================================================
 * Trạng Thái Vòng Đời Giao Dịch Phân Tán (Saga Distributed Transaction Status)
 * ==============================================================================
 */
public enum SagaStatus {
    STARTED,
    PENDING,
    STEP_COMPLETED,
    COMPLETED,
    FAILED,
    COMPENSATING,
    COMPENSATED_REVERSED,
    COMPENSATION_FAILED
}
