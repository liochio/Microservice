package com.liochio.music.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "albums")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "artist_id")
    private Long artistId;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "release_year")
    private Integer releaseYear;
}
