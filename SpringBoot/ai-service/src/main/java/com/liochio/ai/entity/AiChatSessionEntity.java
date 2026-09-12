package com.liochio.ai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "ai_chat_sessions", indexes = {
        @Index(name = "idx_ai_session_lookup", columnList = "tenant_id, session_token")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatSessionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;

    @Column(name = "session_token", length = 100, nullable = false)
    private String sessionToken;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "guest_ip", length = 50)
    private String guestIp;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "last_activity_at", nullable = false)
    @Builder.Default
    private Instant lastActivityAt = Instant.now();
}
