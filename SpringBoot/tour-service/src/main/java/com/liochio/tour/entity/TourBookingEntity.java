package com.liochio.tour.entity;

import com.liochio.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "tour_bookings")
@SQLDelete(sql = "UPDATE tour_bookings SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourBookingEntity extends BaseEntity {

    @Column(name = "booking_code", length = 50, nullable = false)
    private String bookingCode;

    @Column(name = "tour_id", nullable = false)
    private Long tourId;

    @Column(name = "departure_id")
    private Long departureId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "customer_name", length = 150, nullable = false)
    private String customerName;

    @Column(name = "customer_email", length = 150, nullable = false)
    private String customerEmail;

    @Column(name = "customer_phone", length = 30, nullable = false)
    private String customerPhone;

    @Column(name = "number_of_adults", nullable = false)
    @Builder.Default
    private Integer numberOfAdults = 1;

    @Column(name = "number_of_children", nullable = false)
    @Builder.Default
    private Integer numberOfChildren = 0;

    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "currency", length = 10)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "booking_status", length = 30)
    @Builder.Default
    private String bookingStatus = "PENDING";

    @Column(name = "payment_status", length = 30)
    @Builder.Default
    private String paymentStatus = "UNPAID";

    @Column(name = "payment_order_id", length = 100)
    private String paymentOrderId;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;
}
