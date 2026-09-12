package com.liochio.film.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "movie_episodes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieEpisodeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(name = "episode_number", nullable = false)
    private Integer episodeNumber;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "video_cdn_url", length = 500, nullable = false)
    private String videoCdnUrl;

    @Column(name = "subtitles", columnDefinition = "JSON")
    private String subtitles;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;
}
