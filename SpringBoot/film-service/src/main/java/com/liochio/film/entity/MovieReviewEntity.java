package com.liochio.film.entity;

import com.liochio.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "movie_reviews")
@SQLDelete(sql = "UPDATE movie_reviews SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieReviewEntity extends BaseEntity {

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name", length = 100, nullable = false)
    private String userName;

    @Column(name = "rating", precision = 2, scale = 1, nullable = false)
    private BigDecimal rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
}
