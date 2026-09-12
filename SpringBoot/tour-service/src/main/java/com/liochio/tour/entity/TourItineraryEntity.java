package com.liochio.tour.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "tour_itineraries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourItineraryEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tour_id", nullable = false)
    private Long tourId;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "meals", length = 100)
    private String meals;

    @Column(name = "media_gallery", columnDefinition = "JSON")
    private String mediaGallery;
}
