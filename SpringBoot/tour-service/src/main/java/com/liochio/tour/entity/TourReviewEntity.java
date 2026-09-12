package com.liochio.tour.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "tour_reviews", indexes = {
        @Index(name = "idx_review_tour", columnList = "tenant_id, tour_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourReviewEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "tour_id", nullable = false)
    private Long tourId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "rating_score", nullable = false)
    @Builder.Default
    private Integer ratingScore = 5;

    @Column(name = "comment", columnDefinition = "TEXT", nullable = false)
    private String comment;

    @Column(name = "media_attachments", columnDefinition = "JSON")
    private String mediaAttachments;

    @Column(name = "is_approved", nullable = false)
    @Builder.Default
    private Boolean isApproved = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
