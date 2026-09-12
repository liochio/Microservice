package com.liochio.worker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "message_templates", indexes = {
        @Index(name = "idx_tpl_code_lang", columnList = "template_code, language_code")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_code", length = 100, nullable = false)
    private String templateCode;

    @Column(name = "language_code", length = 10, nullable = false)
    @Builder.Default
    private String languageCode = "vi";

    @Column(name = "channel", length = 30, nullable = false)
    @Builder.Default
    private String channel = "EMAIL";

    @Column(name = "subject", length = 255, nullable = false)
    private String subject;

    @Column(name = "body_html", columnDefinition = "TEXT", nullable = false)
    private String bodyHtml;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
