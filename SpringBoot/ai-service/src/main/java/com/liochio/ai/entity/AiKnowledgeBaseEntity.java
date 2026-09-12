package com.liochio.ai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "ai_knowledge_base", indexes = {
        @Index(name = "idx_ai_kb_tenant", columnList = "tenant_id, is_active")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiKnowledgeBaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "doc_title", length = 255, nullable = false)
    private String docTitle;

    @Column(name = "content_chunk", columnDefinition = "LONGTEXT", nullable = false)
    private String contentChunk;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
