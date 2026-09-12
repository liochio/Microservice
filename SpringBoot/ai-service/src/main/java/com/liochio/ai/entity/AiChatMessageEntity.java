package com.liochio.ai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "ai_chat_messages", indexes = {
        @Index(name = "idx_ai_msg_session", columnList = "session_id, created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatMessageEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "sender_type", length = 30, nullable = false)
    @Builder.Default
    private String senderType = "USER"; // USER, ASSISTANT, SYSTEM

    @Column(name = "message_text", columnDefinition = "LONGTEXT", nullable = false)
    private String messageText;

    @Column(name = "tokens_used")
    @Builder.Default
    private Integer tokensUsed = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
