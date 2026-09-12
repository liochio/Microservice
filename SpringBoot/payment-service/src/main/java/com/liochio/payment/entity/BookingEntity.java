package com.liochio.payment.entity;

import com.liochio.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * ==============================================================================
 * Thực Thể Đơn Hàng & Đặt Chỗ (Booking Entity - Bảng 18)
 * ==============================================================================
 */
@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "uk_tenant_booking_code", columnList = "tenant_id, booking_code", unique = true),
        @Index(name = "idx_booking_customer", columnList = "tenant_id, customer_id, status, is_deleted")
})
@SQLDelete(sql = "UPDATE bookings SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEntity extends BaseEntity {

    @Column(name = "booking_code", length = 50, nullable = false)
    private String bookingCode;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "booking_date", nullable = false)
    @Builder.Default
    private Instant bookingDate = Instant.now();

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "unit_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "discount_amount", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "currency", length = 10, nullable = false)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, CONFIRMED, PAID, CANCELLED, REFUNDED

    @Column(name = "payment_status", length = 30, nullable = false)
    @Builder.Default
    private String paymentStatus = "UNPAID"; // UNPAID, PARTIALLY_PAID, PAID, REFUNDED

    @Column(name = "booking_payload", columnDefinition = "JSON", nullable = false)
    private String bookingPayload;

    @Column(name = "customer_note", columnDefinition = "TEXT")
    private String customerNote;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;
}
