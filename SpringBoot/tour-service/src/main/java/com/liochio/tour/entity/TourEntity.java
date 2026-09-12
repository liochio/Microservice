package com.liochio.tour.entity;

import com.liochio.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "tours", indexes = {
        @Index(name = "uk_tenant_tour_slug", columnList = "tenant_id, slug", unique = true),
        @Index(name = "idx_tour_lookup", columnList = "tenant_id, status, is_deleted")
})
@SQLDelete(sql = "UPDATE tours SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourEntity extends BaseEntity {

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "slug", length = 255, nullable = false)
    private String slug;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "duration_nights", nullable = false)
    private Integer durationNights;

    @Column(name = "departure_location", length = 150)
    private String departureLocation;

    @Column(name = "destination", length = 150)
    private String destination;

    @Column(name = "base_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal basePrice;

    @Column(name = "transportation", length = 100)
    private String transportation;

    @Column(name = "included_services", columnDefinition = "JSON")
    private String includedServices;

    @Column(name = "excluded_services", columnDefinition = "JSON")
    private String excludedServices;

    @Column(name = "policies_refund", columnDefinition = "TEXT")
    private String policiesRefund;

    @Column(name = "i18n_content", columnDefinition = "JSON")
    private String i18nContent;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "PUBLISHED";

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;
}
