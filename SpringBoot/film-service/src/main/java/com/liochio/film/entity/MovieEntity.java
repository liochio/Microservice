package com.liochio.film.entity;

import com.liochio.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "movies", indexes = {
        @Index(name = "uk_tenant_movie", columnList = "tenant_id, slug", unique = true)
})
@SQLDelete(sql = "UPDATE movies SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieEntity extends BaseEntity {

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "original_title", length = 255)
    private String originalTitle;

    @Column(name = "slug", length = 255, nullable = false)
    private String slug;

    @Column(name = "movie_type", length = 30, nullable = false)
    @Builder.Default
    private String movieType = "SINGLE";

    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    @Column(name = "trailer_url", length = 500)
    private String trailerUrl;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(name = "quality", length = 20)
    @Builder.Default
    private String quality = "HD";

    @Column(name = "age_rating", length = 10)
    @Builder.Default
    private String ageRating = "16+";

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "director", length = 150)
    private String director;

    @Column(name = "cast_members", columnDefinition = "JSON")
    private String castMembers;

    @Column(name = "genres", columnDefinition = "JSON")
    private String genres;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "rating_avg", precision = 3, scale = 1)
    @Builder.Default
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "PUBLISHED";
}
