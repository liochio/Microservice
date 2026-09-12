package com.liochio.music.entity;

import com.liochio.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "music_genres")
@SQLDelete(sql = "UPDATE music_genres SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicGenreEntity extends BaseEntity {

    @Column(name = "genre_code", length = 50, nullable = false)
    private String genreCode;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;
}
