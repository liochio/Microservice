package com.liochio.tour.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tour_departures")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourDepartureEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tour_id", nullable = false)
    private Long tourId;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(name = "max_slots", nullable = false)
    private Integer maxSlots;

    @Column(name = "booked_slots", nullable = false)
    @Builder.Default
    private Integer bookedSlots = 0;

    @Column(name = "adult_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal adultPrice;

    @Column(name = "child_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal childPrice;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "OPEN";
}
