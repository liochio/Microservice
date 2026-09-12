package com.liochio.music.entity;

import com.liochio.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "songs", indexes = {
        @Index(name = "idx_song_lookup", columnList = "tenant_id, slug")
})
@SQLDelete(sql = "UPDATE songs SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongEntity extends BaseEntity {

    @Column(name = "album_id")
    private Long albumId;

    @Column(name = "artist_id")
    private Long artistId;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "slug", length = 255, nullable = false)
    private String slug;

    @Column(name = "audio_url", length = 500, nullable = false)
    private String audioUrl;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "bitrate", length = 20)
    @Builder.Default
    private String bitrate = "320kbps";

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    @Column(name = "composer", length = 150)
    private String composer;

    @Column(name = "lyrics_lrc", columnDefinition = "TEXT")
    private String lyricsLrc;

    @Column(name = "play_count", nullable = false)
    @Builder.Default
    private Long playCount = 0L;

    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Long likeCount = 0L;

    @Column(name = "is_premium", nullable = false)
    @Builder.Default
    private Boolean isPremium = false;
}
