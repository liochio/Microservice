package com.liochio.film.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "movie_genres")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieGenreEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "slug", length = 100, nullable = false)
    private String slug;

    @Column(name = "description", length = 255)
    private String description;
}
