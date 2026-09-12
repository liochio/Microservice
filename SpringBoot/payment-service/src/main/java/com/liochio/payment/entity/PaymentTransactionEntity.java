package com.liochio.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Giao Dịch Thanh Toán & Hóa Đơn (Payment Transaction - Bảng 20)
 * ==============================================================================
 */
@Entity
@Table(name = "payment_transactions", indexes = {
        @Index(name = "uk_tenant_transaction", columnList = "tenant_id, transaction_code", unique = true),
        @Index(name = "idx_trans_booking", columnList = "booking_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_code", length = 100, nullable = false)
    private String transactionCode;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "gateway_name", length = 50, nullable = false)
    private String gatewayName;

    @Column(name = "gateway_transaction_id", length = 100)
    private String gatewayTransactionId;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 10, nullable = false)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, SUCCESS, FAILED, REFUNDED

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "gateway_response", columnDefinition = "JSON")
    private String gatewayResponse;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
