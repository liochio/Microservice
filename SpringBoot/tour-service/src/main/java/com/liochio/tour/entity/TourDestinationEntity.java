package com.liochio.tour.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "tour_destinations", indexes = {
        @Index(name = "uk_tenant_dest_slug", columnList = "tenant_id, slug", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourDestinationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "slug", length = 150, nullable = false)
    private String slug;

    @Column(name = "country", length = 100)
    @Builder.Default
    private String country = "Việt Nam";

    @Column(name = "region", length = 50)
    private String region;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
