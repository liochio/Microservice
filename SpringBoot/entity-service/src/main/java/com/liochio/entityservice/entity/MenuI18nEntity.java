package com.liochio.entityservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "menu_i18n", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"menu_code", "lang"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuI18nEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_code", length = 80, nullable = false)
    private String menuCode;

    @Column(name = "lang", length = 10, nullable = false)
    private String lang;

    @Column(name = "title", length = 150, nullable = false)
    private String title;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
