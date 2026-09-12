package com.liochio.ai.controller;

import com.liochio.common.constant.MessageConstants;
import com.liochio.common.context.TenantContext;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.common.i18n.MessageService;
import com.liochio.ai.entity.AiChatMessageEntity;
import com.liochio.ai.entity.AiChatSessionEntity;
import com.liochio.ai.repository.AiChatMessageRepository;
import com.liochio.ai.repository.AiChatSessionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/ai", "/api/ai"})
@RequiredArgsConstructor
@Tag(name = "AI Chatbot Controller", description = "Dịch vụ độc lập quản lý trợ lý AI RAG, hội thoại và kho tri thức")
public class AiChatbotController {

    private final AiChatSessionRepository sessionRepository;
    private final AiChatMessageRepository messageRepository;
    private final MessageService messageService;

    @PostMapping("/chat/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Khởi tạo phiên hội thoại mới với Chatbot AI")
    public ApiResponse<AiChatSessionEntity> createSession() {
        String tenantId = TenantContext.getTenantId();
        AiChatSessionEntity session = AiChatSessionEntity.builder()
                .tenantId(tenantId)
                .sessionToken(UUID.randomUUID().toString())
                .build();
        return ApiResponse.created(sessionRepository.save(session), messageService.getMessage(MessageConstants.MSG_AI_SESSION_CREATED));
    }

    @GetMapping("/chat/sessions/{sessionId}/messages")
    @Operation(summary = "Lấy lịch sử tin nhắn của một phiên hội thoại")
    public ApiResponse<List<AiChatMessageEntity>> getMessages(@PathVariable Long sessionId) {
        return ApiResponse.success(messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId));
    }

    @PostMapping("/chat/sessions/{sessionId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Gửi tin nhắn và nhận phản hồi từ AI Assistant")
    public ApiResponse<AiChatMessageEntity> sendMessage(
            @PathVariable Long sessionId,
            @RequestBody AiChatMessageEntity request
    ) {
        AiChatSessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Lưu tin nhắn của User
        request.setSessionId(sessionId);
        request.setSenderType("USER");
        messageRepository.save(request);

        // Sinh phản hồi từ AI
        AiChatMessageEntity assistantReply = AiChatMessageEntity.builder()
                .sessionId(sessionId)
                .senderType("ASSISTANT")
                .messageText("Hello! I am the AI Assistant for " + session.getTenantId() + ". How may I assist you today?")
                .tokensUsed(35)
                .build();
        AiChatMessageEntity savedReply = messageRepository.save(assistantReply);

        session.setLastActivityAt(Instant.now());
        sessionRepository.save(session);

        return ApiResponse.created(savedReply, messageService.getMessage(MessageConstants.MSG_AI_MESSAGE_SENT));
    }
}
