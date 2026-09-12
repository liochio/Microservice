package com.liochio.ai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "tenant_ai_configs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantAiConfigEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "ai_provider", length = 50, nullable = false)
    @Builder.Default
    private String aiProvider = "OPENAI";

    @Column(name = "api_key_encrypted", length = 500)
    private String apiKeyEncrypted;

    @Column(name = "model_name", length = 50)
    @Builder.Default
    private String modelName = "gpt-4o-mini";

    @Column(name = "system_prompt", columnDefinition = "TEXT")
    private String systemPrompt;

    @Column(name = "temperature", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal temperature = new BigDecimal("0.70");

    @Column(name = "max_tokens")
    @Builder.Default
    private Integer maxTokens = 2000;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
